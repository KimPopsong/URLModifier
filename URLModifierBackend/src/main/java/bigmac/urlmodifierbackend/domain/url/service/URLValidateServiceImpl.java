package bigmac.urlmodifierbackend.domain.url.service;

import bigmac.urlmodifierbackend.domain.url.exception.URLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class URLValidateServiceImpl {

    private static final List<String> BLOCKED_DOMAINS = List.of("bit.ly", "tinyurl.com",
        "t.co");  // 단축 URL 도메인

    private final String myDomain;

    public URLValidateServiceImpl(@Value("${custom.BE_BASE_URL}") String beBaseUrl) {
        this.myDomain = extractHost(beBaseUrl);
    }

    /**
     * 원본 URL 검증: 형식(http/https), 자체 도메인 재단축, 외부 단축 서비스 여부 확인
     *
     * @param url 단축하려는 URL
     */
    public void validateOriginalUrl(String url) {
        String host;
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();

            if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase(
                "https")) || uri.getHost() == null) {
                throw new URLException("올바른 URL 형식이 아닙니다. (http/https URL만 가능합니다.)");
            }

            host = uri.getHost().toLowerCase(Locale.ROOT);
        } catch (URISyntaxException e) {
            throw new URLException("올바른 URL 형식이 아닙니다. (http/https URL만 가능합니다.)");
        }

        // 내 서비스 도메인 체크
        if (myDomain != null && matchesDomain(host, myDomain)) {
            throw new URLException("단축 URL은 재단축이 불가능합니다.");
        }

        // 외부 단축 서비스 체크
        for (String blocked : BLOCKED_DOMAINS) {
            if (matchesDomain(host, blocked)) {
                throw new URLException("타 사이트의 URL은 단축이 불가능합니다.");
            }
        }
    }

    /**
     * host가 domain과 같거나 그 서브도메인인지 확인
     */
    private boolean matchesDomain(String host, String domain) {
        return host.equals(domain) || host.endsWith("." + domain);
    }

    private static String extractHost(String baseUrl) {
        try {
            String host = new URI(baseUrl).getHost();
            return host == null ? null : host.toLowerCase(Locale.ROOT);
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
