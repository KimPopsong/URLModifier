package bigmac.urlmodifierbackend.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserLoginResponse {

    private String accessToken;
    private String refreshToken;
}
