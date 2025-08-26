package bigmac.urlmodifierbackend.domain.url.service;

import bigmac.urlmodifierbackend.domain.url.dto.request.CustomURLRequest;
import bigmac.urlmodifierbackend.domain.url.exception.URLException;
import bigmac.urlmodifierbackend.domain.url.model.ClickEvent;
import bigmac.urlmodifierbackend.domain.url.model.URL;
import bigmac.urlmodifierbackend.domain.url.repository.ClickEventRepository;
import bigmac.urlmodifierbackend.domain.url.repository.URLRepository;
import bigmac.urlmodifierbackend.domain.user.model.User;
import bigmac.urlmodifierbackend.domain.user.repository.UserRepository;
import bigmac.urlmodifierbackend.global.util.Base62;
import bigmac.urlmodifierbackend.global.util.QRCodeUtil;
import bigmac.urlmodifierbackend.global.util.SnowflakeIdGenerator;
import java.util.Base64;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class URLServiceImpl implements URLService {

    private final URLRepository urlRepository;
    private final ClickEventRepository clickEventRepository;
    private final UserRepository userRepository;
    private final SnowflakeIdGenerator idGenerator;

    @Value("${custom.BE_BASE_URL}")
    private String BE_BASE_URL;
    @Value("${custom.FE_BASE_URL}")
    private String FE_BASE_URL;

    /**
     * 주어진 URL이 DB에 있는지 확인 후 있다면 DB에 있는 값 반환. 없다면 단축 후 DB 저장 및 반환
     *
     * @param originURL 기본 URL
     * @param user      사용자
     * @return 단축된 URL
     */
    @Transactional
    @Override
    public URL makeURLShort(User user, String originURL) {
        this.checkUser(user);

        Optional<URL> findByOriginURL = urlRepository.findByOriginURL(originURL);

        if (findByOriginURL.isPresent())  // 이미 같은 URL이 DB에 있다면 계산하지 말고 반환
        {
            return findByOriginURL.get();
        } else  // 단축 URL 생성
        {
            long id = idGenerator.nextId();
            String shortenedURL;

            // 겹치지 않는 URL 생성
            do {
                shortenedURL = Base62.encode(id);
            } while (urlRepository.findByShortenedURL(shortenedURL).isPresent());

            String qrCodeBase64 = "";

            try  // QR코드 이미지 생성
            {
                byte[] qrImage = QRCodeUtil.generateQRCodeImage(originURL, 200, 200);
                qrCodeBase64 = Base64.getEncoder().encodeToString(qrImage);
            } catch (Exception e) {
                e.printStackTrace();
                throw new URLException("단축 URL 생성간 오류가 발생하였습니다.");
            }

            URL newUrl = new URL(id, user, originURL, shortenedURL, qrCodeBase64);

            return urlRepository.save(newUrl);
        }
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

        Optional<URL> shortenedURL = urlRepository.findByShortenedURL(
            customURLRequest.getCustomURL());

        if (shortenedURL.isPresent()) {  // 이미 생성된 url이 있다면
            throw new URLException("이미 존재하는 단축 URL입니다.");
        } else if (customURLRequest.getCustomURL().length() >= 31
            || customURLRequest.getCustomURL().length() <= 5) {  // 글자 길이 제한
            throw new URLException("단축 URL의 길이가 올바르지 않습니다.");
        } else {  // 생성된 customURL이 없다면
            String qrCodeBase64 = "";

            try  // QR코드 이미지 생성
            {
                byte[] qrImage = QRCodeUtil.generateQRCodeImage(customURLRequest.getOriginURL(),
                    200, 200);
                qrCodeBase64 = Base64.getEncoder().encodeToString(qrImage);
            } catch (Exception e) {
                e.printStackTrace();
                throw new URLException("단축 URL 생성간 오류가 발생하였습니다.");
            }

            URL newUrl = new URL(idGenerator.nextId(), user, customURLRequest.getOriginURL(),
                customURLRequest.getCustomURL(), qrCodeBase64);

            return urlRepository.save(newUrl);
        }
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

    private void checkUser(User user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다");
        }

        userRepository.findById(user.getId()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 사용자입니다."));
    }
}
