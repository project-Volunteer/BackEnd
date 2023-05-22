package project.volunteer.domain.user.application;

import project.volunteer.domain.user.api.dto.UserDashboardResponse;
import project.volunteer.domain.user.api.dto.response.UserJoinRequestListResponse;
import project.volunteer.domain.user.api.dto.response.UserRecruitingListResponse;

public interface UserService {
	public UserJoinRequestListResponse findUserJoinRequest();

	public UserRecruitingListResponse findUserRecruiting();

}
