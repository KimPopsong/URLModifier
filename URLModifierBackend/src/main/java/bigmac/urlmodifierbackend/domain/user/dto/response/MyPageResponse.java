package bigmac.urlmodifierbackend.domain.user.dto.response;

import bigmac.urlmodifierbackend.domain.url.dto.response.URLResponse;
import java.util.List;
import lombok.Data;

@Data
public class MyPageResponse {

    String email;
    String nickname;
    List<URLResponse> urls;
}
