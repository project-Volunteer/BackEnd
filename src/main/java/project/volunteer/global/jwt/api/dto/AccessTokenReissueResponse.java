package project.volunteer.global.jwt.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.global.common.response.BaseResponse;

@Getter @Setter
@NoArgsConstructor
public class AccessTokenReissueResponse extends BaseResponse{
	private String accessToken;

	public AccessTokenReissueResponse(String message, String accessToken) {
		super(message);
		this.accessToken = accessToken;
	}

}
