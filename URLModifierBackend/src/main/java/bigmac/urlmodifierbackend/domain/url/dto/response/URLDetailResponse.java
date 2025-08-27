package bigmac.urlmodifierbackend.domain.url.dto.response;

import bigmac.urlmodifierbackend.domain.url.model.ClickEvent;
import bigmac.urlmodifierbackend.domain.url.model.URL;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class URLDetailResponse {

    private Long id;
    private String originURL;
    private String shortenedURL;
    private String qrCode;
    private LocalDateTime createdAt;
    private List<ClickEvent> clickEventList;

    public void urlTourlDetail(URL url) {
        this.id = url.getId();
        this.originURL = url.getOriginURL();
        this.shortenedURL = url.getShortenedURL();
        this.qrCode = url.getQrCode();
        this.createdAt = url.getCreatedAt();
    }
}
