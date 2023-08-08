package project.volunteer.domain.user.dao.queryDto;

import java.util.List;

//import project.volunteer.domain.user.api.dto.HistoryTimeInfo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import project.volunteer.domain.user.dao.queryDto.dto.UserHistoryQuery;
import project.volunteer.domain.user.dao.queryDto.dto.UserRecruitingQuery;
import project.volunteer.domain.user.dao.queryDto.dto.UserRecruitmentJoinRequestQuery;

public interface UserQueryDtoRepository {
	List<UserRecruitmentJoinRequestQuery> findUserRecruitmentJoinRequestDto(Long userNo);

	List<UserRecruitingQuery> findUserRecruitingDto(Long userNo);

	public Slice<UserHistoryQuery> findHistoryDtos(Long loginUserNo, Pageable pageable, Long lastId);

}
