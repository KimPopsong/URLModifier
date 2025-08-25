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

    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(@RequestBody UserRegisterRequest userRegisterRequest) {
        userService.registerUser(userRegisterRequest);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> loginUser(
        @RequestBody UserLoginRequest userLoginRequest) {
        User user = userService.loginUser(userLoginRequest);

        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        return ResponseEntity.ok(new UserLoginResponse(accessToken, refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logoutUser(
        @RequestHeader("Authorization") String authorizationHeader) {
        userService.logoutUser(authorizationHeader);

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
