package bigmac.urlmodifierbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class URLInfoResponse {
    private boolean isValid;
    private String originUrl;
}