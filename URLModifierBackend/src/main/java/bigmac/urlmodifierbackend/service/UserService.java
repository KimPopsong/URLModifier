package bigmac.urlmodifierbackend.service;

import bigmac.urlmodifierbackend.dto.UserLoginRequest;
import bigmac.urlmodifierbackend.dto.UserRegisterRequest;
import bigmac.urlmodifierbackend.model.User;

public interface UserService {
    Boolean isEmailExist(String email);

    void registerUser(UserRegisterRequest userRegisterRequest);

    User loginUser(UserLoginRequest userLoginRequest);
}
