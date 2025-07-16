package bigmac.urlmodifierbackend.controller;

import bigmac.urlmodifierbackend.dto.UserRegisterRequest;
import bigmac.urlmodifierbackend.model.User;
import bigmac.urlmodifierbackend.service.UserService;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<User> registerUser(@RequestBody UserRegisterRequest userRegisterRequest)
    {
        User user = userService.registerUser(userRegisterRequest);

        return ResponseEntity.ok(user);
    }
}
