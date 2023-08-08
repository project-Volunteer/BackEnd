package project.volunteer.domain.user.application;

import project.volunteer.domain.user.api.dto.response.*;

public interface UserDtoService {

	public UserJoinRequestListResponse findUserJoinRequest(Long userNo);

	public UserRecruitingListResponse findUserRecruiting(Long userNo);

	public HistoryTimeInfo findHistoryTimeInfo(Long loginUserNo);

	public ActivityInfo findActivityInfo(Long loginUserNo);

	public JoinScheduleListResponse findUserSchedule(Long loginUserNo);

	public JoinRecruitmentListResponse findUserRecruitment(Long loginUserNo);

	public RecruitmentTempListResponse findRecruitmentTempDtos(Long loginUserNo);

	public LogboardTempListResponse findLoboardTempDtos(Long loginUserNo);
}
