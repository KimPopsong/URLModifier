package bigmac.urlmodifierbackend.controller;

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

@RestController
@RequiredArgsConstructor
public class URLController {
    private final URLService urlService;
    @Value("${server.base-url}")
    private String baseUrl;

    @PostMapping("/short-urls")
    public ResponseEntity<URLResponse> makeURLShort(@RequestBody URLRequest urlRequest)
    {
        URL url = urlService.makeURLShort(urlRequest.getUrl());

        return ResponseEntity.ok(new URLResponse(baseUrl + url.getShortenedURL(), url.getQrCode()));
    }

    @GetMapping("/{shortUrl}")
    public ResponseEntity<Void> redirectToOriginal(@PathVariable String shortUrl)
    {
        Optional<URL> url = urlService.getOriginURL(shortUrl);

        if (url.isEmpty())  // URL이 존재하지 않는다면
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);  // 에러
        }

        else
        {
            return ResponseEntity.status(HttpStatus.FOUND).header("Location", url.get().getOriginURL()).build();  // 리디렉션
        }
    }
}