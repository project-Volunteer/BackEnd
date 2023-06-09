package project.volunteer.domain.user.application;

import org.springframework.web.multipart.MultipartFile;

import project.volunteer.domain.user.api.dto.response.UserAlarmResponse;
import project.volunteer.domain.user.api.dto.response.UserJoinRequestListResponse;
import project.volunteer.domain.user.api.dto.response.UserRecruitingListResponse;

public interface UserService {
	public void userAlarmUpdate(Long userNo, Boolean joinAlarm, Boolean noticeAlarm, Boolean beforeAlarm);

	public void userInfoUpdate(Long userNo, String nickname, String email, String picture);
}
