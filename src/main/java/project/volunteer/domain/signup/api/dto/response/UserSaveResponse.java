package project.volunteer.domain.signup.api.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.global.common.dto.BaseResponse;

@Getter 
@Setter
@NoArgsConstructor
public class UserSaveResponse extends BaseResponse{
	private Long userNo;

	public UserSaveResponse(String message, Long userNo) {
		super(message);
		this.userNo = userNo;
	}

}
