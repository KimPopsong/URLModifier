package bigmac.urlmodifierbackend.domain.url.service;

import bigmac.urlmodifierbackend.domain.url.dto.URLCacheDto;
import bigmac.urlmodifierbackend.domain.url.dto.request.CustomURLRequest;
import bigmac.urlmodifierbackend.domain.url.dto.request.URLRequest;
import bigmac.urlmodifierbackend.domain.url.dto.response.URLDetailResponse;
import bigmac.urlmodifierbackend.domain.url.exception.URLException;
import bigmac.urlmodifierbackend.domain.url.exception.URLExpiredException;
import bigmac.urlmodifierbackend.domain.url.model.ClickEvent;
import bigmac.urlmodifierbackend.domain.url.model.URL;
import bigmac.urlmodifierbackend.domain.url.repository.ClickEventRepository;
import bigmac.urlmodifierbackend.domain.url.repository.URLRepository;
import bigmac.urlmodifierbackend.domain.user.exception.URLFindException;
import bigmac.urlmodifierbackend.domain.user.model.User;
import bigmac.urlmodifierbackend.domain.user.repository.UserRepository;
import bigmac.urlmodifierbackend.global.util.Base62;
import bigmac.urlmodifierbackend.global.util.QRCodeUtil;
import bigmac.urlmodifierbackend.global.util.SnowflakeIdGenerator;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class URLServiceImpl implements URLService {

    private static final String URL_SLUG_CACHE = "url:slug:";
    private static final String URL_CLICK_COUNT = "url:clicks:";
    private static final long URL_CACHE_TTL_SECONDS = 3600L;
    // 애플리케이션 경로와 겹쳐 리다이렉트가 불가능해지는 슬러그
    private static final Set<String> RESERVED_SLUGS = Set.of("short-urls", "urls", "auth", "me",
        "swagger-ui", "v3", "api-docs", "swagger-resources", "webjars", "actuator", "error");
    private final URLValidateServiceImpl urlValidateService;
    private final URLRepository urlRepository;
    private final ClickEventRepository clickEventRepository;
    private final UserRepository userRepository;
    private final SnowflakeIdGenerator idGenerator;
    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    @Value("${custom.BE_BASE_URL}")
    private String BE_BASE_URL;
    @Value("${custom.FE_BASE_URL}")
    private String FE_BASE_URL;

    @Transactional
    @Override
    public URL makeURLShort(@Nullable User user, URLRequest urlRequest) {
        String originURL = urlRequest.getUrl();

        Optional<URL> existingURL = (user != null)
            ? urlRepository.findByUserAndOriginURL(user, originURL)
            : urlRepository.findFirstByOriginURLAndUserIsNull(originURL);

        if (existingURL.isPresent()) {
            return existingURL.get();
        }

        urlValidateService.validateOriginalUrl(originURL);

        // 커스텀 URL 등과 충돌 시 새 ID로 재생성 (ID가 같으면 인코딩 결과도 같으므로 ID부터 다시 발급)
        long id;
        String shortenedURL;
        do {
            id = idGenerator.nextId();
            shortenedURL = Base62.encode(id);
        } while (urlRepository.findByShortenedURL(shortenedURL).isPresent());

        String qrCodeBase64 = generateQRCode(BE_BASE_URL + shortenedURL);

        URL newUrl = new URL(id, user, originURL, shortenedURL, qrCodeBase64);
        if (user != null) {
            newUrl.setExpiresAt(urlRequest.getExpiresAt());
            newUrl.setMaxClicks(urlRequest.getMaxClicks());
        }

        URL saved = urlRepository.save(newUrl);
        cacheURL(saved);

        return saved;
    }

    @Transactional
    @Override
    public URL makeCustomURL(User user, CustomURLRequest customURLRequest) {
        this.checkUser(user);
        urlValidateService.validateOriginalUrl(customURLRequest.getOriginURL());

        if (RESERVED_SLUGS.contains(customURLRequest.getCustomURL().toLowerCase(Locale.ROOT))) {
            throw new URLException("사용할 수 없는 커스텀 URL입니다.");
        }

        Optional<URL> existingCustomURL = urlRepository.findByUserAndOriginURL(user,
            customURLRequest.getOriginURL());

        if (existingCustomURL.isPresent()) {
            throw new URLException("이미 해당 URL로 커스텀 URL을 생성하셨습니다.");
        }

        Optional<URL> shortenedURL = urlRepository.findByShortenedURL(
            customURLRequest.getCustomURL());

        if (shortenedURL.isPresent()) {
            throw new URLException("이미 존재하는 단축 URL입니다.");
        }

        String fullShortenedURL = BE_BASE_URL + customURLRequest.getCustomURL();
        String qrCodeBase64 = generateQRCode(fullShortenedURL);

        URL newUrl = new URL(idGenerator.nextId(), user, customURLRequest.getOriginURL(),
            customURLRequest.getCustomURL(), qrCodeBase64);
        newUrl.setExpiresAt(customURLRequest.getExpiresAt());
        newUrl.setMaxClicks(customURLRequest.getMaxClicks());

        URL saved = urlRepository.save(newUrl);
        cacheURL(saved);

        return saved;
    }

    @Override
    public Optional<URL> getOriginURLByShortURL(String shortenedUrl) {
        URLCacheDto cached = getURLFromCache(shortenedUrl);

        if (cached != null) {
            return Optional.of(buildURLFromCache(cached));
        }

        Optional<URL> result = urlRepository.findByShortenedURL(shortenedUrl);
        result.ifPresent(this::cacheURL);

        return result;
    }

    @Transactional
    @Override
    public URL redirectToOriginal(String referrer, String userAgent, String ipAddress,
        String shortenedUrl) {
        URLCacheDto cached = getURLFromCache(shortenedUrl);
        URL urlForReturn;
        Long urlId;

        if (cached != null) {
            // 캐시 히트: DB 조회 없이 유효성 검사
            if (cached.getExpiresAt() != null && LocalDateTime.now()
                .isAfter(cached.getExpiresAt())) {
                throw new URLExpiredException("시간 만료");
            }

            if (cached.getMaxClicks() != null) {
                URL urlRef = urlRepository.getReferenceById(cached.getId());
                long clickCount = getOrInitClickCount(cached.getId(), urlRef);
                if (clickCount >= cached.getMaxClicks()) {
                    throw new URLExpiredException("클릭 수 초과");
                }
            }

            urlId = cached.getId();
            urlForReturn = buildURLFromCache(cached);
        } else {
            // 캐시 미스: DB 조회 후 캐시 저장
            URL url = urlRepository.findByShortenedURL(shortenedUrl).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "URL이 존재하지 않습니다."));
            cacheURL(url);

            if (url.getExpiresAt() != null && LocalDateTime.now().isAfter(url.getExpiresAt())) {
                throw new URLExpiredException("시간 만료");
            }

            if (url.getMaxClicks() != null) {
                long clickCount = getOrInitClickCount(url.getId(), url);
                if (clickCount >= url.getMaxClicks()) {
                    throw new URLExpiredException("클릭 수 초과");
                }
            }

            urlId = url.getId();
            urlForReturn = url;
        }

        // 클릭 이벤트 저장: getReferenceById로 SELECT 없이 FK 참조
        URL urlRef = urlRepository.getReferenceById(urlId);
        clickEventRepository.save(
            ClickEvent.builder().url(urlRef).referrer(referrer).ipAddress(ipAddress)
                .userAgent(userAgent).build());

        // maxClicks 있는 URL만 Redis 카운터 증가
        if (urlForReturn.getMaxClicks() != null) {
            stringRedisTemplate.opsForValue().increment(URL_CLICK_COUNT + urlId);
        }

        return urlForReturn;
    }

    @Transactional
    @Override
    public void deleteUrl(User user, Long urlId) {
        this.checkUser(user);

        URL url = findUrlOrThrowException(urlId);
        this.validateUrlOwnership(user, url);

        clickEventRepository.deleteAllByUrl(url);
        urlRepository.deleteById(urlId);

        evictURLCache(url.getShortenedURL(), urlId);
    }

    @Override
    public URLDetailResponse detailUrl(User user, Long urlId) {
        this.checkUser(user);

        URL url = findUrlOrThrowException(urlId);
        this.validateUrlOwnership(user, url);

        // 클릭 이벤트를 모두 메모리에 올리지 않고 DB에서 집계
        Map<String, Long> dailyClicks = clickEventRepository.countDailyClicks(url).stream()
            .collect(Collectors.toMap(row -> (String) row[0], row -> (Long) row[1],
                (a, b) -> a, LinkedHashMap::new));

        long totalClicks = dailyClicks.values().stream().mapToLong(Long::longValue).sum();

        return new URLDetailResponse(String.valueOf(url.getId()), url.getOriginURL(),
            (BE_BASE_URL + url.getShortenedURL()).replaceFirst("https?://", ""),
            url.getQrCode(), url.getCreatedAt(), url.getExpiresAt(),
            url.getMaxClicks(), totalClicks, dailyClicks);
    }

    // ── Cache helpers ─────────────────────────────────────────────────────────

    private URLCacheDto getURLFromCache(String slug) {
        Object cached = redisTemplate.opsForValue().get(URL_SLUG_CACHE + slug);
        if (cached instanceof URLCacheDto) {
            return (URLCacheDto) cached;
        }
        return null;
    }

    private void cacheURL(URL url) {
        URLCacheDto dto = new URLCacheDto(url.getId(), url.getOriginURL(), url.getShortenedURL(),
            url.getExpiresAt(), url.getMaxClicks());
        redisTemplate.opsForValue()
            .set(URL_SLUG_CACHE + url.getShortenedURL(), dto, URL_CACHE_TTL_SECONDS,
                TimeUnit.SECONDS);
    }

    private void evictURLCache(String slug, Long urlId) {
        redisTemplate.delete(URL_SLUG_CACHE + slug);
        stringRedisTemplate.delete(URL_CLICK_COUNT + urlId);
    }

    private URL buildURLFromCache(URLCacheDto dto) {
        URL url = new URL();
        url.setId(dto.getId());
        url.setOriginURL(dto.getOriginURL());
        url.setShortenedURL(dto.getShortenedURL());
        url.setExpiresAt(dto.getExpiresAt());
        url.setMaxClicks(dto.getMaxClicks());
        return url;
    }

    /**
     * Redis 클릭 카운터 조회. 키 미존재 시 DB에서 초기값 설정.
     */
    private long getOrInitClickCount(Long urlId, URL urlRef) {
        String countKey = URL_CLICK_COUNT + urlId;
        String val = stringRedisTemplate.opsForValue().get(countKey);
        if (val != null) {
            return Long.parseLong(val);
        }
        // 캐시 미스: DB에서 현재 클릭 수를 읽어 초기화 (동시 요청 대비 setIfAbsent 사용)
        long dbCount = clickEventRepository.countByUrl(urlRef);
        stringRedisTemplate.opsForValue().setIfAbsent(countKey, String.valueOf(dbCount), URL_CACHE_TTL_SECONDS, TimeUnit.SECONDS);
        return dbCount;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private String generateQRCode(String url) {
        try {
            byte[] qrImage = QRCodeUtil.generateQRCodeImage(url, 200, 200);
            return Base64.getEncoder().encodeToString(qrImage);
        } catch (Exception e) {
            log.error("QR 코드 생성 중 오류 발생: {}", url, e);
            throw new URLException("QR 코드 생성 중 오류가 발생하였습니다.");
        }
    }

    private URL findUrlOrThrowException(Long urlId) {
        return urlRepository.findById(urlId)
            .orElseThrow(() -> new URLFindException("유효하지 않은 URL입니다."));
    }

    private void validateUrlOwnership(User user, URL url) {
        if (!Objects.equals(url.getUser().getId(), user.getId())) {
            throw new URLException("본인의 URL이 아닙니다.");
        }
    }

    private void checkUser(User user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다");
        }

        userRepository.findById(user.getId()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 사용자입니다."));
    }
}
