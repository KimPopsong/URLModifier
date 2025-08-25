package bigmac.urlmodifierbackend.domain.user.controller;

import bigmac.urlmodifierbackend.domain.user.dto.request.UserLoginRequest;
import bigmac.urlmodifierbackend.domain.user.dto.request.UserRegisterRequest;
import bigmac.urlmodifierbackend.domain.user.dto.response.UserLoginResponse;
import bigmac.urlmodifierbackend.domain.user.model.User;
import bigmac.urlmodifierbackend.domain.user.service.UserService;
import bigmac.urlmodifierbackend.global.util.JwtUtil;
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
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 회원가입
     *
     * @param userRegisterRequest 이메일과 비밀번호
     * @return HttpStatus.OK
     */
    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(@RequestBody UserRegisterRequest userRegisterRequest) {
        userService.registerUser(userRegisterRequest);

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
        User user = userService.loginUser(userLoginRequest);

        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        return ResponseEntity.ok(new UserLoginResponse(accessToken, refreshToken));
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
        userService.logoutUser(authorizationHeader);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // TODO SMTP 설정
}
