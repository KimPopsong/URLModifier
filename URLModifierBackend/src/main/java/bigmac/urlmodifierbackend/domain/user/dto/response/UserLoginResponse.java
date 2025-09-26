package bigmac.urlmodifierbackend.domain.user.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserLoginResponse {

    private String userId;
    private String email;
    private JwtResponse jwtResponse;
}
