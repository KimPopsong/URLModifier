package bigmac.urlmodifierbackend.domain.url.service;

import bigmac.urlmodifierbackend.domain.url.exception.URLException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class URLValidateServiceImpl {

    private static final List<String> BLOCKED_DOMAINS = List.of("bit.ly", "tinyurl.com",
        "t.co");  // 단축 URL 도메인

    private static final String MY_DOMAIN = "short.ly";  // TODO

    /**
     * 단축 URL을 다시 단축하는지 확인
     *
     * @param url 단축하려는 URL
     */
    public void validateOriginalUrl(String url) {
        // 내 서비스 도메인 체크
        if (url.contains(MY_DOMAIN)) {
            throw new URLException("단축 URL은 재단축이 불가능합니다.");
        }

        // 외부 단축 서비스 체크
        for (String blocked : BLOCKED_DOMAINS) {
            if (url.contains(blocked)) {
                throw new URLException("타 사이트의 URL은 단축이 불가능합니다.");
            }
        }
    }
}
