package bigmac.urlmodifierbackend.domain.user.service;

import bigmac.urlmodifierbackend.domain.url.dto.response.URLResponse;
import bigmac.urlmodifierbackend.domain.url.repository.ClickEventRepository;
import bigmac.urlmodifierbackend.domain.url.repository.URLRepository;
import bigmac.urlmodifierbackend.domain.user.dto.response.MyPageResponse;
import bigmac.urlmodifierbackend.domain.user.model.User;
import bigmac.urlmodifierbackend.domain.user.repository.UserRepository;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class MyPageServiceImpl implements MyPageService {

    private final UserRepository userRepository;
    private final URLRepository urlRepository;
    private final ClickEventRepository clickEventRepository;

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
            url -> new URLResponse(String.valueOf(url.getId()), url.getOriginURL(),
                url.getShortenedURL(), url.getQrCode())).collect(Collectors.toList()));

        return myPageResponse;
    }

    private void checkUser(User user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다");
        }

        userRepository.findById(user.getId()).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 사용자입니다."));
    }
}
