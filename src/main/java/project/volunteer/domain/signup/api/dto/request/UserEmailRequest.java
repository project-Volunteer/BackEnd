package project.volunteer.domain.signup.api.dto.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEmailRequest {
	@NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "올바르지 않은 이메일 형식입니다.")
	private String email;
}
