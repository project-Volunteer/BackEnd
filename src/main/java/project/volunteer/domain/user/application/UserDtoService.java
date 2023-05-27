package project.volunteer.domain.user.application;

import project.volunteer.domain.user.api.dto.response.UserAlarmResponse;
import project.volunteer.domain.user.api.dto.response.UserJoinRequestListResponse;
import project.volunteer.domain.user.api.dto.response.UserRecruitingListResponse;

public interface UserDtoService {

	public UserJoinRequestListResponse findUserJoinRequest(Long userNo);

	public UserRecruitingListResponse findUserRecruiting(Long userNo);

	public UserAlarmResponse findUserAlarm(Long userNo);
}
