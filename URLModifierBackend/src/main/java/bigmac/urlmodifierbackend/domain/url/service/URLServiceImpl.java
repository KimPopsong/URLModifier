package bigmac.urlmodifierbackend.domain.url.service;

import bigmac.urlmodifierbackend.domain.url.model.URL;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class URLServiceImpl implements URLService {

    private final URLRepository urlRepository;
    private final UserRepository userRepository;
    private final SnowflakeIdGenerator idGenerator;
    @Value("${server.base-url}")
    private String baseUrl;

    /**
     * 주어진 URL이 DB에 있는지 확인 후 있다면 DB에 있는 값 반환. 없다면 단축 후 DB 저장 및 반환
     *
     * @param originURL 기본 URL
     * @param user      사용자
     * @return 단축된 URL
     */
    @Transactional
    @Override
    public URL makeURLShort(String originURL, User user) {
        Optional<URL> findByOriginURL = urlRepository.findByOriginURL(originURL);

        if (findByOriginURL.isPresent())  // 이미 같은 URL이 DB에 있다면 계산하지 말고 반환
        {
            return findByOriginURL.get();
        } else  // 단축 URL 생성
        {
            long id = idGenerator.nextId();

            String shortenedURL = Base62.encode(id);

            String qrCodeBase64 = "";

            try  // QR코드 이미지 생성
            {
                byte[] qrImage = QRCodeUtil.generateQRCodeImage(originURL, 200, 200);
                qrCodeBase64 = Base64.getEncoder().encodeToString(qrImage);
            } catch (Exception e) {
                e.printStackTrace();
            }

            URL newUrl = new URL(id, user, originURL, shortenedURL, qrCodeBase64);
            
            urlRepository.save(newUrl);

            return newUrl;
        }
    }

    @Override
    public Optional<URL> getOriginURLByShortURL(String shortUrl) {
        return urlRepository.findByShortenedURL(shortUrl);
    }
}
