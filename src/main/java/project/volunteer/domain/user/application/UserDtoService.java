package project.volunteer.domain.user.application;

import project.volunteer.domain.user.api.dto.response.*;

public interface UserDtoService {

	public UserJoinRequestListResponse findUserJoinRequest(Long userNo);

	public UserRecruitingListResponse findUserRecruiting(Long userNo);

	public UserAlarmResponse findUserAlarm(Long userNo);

	public UserInfo findUserInfo(Long loginUserNo);

	public HistoryTimeInfo findHistoryTimeInfo(Long loginUserNo);

	public ActivityInfo findActivityInfo(Long loginUserNo);
}
