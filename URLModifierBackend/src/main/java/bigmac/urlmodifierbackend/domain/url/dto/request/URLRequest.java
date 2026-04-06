package bigmac.urlmodifierbackend.domain.url.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class URLRequest {

    @NotNull
    private String url;
    private LocalDateTime expiresAt;
    private Integer maxClicks;
}
