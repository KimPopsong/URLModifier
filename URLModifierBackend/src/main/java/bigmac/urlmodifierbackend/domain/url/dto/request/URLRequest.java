package bigmac.urlmodifierbackend.domain.url.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class URLRequest {

    @NotNull
    private String url;
}
