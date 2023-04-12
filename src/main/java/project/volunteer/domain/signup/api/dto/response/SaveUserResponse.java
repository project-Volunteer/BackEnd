package project.volunteer.domain.signup.api.dto.response;

import lombok.Data;
import project.volunteer.domain.signup.dto.SaveUserInfoDTO;

@Data
public class SaveUserResponse {
    private SaveUserInfoDTO userInfo;

	public SaveUserResponse(SaveUserInfoDTO userInfo) {
		this.userInfo = userInfo;
	}
}
