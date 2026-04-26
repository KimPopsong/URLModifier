package bigmac.urlmodifierbackend.domain.url.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class URLCacheDto implements Serializable {

    private Long id;
    private String originURL;
    private String shortenedURL;
    private LocalDateTime expiresAt;
    private Integer maxClicks;
}
