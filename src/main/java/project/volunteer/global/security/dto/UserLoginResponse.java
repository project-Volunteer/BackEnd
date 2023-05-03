package project.volunteer.global.security.dto;

import lombok.Data;
import project.volunteer.global.common.response.BaseResponse;

@Data
public class UserLoginResponse extends BaseResponse{
    private UserLoginInfo userInfo;
    private String accessToken;

	public UserLoginResponse(String message, UserLoginInfo userInfo, String accessToken) {
		super(message);
		this.userInfo = userInfo;
		this.accessToken = accessToken;
	}
}
