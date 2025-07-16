package bigmac.urlmodifierbackend.service;

import bigmac.urlmodifierbackend.dto.UserRegisterRequest;
import bigmac.urlmodifierbackend.model.User;

public interface UserService {
    Boolean isEmailExist(String email);

    User registerUser(UserRegisterRequest userRegisterRequest);
}
