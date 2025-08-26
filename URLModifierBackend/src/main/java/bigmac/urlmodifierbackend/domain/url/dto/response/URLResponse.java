package bigmac.urlmodifierbackend.domain.url.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class URLResponse {

    private Long id;
    private String originUrl;
    private String shortenedUrl;
    private String qrCode;
}
