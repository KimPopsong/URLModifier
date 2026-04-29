package bigmac.urlmodifierbackend.domain.url.controller;

import bigmac.urlmodifierbackend.domain.url.dto.request.CustomURLRequest;
import bigmac.urlmodifierbackend.domain.url.dto.request.URLRequest;
import bigmac.urlmodifierbackend.domain.url.dto.response.URLDetailResponse;
import bigmac.urlmodifierbackend.domain.url.dto.response.URLInfoResponse;
import bigmac.urlmodifierbackend.domain.url.dto.response.URLResponse;
import bigmac.urlmodifierbackend.domain.url.exception.URLExpiredException;
import bigmac.urlmodifierbackend.domain.url.model.URL;
import bigmac.urlmodifierbackend.domain.url.service.URLService;
import bigmac.urlmodifierbackend.domain.user.model.User;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
public class URLController {

    private final URLService urlService;

    @Value("${custom.BE_BASE_URL}")
    private String BE_BASE_URL;
    @Value("${custom.FE_BASE_URL}")
    private String FE_BASE_URL;

    @PostMapping("/short-urls")
    public ResponseEntity<URLResponse> makeURLShort(@AuthenticationPrincipal @Nullable User user,
        @RequestBody URLRequest urlRequest) {
        URL url = urlService.makeURLShort(user, urlRequest);

        String fullUrl = BE_BASE_URL + url.getShortenedURL();
        URLResponse response = URLResponse.builder()
            .id(String.valueOf(url.getId()))
            .originUrl(urlRequest.getUrl())
            .shortenedUrl(fullUrl.replaceFirst("https?://", ""))
            .qrCode(url.getQrCode())
            .expiresAt(url.getExpiresAt())
            .maxClicks(url.getMaxClicks())
            .clickCount(0L)
            .expired(false)
            .build();

        return ResponseEntity.created(URI.create(fullUrl)).body(response);
    }

    @PostMapping("/short-urls/custom")
    public ResponseEntity<URLResponse> makeCustomURL(@AuthenticationPrincipal User user,
        @RequestBody CustomURLRequest customURLRequest) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "커스텀 URL을 사용하려면 로그인이 필요합니다.");
        }

        URL url = urlService.makeCustomURL(user, customURLRequest);

        String fullUrl = BE_BASE_URL + url.getShortenedURL();
        URLResponse response = URLResponse.builder()
            .id(String.valueOf(url.getId()))
            .originUrl(url.getOriginURL())
            .shortenedUrl(fullUrl.replaceFirst("https?://", ""))
            .qrCode(url.getQrCode())
            .expiresAt(url.getExpiresAt())
            .maxClicks(url.getMaxClicks())
            .clickCount(0L)
            .expired(false)
            .build();

        return ResponseEntity.created(URI.create(fullUrl)).body(response);
    }

    @GetMapping("/short-urls/{shortUrl}")
    public ResponseEntity<URLInfoResponse> isValidURL(@PathVariable String shortUrl) {
        Optional<URL> url = urlService.getOriginURLByShortURL(shortUrl);

        if (url.isEmpty()) {
            return ResponseEntity.ok(new URLInfoResponse(false, null));
        } else {
            return ResponseEntity.ok(new URLInfoResponse(true, url.get().getOriginURL()));
        }
    }

    @GetMapping("/{shortenedUrl}")
    public ResponseEntity<Void> redirectToOriginal(@PathVariable String shortenedUrl,
        HttpServletRequest request) {
        String referrer = request.getHeader("referer");
        String userAgent = request.getHeader("User-Agent");
        String ipAddress = request.getRemoteAddr();

        try {
            URL url = urlService.redirectToOriginal(referrer, userAgent, ipAddress, shortenedUrl);
            return ResponseEntity.status(HttpStatus.FOUND).header("Location", url.getOriginURL()).build();
        } catch (URLExpiredException e) {
            return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", FE_BASE_URL + "?expired=" + shortenedUrl)
                .build();
        }
    }

    @DeleteMapping("/urls/{urlId}")
    public ResponseEntity<Void> deleteUrl(@AuthenticationPrincipal User user,
        @PathVariable Long urlId) {
        urlService.deleteUrl(user, urlId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @GetMapping("/urls/{urlId}")
    public ResponseEntity<URLDetailResponse> detailUrl(@AuthenticationPrincipal User user,
        @PathVariable Long urlId) {
        return ResponseEntity.ok(urlService.detailUrl(user, urlId));
    }
}
