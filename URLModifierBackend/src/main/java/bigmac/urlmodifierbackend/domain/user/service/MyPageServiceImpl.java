package bigmac.urlmodifierbackend.domain.user.service;

import bigmac.urlmodifierbackend.domain.url.dto.response.URLResponse;
import bigmac.urlmodifierbackend.domain.url.exception.URLException;
import bigmac.urlmodifierbackend.domain.url.model.URL;
import bigmac.urlmodifierbackend.domain.url.repository.URLRepository;
import bigmac.urlmodifierbackend.domain.user.dto.response.MyPageResponse;
import bigmac.urlmodifierbackend.domain.user.exception.URLFindException;
import bigmac.urlmodifierbackend.domain.user.model.User;
import bigmac.urlmodifierbackend.domain.user.repository.UserRepository;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class MyPageServiceImpl implements MyPageService {

    private final UserRepository userRepository;
    private final URLRepository urlRepository;

    /**
     * 마이페이지
     *
     * @param user 사용자 정보
     * @return MyPageResponse
     */
    @Override
    public MyPageResponse getMyPage(User user) {
        this.checkUser(user);

        MyPageResponse myPageResponse = new MyPageResponse();

        myPageResponse.setEmail(user.getEmail());
        myPageResponse.setNickname(user.getNickName());
        myPageResponse.setUrls(urlRepository.findByUser(user).stream().map(
            url -> new URLResponse(url.getId(), url.getOriginURL(), url.getShortenedURL(),
                url.getQrCode())).collect(Collectors.toList()));

        return myPageResponse;
    }

    /**
     * 단축 URL 제거
     *
     * @param user  사용자 정보
     * @param urlId 제거하려는 URL의 ID
     */
    @Transactional
    @Override
    public void deleteUrl(User user, Long urlId) {
        this.checkUser(user);

        Optional<URL> url = urlRepository.findById(urlId);

        if (url.isEmpty()) {
            throw new URLFindException("URL이 존재하지 않습니다.");
        } else if (!Objects.equals(url.get().getUser().getId(), user.getId())) {
            throw new URLException("본인의 URL이 아닙니다.");
        }

        urlRepository.deleteById(urlId);
    }

    private void checkUser(User user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다");
        }

        userRepository.findById(user.getId()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 사용자입니다."));
    }
}
