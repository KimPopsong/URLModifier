package bigmac.urlmodifierbackend.domain.user.controller;

import bigmac.urlmodifierbackend.domain.url.dto.response.URLDetailResponse;
import bigmac.urlmodifierbackend.domain.user.dto.response.MyPageResponse;
import bigmac.urlmodifierbackend.domain.user.model.User;
import bigmac.urlmodifierbackend.domain.user.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    /**
     * 단축 URL 제거
     *
     * @param user  사용자 정보
     * @param urlId 제거하려는 URL의 ID
     * @return HttpStatus.ACCEPTED
     */
    @DeleteMapping("/urls/{urlId}")
    public ResponseEntity<Void> deleteUrl(@AuthenticationPrincipal User user,
        @PathVariable Long urlId) {
        myPageService.deleteUrl(user, urlId);

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    /**
     * URL 통계 확인
     *
     * @param user  사용자 정보
     * @param urlId 확인하려는 URL의 ID
     * @return URLDetailResponse
     */
    @GetMapping("/urls/{urlId}")
    public ResponseEntity<URLDetailResponse> detailUrl(@AuthenticationPrincipal User user,
        @PathVariable Long urlId) {
        return ResponseEntity.ok(myPageService.detailUrl(user, urlId));
    }
}
