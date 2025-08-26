package bigmac.urlmodifierbackend.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtResponse {

    private String accessToken;
    private String refreshToken;
}
