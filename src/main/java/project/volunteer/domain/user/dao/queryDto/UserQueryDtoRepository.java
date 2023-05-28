package project.volunteer.domain.user.dao.queryDto;

import java.util.List;

//import project.volunteer.domain.user.api.dto.HistoryTimeInfo;
import project.volunteer.domain.user.dao.queryDto.dto.UserRecruitingQuery;
import project.volunteer.domain.user.dao.queryDto.dto.UserRecruitmentJoinRequestQuery;

public interface UserQueryDtoRepository {
	List<UserRecruitmentJoinRequestQuery> findUserRecruitmentJoinRequestDto(Long userNo);

	List<UserRecruitingQuery> findUserRecruitingDto(Long userNo);
}
