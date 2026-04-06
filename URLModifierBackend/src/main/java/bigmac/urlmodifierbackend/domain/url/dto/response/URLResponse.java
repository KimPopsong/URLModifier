package bigmac.urlmodifierbackend.domain.url.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class URLResponse {

    private String id;
    private String originUrl;
    private String shortenedUrl;
    private String qrCode;
    private LocalDateTime expiresAt;
    private Integer maxClicks;
    private long clickCount;
    private boolean expired;
}
