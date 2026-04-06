package bigmac.urlmodifierbackend.domain.url.dto.request;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class CustomURLRequest {

    String originURL;
    String customURL;
    LocalDateTime expiresAt;
    Integer maxClicks;
}
