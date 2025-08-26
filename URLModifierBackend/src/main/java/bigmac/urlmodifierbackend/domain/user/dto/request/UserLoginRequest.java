package bigmac.urlmodifierbackend.domain.user.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginRequest {

    String email;
    String password;
}
