package bigmac.urlmodifierbackend.domain.url.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class URLResponse {

    private String id;
    private String originUrl;
    private String shortenedUrl;
    private String qrCode;
}
