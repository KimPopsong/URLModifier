package bigmac.urlmodifierbackend.domain.url.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import bigmac.urlmodifierbackend.domain.url.exception.URLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class URLValidateServiceImplTest {

    private URLValidateServiceImpl validateService;

    @BeforeEach
    void setUp() {
        validateService = new URLValidateServiceImpl("https://short.example.com/");
    }

    @Test
    @DisplayName("정상적인 http/https URL은 통과한다")
    void validUrlPasses() {
        assertThatCode(() -> validateService.validateOriginalUrl("https://www.google.com/search"))
            .doesNotThrowAnyException();
        assertThatCode(() -> validateService.validateOriginalUrl("http://example.com"))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("http/https가 아닌 스킴은 거부한다")
    void nonHttpSchemeRejected() {
        assertThatThrownBy(() -> validateService.validateOriginalUrl("javascript:alert(1)"))
            .isInstanceOf(URLException.class);
        assertThatThrownBy(() -> validateService.validateOriginalUrl("ftp://example.com/file"))
            .isInstanceOf(URLException.class);
    }

    @Test
    @DisplayName("URL 형식이 아니면 거부한다")
    void malformedUrlRejected() {
        assertThatThrownBy(() -> validateService.validateOriginalUrl("not a url"))
            .isInstanceOf(URLException.class);
        assertThatThrownBy(() -> validateService.validateOriginalUrl("example.com"))
            .isInstanceOf(URLException.class);
    }

    @Test
    @DisplayName("자체 서비스 도메인은 재단축이 불가능하다")
    void ownDomainRejected() {
        assertThatThrownBy(
            () -> validateService.validateOriginalUrl("https://short.example.com/abc"))
            .isInstanceOf(URLException.class);
    }

    @Test
    @DisplayName("외부 단축 서비스 도메인은 거부한다")
    void externalShortenerRejected() {
        assertThatThrownBy(() -> validateService.validateOriginalUrl("https://bit.ly/abc"))
            .isInstanceOf(URLException.class);
        assertThatThrownBy(() -> validateService.validateOriginalUrl("https://sub.bit.ly/abc"))
            .isInstanceOf(URLException.class);
    }

    @Test
    @DisplayName("차단 도메인 문자열이 경로에 포함돼도 호스트가 다르면 통과한다")
    void pathContainingBlockedDomainPasses() {
        assertThatCode(
            () -> validateService.validateOriginalUrl("https://example.com/article-about-bit.ly"))
            .doesNotThrowAnyException();
    }
}
