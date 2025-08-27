package bigmac.urlmodifierbackend.domain.user.controller;

import bigmac.urlmodifierbackend.domain.user.dto.response.MyPageResponse;
import bigmac.urlmodifierbackend.domain.user.model.User;
import bigmac.urlmodifierbackend.domain.user.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/me")
public class MyPageController {

    private final MyPageService myPageService;

    /**
     * 마이페이지
     *
     * @param user 사용자 정보
     * @return MyPageResponse
     */
    @GetMapping("")
    public ResponseEntity<MyPageResponse> getMyPage(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(myPageService.getMyPage(user));
    }
}
