package bigmac.urlmodifierbackend.domain.url.controller;

import bigmac.urlmodifierbackend.domain.url.dto.request.CustomURLRequest;
import bigmac.urlmodifierbackend.domain.url.dto.request.URLRequest;
import bigmac.urlmodifierbackend.domain.url.dto.response.URLInfoResponse;
import bigmac.urlmodifierbackend.domain.url.dto.response.URLResponse;
import bigmac.urlmodifierbackend.domain.url.model.URL;
import bigmac.urlmodifierbackend.domain.url.service.URLService;
import bigmac.urlmodifierbackend.domain.user.model.User;
import java.net.URI;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * URL 단축 및 리디렉션 요청을 처리하는 컨트롤러
 */
@RestController
@RequiredArgsConstructor
public class URLController {

    private final URLService urlService;

    @Value("${custom.BE_BASE_URL}")
    private String BE_BASE_URL;
    @Value("${custom.FE_BASE_URL}")
    private String FE_BASE_URL;

    /**
     * 원본 URL을 받아 단축 URL과 QR 코드를 생성
     *
     * @param urlRequest 원본 URL
     * @return 생성된 단축 URL과 QR 코드
     */
    @PostMapping("/short-urls")
    public ResponseEntity<URLResponse> makeURLShort(@AuthenticationPrincipal User user,
        @RequestBody URLRequest urlRequest) {
        URL url = urlService.makeURLShort(user, urlRequest.getUrl());

        return ResponseEntity.created(URI.create(BE_BASE_URL + url.getShortenedURL())).body(
            new URLResponse(url.getId(), urlRequest.getUrl(), BE_BASE_URL + url.getShortenedURL(),
                url.getQrCode()));  // 생성 응답(201 CREATED)과 함께 생성된 단축 URL과 QR 코드를 반환
    }

    /**
     * 사용자 커스텀 URL 생성
     *
     * @param user             사용자 정보
     * @param customURLRequest 생성하려는 원본 URL과 커스텀 URL
     * @return 생성된 단축 URL과 QR 코드
     */
    @PostMapping("/short-urls/custom")
    public ResponseEntity<URLResponse> makeCustomURL(@AuthenticationPrincipal User user,
        @RequestBody CustomURLRequest customURLRequest) {
        URL url = urlService.makeCustomURL(user, customURLRequest);

        return ResponseEntity.created(URI.create(BE_BASE_URL + url.getShortenedURL())).body(
            new URLResponse(url.getId(), url.getOriginURL(), BE_BASE_URL + url.getShortenedURL(),
                url.getQrCode()));  // 생성 응답(201 CREATED)과 함께 생성된 단축 URL과 QR 코드를 반환
    }

    /**
     * 원본 URL 확인
     *
     * @param shortUrl 단축 URL
     * @return URL 정보가 존재하는 경우 true와 원본 URL, 아닌 경우 false와 null 반환
     */
    @GetMapping("/short-urls/{shortUrl}")
    public ResponseEntity<URLInfoResponse> isValidURL(@PathVariable String shortUrl) {
        Optional<URL> url = urlService.getOriginURLByShortURL(shortUrl);

        if (url.isEmpty())  // URL 정보가 존재하지 않는 경우
        {
            return ResponseEntity.ok(new URLInfoResponse(false, null));
        } else  // URL 정보가 존재하는 경우
        {
            return ResponseEntity.ok(new URLInfoResponse(true, url.get().getOriginURL()));
        }
    }

    /**
     * 단축 URL을 받아 원본 URL로 리디렉션
     *
     * @param shortUrl 경로 변수로 받은 단축 URL 문자열
     * @return 원본 URL로 리디렉션하는 응답
     */
    @GetMapping("/{shortUrl}")
    public ResponseEntity<Void> redirectToOriginal(@PathVariable String shortUrl) {
        Optional<URL> url = urlService.getOriginURLByShortURL(shortUrl);

        if (url.isEmpty())  // URL 정보가 존재하지 않는 경우
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "URL not found");  // 404 Not Found 에러 발생
        } else  // URL 정보가 존재하는 경우
        {
            return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", url.get().getOriginURL())
                .build();  // HTTP 상태 코드 302 (Found)와 함께 Location 헤더에 원본 URL을 담아 리디렉션 응답
        }
    }
}
