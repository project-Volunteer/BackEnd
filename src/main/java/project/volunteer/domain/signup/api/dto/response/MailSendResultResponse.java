package project.volunteer.domain.signup.api.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.global.common.response.BaseResponse;

@Getter
@Setter
@NoArgsConstructor
public class MailSendResultResponse extends BaseResponse{
	private String authCode;

	public MailSendResultResponse(String message, String authCode) {
		super(message);
		this.authCode = authCode;
	}
}
