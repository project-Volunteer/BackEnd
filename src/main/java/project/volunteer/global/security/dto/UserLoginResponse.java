package project.volunteer.global.security.dto;

import lombok.Data;
import project.volunteer.global.common.response.BaseResponse;

@Data
public class UserLoginResponse{
    private UserLoginInfo userInfo;
    private String accessToken;

	public UserLoginResponse(UserLoginInfo userInfo, String accessToken) {
		this.userInfo = userInfo;
		this.accessToken = accessToken;
	}
}
