package bigmac.urlmodifierbackend.domain.url.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class CustomURLRequest {

    @NotBlank(message = "원본 URL을 입력해주세요.")
    String originURL;

    @NotBlank(message = "커스텀 URL을 입력해주세요.")
    @Size(min = 1, max = 30, message = "커스텀 URL은 1자 이상 30자 이하여야 합니다.")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "커스텀 URL은 영문, 숫자, '-', '_'만 사용할 수 있습니다.")
    String customURL;

    LocalDateTime expiresAt;

    @Positive(message = "최대 클릭 수는 1 이상이어야 합니다.")
    Integer maxClicks;
}
