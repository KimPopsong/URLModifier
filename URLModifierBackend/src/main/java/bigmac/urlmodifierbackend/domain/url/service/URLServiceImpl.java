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
import java.util.List;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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
    // 클릭 수 한도 검사+증가를 원자적으로 수행하는 Lua 스크립트 (경쟁 조건 방지)
    private static final RedisScript<Long> CHECK_AND_INCREMENT_CLICKS_SCRIPT = RedisScript.of(
        new ClassPathResource("scripts/check_and_increment_clicks.lua"), Long.class);
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
        cacheURLAfterCommit(saved);

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
        cacheURLAfterCommit(saved);

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
                if (!tryConsumeClick(cached.getId(), cached.getMaxClicks(), urlRef)) {
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
                if (!tryConsumeClick(url.getId(), url.getMaxClicks(), url)) {
                    throw new URLExpiredException("클릭 수 초과");
                }
            }

            urlId = url.getId();
            urlForReturn = url;
        }

        // 클릭 이벤트 저장: getReferenceById로 SELECT 없이 FK 참조
        // (maxClicks 카운터는 위 tryConsumeClick에서 이미 원자적으로 증가됨)
        URL urlRef = urlRepository.getReferenceById(urlId);
        clickEventRepository.save(
            ClickEvent.builder().url(urlRef).referrer(referrer).ipAddress(ipAddress)
                .userAgent(userAgent).build());

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

        evictURLCacheAfterCommit(url.getShortenedURL(), urlId);
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

    /**
     * 트랜잭션 커밋 이후에 캐시를 적재한다. 커밋이 롤백되면 캐시에 유령 항목이 남는 문제를 방지.
     * (트랜잭션이 없으면 즉시 적재)
     */
    private void cacheURLAfterCommit(URL url) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    cacheURL(url);
                }
            });
        } else {
            cacheURL(url);
        }
    }

    /**
     * 트랜잭션 커밋 이후에 캐시를 무효화한다. 삭제가 롤백되면 캐시가 먼저 지워지는 창(window)을 방지.
     */
    private void evictURLCacheAfterCommit(String slug, Long urlId) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    evictURLCache(slug, urlId);
                }
            });
        } else {
            evictURLCache(slug, urlId);
        }
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
     * maxClicks 한도를 원자적으로 검사하고, 한도 내이면 Redis 카운터를 증가시킨다.
     * 검사와 증가 사이에 다른 요청이 끼어들 수 없어 동시 요청으로 인한 한도 초과를 방지한다.
     *
     * @return true = 허용됨(카운터 증가 완료), false = 한도 초과(카운터 변화 없음)
     */
    private boolean tryConsumeClick(Long urlId, Integer maxClicks, URL urlRef) {
        String countKey = URL_CLICK_COUNT + urlId;
        Long result = executeClickScript(countKey, maxClicks);

        if (result != null && result == -2L) {
            // 카운터가 아직 없음: DB에서 초기값을 읽어 원자적으로 세팅(setIfAbsent) 후 재시도.
            // 동시에 여러 요청이 초기화를 시도해도 setIfAbsent로 하나만 반영되므로 안전하다.
            long dbCount = clickEventRepository.countByUrl(urlRef);
            stringRedisTemplate.opsForValue()
                .setIfAbsent(countKey, String.valueOf(dbCount), URL_CACHE_TTL_SECONDS, TimeUnit.SECONDS);
            result = executeClickScript(countKey, maxClicks);
        }

        return result != null && result != -1L && result != -2L;
    }

    private Long executeClickScript(String countKey, Integer maxClicks) {
        return stringRedisTemplate.execute(CHECK_AND_INCREMENT_CLICKS_SCRIPT, List.of(countKey),
            String.valueOf(maxClicks));
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
