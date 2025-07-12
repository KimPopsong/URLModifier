package bigmac.urlmodifierbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class URLResponse {
    private String shortenedUrl;
    private String qrCode;
}
