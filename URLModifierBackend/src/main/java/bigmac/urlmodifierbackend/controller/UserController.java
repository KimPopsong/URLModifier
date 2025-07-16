package bigmac.urlmodifierbackend.controller;

import bigmac.urlmodifierbackend.dto.UserLoginRequest;
import bigmac.urlmodifierbackend.dto.UserRegisterRequest;
import bigmac.urlmodifierbackend.model.User;
import bigmac.urlmodifierbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PostMapping("/regist")
    public ResponseEntity<Void> registerUser(@RequestBody UserRegisterRequest userRegisterRequest)
    {
        userService.registerUser(userRegisterRequest);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<User> loginUser(@RequestBody UserLoginRequest userLoginRequest)
    {
        User user = userService.loginUser(userLoginRequest);

        // TODO JWT 발급해야함~~

        return ResponseEntity.ok(user);
    }
}
