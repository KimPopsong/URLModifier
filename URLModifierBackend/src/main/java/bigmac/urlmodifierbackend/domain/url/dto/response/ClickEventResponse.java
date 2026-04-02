package bigmac.urlmodifierbackend.domain.url.dto.response;

import bigmac.urlmodifierbackend.domain.url.model.ClickEvent;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClickEventResponse {

    private LocalDateTime clickedAt;

    public static ClickEventResponse from(ClickEvent clickEvent) {
        return new ClickEventResponse(clickEvent.getClickedAt());
    }
}
