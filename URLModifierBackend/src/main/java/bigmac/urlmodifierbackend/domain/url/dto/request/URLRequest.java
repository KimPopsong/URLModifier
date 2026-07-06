package bigmac.urlmodifierbackend.domain.url.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class URLRequest {

    @NotBlank(message = "URL을 입력해주세요.")
    private String url;
    private LocalDateTime expiresAt;

    @Positive(message = "최대 클릭 수는 1 이상이어야 합니다.")
    private Integer maxClicks;
}
