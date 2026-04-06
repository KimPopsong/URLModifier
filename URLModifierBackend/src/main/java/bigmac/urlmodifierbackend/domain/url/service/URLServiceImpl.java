package bigmac.urlmodifierbackend.domain.url.service;

import bigmac.urlmodifierbackend.domain.url.dto.request.CustomURLRequest;
import bigmac.urlmodifierbackend.domain.url.dto.request.URLRequest;
import bigmac.urlmodifierbackend.domain.url.dto.response.ClickEventResponse;
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    private final URLValidateServiceImpl urlValidateService;
    private final URLRepository urlRepository;
    private final ClickEventRepository clickEventRepository;
    private final UserRepository userRepository;
    private final SnowflakeIdGenerator idGenerator;

    @Value("${custom.BE_BASE_URL}")
    private String BE_BASE_URL;
    @Value("${custom.FE_BASE_URL}")
    private String FE_BASE_URL;

    @Transactional
    @Override
    public URL makeURLShort(@Nullable User user, URLRequest urlRequest) {
        String originURL = urlRequest.getUrl();

        if (user != null) {
            Optional<URL> existingUserURL = urlRepository.findByUserAndOriginURL(user, originURL);
            if (existingUserURL.isPresent()) {
                return existingUserURL.get();
            }

            urlValidateService.validateOriginalUrl(originURL);

            long id = idGenerator.nextId();
            String shortenedURL;
            do {
                shortenedURL = Base62.encode(id);
            } while (urlRepository.findByShortenedURL(shortenedURL).isPresent());

            String fullShortenedURL = BE_BASE_URL + shortenedURL;
            String qrCodeBase64 = generateQRCode(fullShortenedURL);

            URL newUrl = new URL(id, user, originURL, shortenedURL, qrCodeBase64);
            newUrl.setExpiresAt(urlRequest.getExpiresAt());
            newUrl.setMaxClicks(urlRequest.getMaxClicks());

            return urlRepository.save(newUrl);
        }

        Optional<URL> anonymousURL = urlRepository.findFirstByOriginURLAndUserIsNull(originURL);
        if (anonymousURL.isPresent()) {
            return anonymousURL.get();
        }

        urlValidateService.validateOriginalUrl(originURL);

        long id = idGenerator.nextId();
        String shortenedURL;
        do {
            shortenedURL = Base62.encode(id);
        } while (urlRepository.findByShortenedURL(shortenedURL).isPresent());

        String fullShortenedURL = BE_BASE_URL + shortenedURL;
        String qrCodeBase64 = generateQRCode(fullShortenedURL);

        return urlRepository.save(new URL(id, null, originURL, shortenedURL, qrCodeBase64));
    }

    @Transactional
    @Override
    public URL makeCustomURL(User user, CustomURLRequest customURLRequest) {
        this.checkUser(user);
        urlValidateService.validateOriginalUrl(customURLRequest.getOriginURL());

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

        int customURLLength = customURLRequest.getCustomURL().length();
        if (customURLLength < 1 || customURLLength > 30) {
            throw new URLException("커스텀 URL은 1자 이상 30자 이하여야 합니다. (현재: " + customURLLength + "자)");
        }

        String fullShortenedURL = BE_BASE_URL + customURLRequest.getCustomURL();
        String qrCodeBase64 = generateQRCode(fullShortenedURL);

        URL newUrl = new URL(idGenerator.nextId(), user, customURLRequest.getOriginURL(),
            customURLRequest.getCustomURL(), qrCodeBase64);
        newUrl.setExpiresAt(customURLRequest.getExpiresAt());
        newUrl.setMaxClicks(customURLRequest.getMaxClicks());

        return urlRepository.save(newUrl);
    }

    @Override
    public Optional<URL> getOriginURLByShortURL(String shortenedUrl) {
        return urlRepository.findByShortenedURL(shortenedUrl);
    }

    @Transactional
    @Override
    public URL redirectToOriginal(String referrer, String userAgent, String ipAddress,
        String shortenedUrl) {
        URL url = urlRepository.findByShortenedURL(shortenedUrl)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "URL이 존재하지 않습니다."));

        if (url.getExpiresAt() != null && LocalDateTime.now().isAfter(url.getExpiresAt())) {
            throw new URLExpiredException("시간 만료");
        }

        if (url.getMaxClicks() != null) {
            long clickCount = clickEventRepository.countByUrl(url);
            if (clickCount >= url.getMaxClicks()) {
                throw new URLExpiredException("클릭 수 초과");
            }
        }

        ClickEvent click = ClickEvent.builder().url(url).referrer(referrer).ipAddress(ipAddress)
            .userAgent(userAgent).build();
        clickEventRepository.save(click);

        return url;
    }

    @Transactional
    @Override
    public void deleteUrl(User user, Long urlId) {
        this.checkUser(user);

        URL url = findUrlOrThrowException(urlId);
        this.validateUrlOwnership(user, url);

        clickEventRepository.deleteAll(clickEventRepository.findAllByUrl(url));
        urlRepository.deleteById(urlId);
    }

    @Override
    public URLDetailResponse detailUrl(User user, Long urlId) {
        this.checkUser(user);

        URL url = findUrlOrThrowException(urlId);
        this.validateUrlOwnership(user, url);

        List<ClickEventResponse> allClickEvent = clickEventRepository.findAllByUrl(url).stream()
            .map(ClickEventResponse::from).collect(java.util.stream.Collectors.toList());

        return new URLDetailResponse(String.valueOf(url.getId()), url.getOriginURL(),
            url.getShortenedURL(), url.getQrCode(), url.getCreatedAt(), url.getExpiresAt(),
            url.getMaxClicks(), allClickEvent);
    }

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
