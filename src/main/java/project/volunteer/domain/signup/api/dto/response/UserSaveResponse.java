package project.volunteer.domain.signup.api.dto.response;

import lombok.Data;
import project.volunteer.domain.signup.dto.UserSave;

@Data
public class UserSaveResponse {
    private UserSave userInfo;

	public UserSaveResponse(UserSave userInfo) {
		this.userInfo = userInfo;
	}
}
