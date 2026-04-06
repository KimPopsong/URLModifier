package bigmac.urlmodifierbackend.domain.user.service;

import bigmac.urlmodifierbackend.domain.user.dto.request.UserLoginRequest;
import bigmac.urlmodifierbackend.domain.user.dto.request.UserRegisterRequest;
import bigmac.urlmodifierbackend.domain.user.dto.request.UserWithdrawRequest;
import bigmac.urlmodifierbackend.domain.user.dto.response.JwtResponse;
import bigmac.urlmodifierbackend.domain.user.dto.response.UserLoginResponse;

public interface AuthService {

    Boolean isEmailExist(String email);

    void registerUser(UserRegisterRequest userRegisterRequest);

    UserLoginResponse loginUser(UserLoginRequest userLoginRequest);

    JwtResponse refreshToken(String refreshToken);

    void logoutUser(String authorizationHeader);

    void withdrawUser(String authorizationHeader, UserWithdrawRequest userWithdrawRequest);
}
