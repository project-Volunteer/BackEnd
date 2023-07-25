package project.volunteer.domain.user.application;

import project.volunteer.domain.user.api.dto.response.*;

public interface UserService {
	public void userRefreshTokenUpdate(Long userNo,String refreshToken);

	public void userAlarmUpdate(Long userNo, Boolean joinAlarm, Boolean noticeAlarm, Boolean beforeAlarm);

	public void userInfoUpdate(Long userNo, String nickname, String email, String picture);

	public UserAlarmResponse findUserAlarm(Long userNo);

	public UserInfo findUserInfo(Long loginUserNo);
}
