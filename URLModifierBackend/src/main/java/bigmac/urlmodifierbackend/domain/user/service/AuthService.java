package bigmac.urlmodifierbackend.domain.user.service;

import bigmac.urlmodifierbackend.domain.user.dto.request.UserLoginRequest;
import bigmac.urlmodifierbackend.domain.user.dto.request.UserRegisterRequest;
import bigmac.urlmodifierbackend.domain.user.dto.response.UserLoginResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    Boolean isEmailExist(String email);

    void registerUser(UserRegisterRequest userRegisterRequest);

    UserLoginResponse loginUser(UserLoginRequest userLoginRequest);

    UserLoginResponse refreshToken(HttpServletRequest request);

    void logoutUser(String authorizationHeader);
}
