package bigmac.urlmodifierbackend.domain.url.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class URLDetailResponse {

    private String id;
    private String originURL;
    private String shortenedURL;
    private String qrCode;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Integer maxClicks;
    private List<ClickEventResponse> clickEventList;
}
