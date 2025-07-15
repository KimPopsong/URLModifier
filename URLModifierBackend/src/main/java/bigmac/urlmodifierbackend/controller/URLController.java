package bigmac.urlmodifierbackend.controller;

import bigmac.urlmodifierbackend.dto.URLInfoResponse;
import bigmac.urlmodifierbackend.dto.URLRequest;
import bigmac.urlmodifierbackend.dto.URLResponse;
import bigmac.urlmodifierbackend.model.URL;
import bigmac.urlmodifierbackend.service.URLService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

/**
 * URL 단축 및 리디렉션 요청을 처리하는 컨트롤러
 */
@RestController
@RequiredArgsConstructor
public class URLController {

    private final URLService urlService;

    @Value("${server.base-url}")
    private String baseUrl;

    /**
     * 원본 URL을 받아 단축 URL과 QR 코드를 생성
     *
     * @param urlRequest 원본 URL을 담고 있는 요청 DTO
     * @return 생성된 단축 URL과 QR 코드를 담은 응답 DTO
     */
    @PostMapping("/short-urls")
    public ResponseEntity<URLResponse> makeURLShort(@RequestBody URLRequest urlRequest)
    {
        URL url = urlService.makeURLShort(urlRequest.getUrl(), null); // JWT 구현 전까지 userId는 null로 전달

        return ResponseEntity.ok(new URLResponse(baseUrl + url.getShortenedURL(), url.getQrCode()));  // 성공 응답(200 OK)과 함께 생성된 단축 URL과 QR 코드를 반환
    }

    @GetMapping("/short-urls/{shortUrl}")
    public ResponseEntity<URLInfoResponse> isValidURL(@PathVariable String shortUrl)
    {
        Optional<URL> url = urlService.getOriginURL(shortUrl);

        if (url.isEmpty())  // URL 정보가 존재하지 않는 경우
        {
            return ResponseEntity.ok(new URLInfoResponse(false, null));
        }

        else  // URL 정보가 존재하는 경우
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
    public ResponseEntity<Void> redirectToOriginal(@PathVariable String shortUrl)
    {
        Optional<URL> url = urlService.getOriginURL(shortUrl);

        if (url.isEmpty())  // URL 정보가 존재하지 않는 경우
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "URL not found");  // 404 Not Found 에러 발생
        }

        else  // URL 정보가 존재하는 경우
        {
            return ResponseEntity.status(HttpStatus.FOUND).header("Location", url.get().getOriginURL()).build();  // HTTP 상태 코드 302 (Found)와 함께 Location 헤더에 원본 URL을 담아 리디렉션 응답
        }
    }
}
