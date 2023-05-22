package project.volunteer.domain.user.api.dto.response;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.domain.user.dao.queryDto.dto.UserRecruitingQuery;

@Getter
@Setter
@NoArgsConstructor
public class UserRecruitingListResponse {
	List<UserRecruitingQuery> recruitingList;

	public UserRecruitingListResponse(List<UserRecruitingQuery> recruitingList) {
		this.recruitingList = recruitingList;
	}
}
