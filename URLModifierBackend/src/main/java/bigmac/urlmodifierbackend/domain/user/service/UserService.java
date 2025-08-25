package bigmac.urlmodifierbackend.domain.user.service;

import bigmac.urlmodifierbackend.domain.user.dto.request.UserLoginRequest;
import bigmac.urlmodifierbackend.domain.user.dto.request.UserRegisterRequest;
import bigmac.urlmodifierbackend.domain.user.model.User;

public interface UserService {

    Boolean isEmailExist(String email);

    void registerUser(UserRegisterRequest userRegisterRequest);

    User loginUser(UserLoginRequest userLoginRequest);

    void logoutUser(String authorizationHeader);
}
