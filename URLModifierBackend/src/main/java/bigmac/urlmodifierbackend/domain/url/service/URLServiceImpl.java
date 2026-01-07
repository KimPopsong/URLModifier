package bigmac.urlmodifierbackend.domain.url.service;

import bigmac.urlmodifierbackend.domain.url.dto.request.CustomURLRequest;
import bigmac.urlmodifierbackend.domain.url.dto.response.URLDetailResponse;
import bigmac.urlmodifierbackend.domain.url.exception.URLException;
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

    /**
     * 원본 URL을 받아 단축 URL과 QR 코드를 생성
     * <p>
     * 주어진 URL이 DB에 있는지 확인 후 있다면 DB에 있는 값 반환. 없다면 단축 후 DB 저장 및 반환
     *
     * @param originURL 기본 URL
     * @param user      사용자 (nullable)
     * @return 단축된 URL
     */
    @Transactional
    @Override
    public URL makeURLShort(@Nullable User user, String originURL) {
        // 로그인한 사용자가 일반 URL을 만들 때
        if (user != null) {
            // 해당 사용자가 이미 같은 originURL로 만든 URL이 있는지 확인
            Optional<URL> existingUserURL = urlRepository.findByUserAndOriginURL(user, originURL);
            if (existingUserURL.isPresent()) {
                // 이미 만든 URL이 있으면 기존 것을 반환
                return existingUserURL.get();
            }

            // 새로 생성 (마이페이지에서 관리하기 위해)
            urlValidateService.validateOriginalUrl(originURL);

            long id = idGenerator.nextId();
            String shortenedURL;

            // 겹치지 않는 URL 생성
            do {
                shortenedURL = Base62.encode(id);
            } while (urlRepository.findByShortenedURL(shortenedURL).isPresent());

            // 단축 URL을 기반으로 QR 코드 생성 (통계 수집을 위해)
            String fullShortenedURL = BE_BASE_URL + shortenedURL;
            String qrCodeBase64 = generateQRCode(fullShortenedURL);

            URL newUrl = new URL(id, user, originURL, shortenedURL, qrCodeBase64);

            return urlRepository.save(newUrl);
        }

        // 비로그인 사용자는 user가 null인 기존 URL만 재사용
        // 로그인한 사용자의 URL이 있으면 새로 생성해야 함
        Optional<URL> anonymousURL = urlRepository.findFirstByOriginURLAndUserIsNull(originURL);

        if (anonymousURL.isPresent()) {
            // 비로그인 사용자가 이미 만든 URL이 있으면 재사용
            return anonymousURL.get();
        }

        // 새로 생성 (비로그인 사용자는 user가 null)
        urlValidateService.validateOriginalUrl(originURL);

        long id = idGenerator.nextId();
        String shortenedURL;

        // 겹치지 않는 URL 생성
        do {
            shortenedURL = Base62.encode(id);
        } while (urlRepository.findByShortenedURL(shortenedURL).isPresent());

        // 단축 URL을 기반으로 QR 코드 생성 (통계 수집을 위해)
        String fullShortenedURL = BE_BASE_URL + shortenedURL;
        String qrCodeBase64 = generateQRCode(fullShortenedURL);

        URL newUrl = new URL(id, null, originURL, shortenedURL, qrCodeBase64);

        return urlRepository.save(newUrl);
    }

    /**
     * 커스텀 URL을 생성
     *
     * @param user             사용자 정보
     * @param customURLRequest 생성하려는 원본 URL과 커스텀 URL
     */
    @Transactional
    @Override
    public URL makeCustomURL(User user, CustomURLRequest customURLRequest) {
        this.checkUser(user);
        urlValidateService.validateOriginalUrl(customURLRequest.getOriginURL());

        // 사용자가 이미 같은 originURL로 커스텀 URL을 만들었는지 확인
        Optional<URL> existingCustomURL = urlRepository.findByUserAndOriginURL(user,
            customURLRequest.getOriginURL());
        if (existingCustomURL.isPresent()) {
            throw new URLException("이미 해당 URL로 커스텀 URL을 생성하셨습니다.");
        }

        // 커스텀 단축 URL이 이미 사용 중인지 확인
        Optional<URL> shortenedURL = urlRepository.findByShortenedURL(
            customURLRequest.getCustomURL());

        if (shortenedURL.isPresent()) {  // 이미 생성된 url이 있다면
            throw new URLException("이미 존재하는 단축 URL입니다.");
        }
        
        // 커스텀 URL 길이 검증 (1~30자)
        int customURLLength = customURLRequest.getCustomURL().length();
        if (customURLLength < 1 || customURLLength > 30) {
            throw new URLException("커스텀 URL은 1자 이상 30자 이하여야 합니다. (현재: " + customURLLength + "자)");
        }
        
        // 단축 URL을 기반으로 QR 코드 생성 (통계 수집을 위해)
        String fullShortenedURL = BE_BASE_URL + customURLRequest.getCustomURL();
        String qrCodeBase64 = generateQRCode(fullShortenedURL);

        URL newUrl = new URL(idGenerator.nextId(), user, customURLRequest.getOriginURL(),
            customURLRequest.getCustomURL(), qrCodeBase64);

        return urlRepository.save(newUrl);
    }

    /**
     * 원본 URL 확인
     *
     * @param shortenedUrl 단축 URL
     * @return Optional<URL>
     */
    @Override
    public Optional<URL> getOriginURLByShortURL(String shortenedUrl) {
        return urlRepository.findByShortenedURL(shortenedUrl);
    }

    /**
     * 단축 URL을 받아 원본 URL로 리디렉션
     *
     * @param referrer     접속한 사람이 사용한 웹사이트
     * @param userAgent    접속한 사람이 사용한 브라우저
     * @param ipAddress    접속한 사람의 IP
     * @param shortenedUrl 단축 URL
     * @return Optional<URL>
     */
    @Transactional
    @Override
    public URL redirectToOriginal(String referrer, String userAgent, String ipAddress,
        String shortenedUrl) {
        Optional<URL> optionalUrl = urlRepository.findByShortenedURL(shortenedUrl);

        if (optionalUrl.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "URL이 존재하지 않습니다.");  // 404 Not Found 에러 발생
        }

        URL url = optionalUrl.get();

        ClickEvent click = ClickEvent.builder().url(url).referrer(referrer).ipAddress(ipAddress)
            .userAgent(userAgent).build();  // 클릭 이벤트 생성 및 저장
        clickEventRepository.save(click);

        return url;
    }

    /**
     * 단축 URL 제거
     *
     * @param user  사용자 정보
     * @param urlId 제거하려는 URL의 ID
     */
    @Transactional
    @Override
    public void deleteUrl(User user, Long urlId) {
        this.checkUser(user);

        URL url = findUrlOrThrowException(urlId);

        this.validateUrlOwnership(user, url);

        urlRepository.deleteById(urlId);
    }

    /**
     * URL 통계 확인
     *
     * @param user  사용자 정보
     * @param urlId 확인하려는 URL의 ID
     * @return URLDetailResponse
     */
    @Override
    public URLDetailResponse detailUrl(User user, Long urlId) {
        this.checkUser(user);

        URL url = findUrlOrThrowException(urlId);

        this.validateUrlOwnership(user, url);

        List<ClickEvent> allClickEvent = clickEventRepository.findAllByUrl(url);

        return new URLDetailResponse(String.valueOf(url.getId()), url.getOriginURL(),
            url.getShortenedURL(), url.getQrCode(), url.getCreatedAt(), allClickEvent);
    }

    /**
     * QR코드 생성
     *
     * @param url QR 코드로 변환할 URL (단축 URL 또는 전체 URL)
     * @return QR코드 Base64
     */
    private String generateQRCode(String url) {
        String qrCodeBase64 = "";

        try  // QR코드 이미지 생성
        {
            byte[] qrImage = QRCodeUtil.generateQRCodeImage(url, 200, 200);
            qrCodeBase64 = Base64.getEncoder().encodeToString(qrImage);
        } catch (Exception e) {
            log.error("QR 코드 생성 중 오류 발생: {}", url, e);
            throw new URLException("QR 코드 생성 중 오류가 발생하였습니다.");
        }
        return qrCodeBase64;
    }

    /**
     * Repository에 URL을 검색하고 없을시 Throw
     *
     * @param urlId 찾고자 하는 URL의 ID
     * @return URL
     */
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
