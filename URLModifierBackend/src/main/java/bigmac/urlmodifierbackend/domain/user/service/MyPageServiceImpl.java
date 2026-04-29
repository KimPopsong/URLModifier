package bigmac.urlmodifierbackend.domain.user.service;

import bigmac.urlmodifierbackend.domain.url.dto.response.URLResponse;
import bigmac.urlmodifierbackend.domain.url.model.URL;
import bigmac.urlmodifierbackend.domain.url.repository.ClickEventRepository;
import bigmac.urlmodifierbackend.domain.url.repository.URLRepository;
import bigmac.urlmodifierbackend.domain.user.dto.response.MyPageResponse;
import bigmac.urlmodifierbackend.domain.user.model.User;
import bigmac.urlmodifierbackend.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class MyPageServiceImpl implements MyPageService {

    private final UserRepository userRepository;
    private final URLRepository urlRepository;
    private final ClickEventRepository clickEventRepository;

    @Value("${custom.BE_BASE_URL}")
    private String BE_BASE_URL;

    @Override
    public MyPageResponse getMyPage(User user) {
        this.checkUser(user);

        MyPageResponse myPageResponse = new MyPageResponse();
        myPageResponse.setEmail(user.getEmail());
        myPageResponse.setNickname(user.getNickName());
        myPageResponse.setUrls(urlRepository.findByUser(user).stream()
            .map(url -> buildURLResponse(url))
            .collect(Collectors.toList()));

        return myPageResponse;
    }

    private URLResponse buildURLResponse(URL url) {
        long clickCount = clickEventRepository.countByUrl(url);
        boolean expired = isExpired(url, clickCount);

        return URLResponse.builder()
            .id(String.valueOf(url.getId()))
            .originUrl(url.getOriginURL())
            .shortenedUrl((BE_BASE_URL + url.getShortenedURL()).replaceFirst("https?://", ""))
            .qrCode(url.getQrCode())
            .expiresAt(url.getExpiresAt())
            .maxClicks(url.getMaxClicks())
            .clickCount(clickCount)
            .expired(expired)
            .build();
    }

    private boolean isExpired(URL url, long clickCount) {
        if (url.getExpiresAt() != null && LocalDateTime.now().isAfter(url.getExpiresAt())) {
            return true;
        }
        if (url.getMaxClicks() != null && clickCount >= url.getMaxClicks()) {
            return true;
        }
        return false;
    }

    private void checkUser(User user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다");
        }

        userRepository.findById(user.getId()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 사용자입니다."));
    }
}
