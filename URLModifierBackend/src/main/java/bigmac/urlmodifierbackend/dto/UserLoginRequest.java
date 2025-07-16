package bigmac.urlmodifierbackend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginRequest {
    String email;
    String password;
}
