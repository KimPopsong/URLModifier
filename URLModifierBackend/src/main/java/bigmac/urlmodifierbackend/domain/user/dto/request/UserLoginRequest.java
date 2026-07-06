package bigmac.urlmodifierbackend.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginRequest {

    @NotBlank(message = "이메일을 입력해주세요.")
    String email;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    String password;
}
