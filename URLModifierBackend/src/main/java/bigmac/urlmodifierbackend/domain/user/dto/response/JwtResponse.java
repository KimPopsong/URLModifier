package bigmac.urlmodifierbackend.domain.user.dto.response;

import bigmac.urlmodifierbackend.domain.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse extends User {

    private String accessToken;
    private String refreshToken;
}
