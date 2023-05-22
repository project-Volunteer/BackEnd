package project.volunteer.domain.user.api.dto.response;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.domain.user.dao.queryDto.dto.UserRecruitmentJoinRequestQuery;

@Getter
@Setter
@NoArgsConstructor
public class UserJoinRequestListResponse {
	List<UserRecruitmentJoinRequestQuery> requestList;

	public UserJoinRequestListResponse(List<UserRecruitmentJoinRequestQuery> requestList) {
		this.requestList = requestList;
	}
}
