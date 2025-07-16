package bigmac.urlmodifierbackend.service;

import bigmac.urlmodifierbackend.model.URL;
import bigmac.urlmodifierbackend.model.User;
import bigmac.urlmodifierbackend.repository.URLRepository;
import bigmac.urlmodifierbackend.util.Base62;
import bigmac.urlmodifierbackend.util.QRCodeUtil;
import bigmac.urlmodifierbackend.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class URLServiceImpl implements URLService {
    private final URLRepository urlRepository;
    private final SnowflakeIdGenerator idGenerator;
    @Value("${server.base-url}")
    private String baseUrl;

    /**
     * 주어진 URL이 DB에 있는지 확인 후 있다면 DB에 있는 값 반환. 없다면 단축 후 DB 저장 및 반환
     *
     * @param originURL 기본 URL
     * @return 단축된 URL
     */
    @Override
    public URL makeURLShort(String originURL, Long userId)  // TODO user 저장
    {
        Optional<URL> findByOriginURL = urlRepository.findByOriginURL(originURL);

        if (findByOriginURL.isPresent())  // 이미 같은 URL이 DB에 있다면 계산하지 말고 반환
        {
            return findByOriginURL.get();
        }

        else  // 단축 URL 생성
        {
            User user = null;  // 사용자 찾아야함
            long id = idGenerator.nextId();

            String shortenedURL = Base62.encode(id);

            String qrCodeBase64 = "";

            try  // QR코드 이미지 생성
            {
                byte[] qrImage = QRCodeUtil.generateQRCodeImage(originURL, 200, 200);
                qrCodeBase64 = Base64.getEncoder().encodeToString(qrImage);
            }

            catch (Exception e)
            {
                e.printStackTrace();
            }

            URL newUrl = new URL(id, user, originURL, shortenedURL, qrCodeBase64);

            urlRepository.save(newUrl);

            return newUrl;
        }
    }

    @Override
    public Optional<URL> getOriginURL(String shortUrl)
    {
        try
        {  // TODO find 로직 수정 -> Base62가 아닌 다른 것으로
            long id = Base62.decode(shortUrl);

            return urlRepository.findById(id);
        }

        catch (Exception e)
        {
            e.printStackTrace();

            return Optional.empty();
        }
    }
}
