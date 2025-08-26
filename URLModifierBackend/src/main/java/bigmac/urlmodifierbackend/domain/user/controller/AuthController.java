package bigmac.urlmodifierbackend.domain.user.controller;

import bigmac.urlmodifierbackend.domain.user.dto.request.UserLoginRequest;
import bigmac.urlmodifierbackend.domain.user.dto.request.UserRegisterRequest;
import bigmac.urlmodifierbackend.domain.user.dto.response.UserLoginResponse;
import bigmac.urlmodifierbackend.domain.user.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입
     *
     * @param userRegisterRequest 이메일과 비밀번호
     * @return HttpStatus.OK
     */
    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(@RequestBody UserRegisterRequest userRegisterRequest) {
        authService.registerUser(userRegisterRequest);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 로그인
     *
     * @param userLoginRequest 이메일과 비밀번호
     * @return accessToken, refreshToken
     */
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> loginUser(
        @RequestBody UserLoginRequest userLoginRequest) {
        return ResponseEntity.ok(authService.loginUser(userLoginRequest));
    }

    /**
     * 리프레시 토큰으로 JWT 재발급
     *
     * @param request HttpOnly 쿠키
     * @return 새로운 accessToken, refreshToken 발급
     */
    @PostMapping("/refresh")
    public ResponseEntity<UserLoginResponse> refreshToken(HttpServletRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    /**
     * 로그아웃
     *
     * @param authorizationHeader JWT
     * @return HttpStatus.OK
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logoutUser(
        @RequestHeader("Authorization") String authorizationHeader) {
        authService.logoutUser(authorizationHeader);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // TODO SMTP 설정
}
