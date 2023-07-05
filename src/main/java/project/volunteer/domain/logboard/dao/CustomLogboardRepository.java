package project.volunteer.domain.logboard.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.query.Param;

import project.volunteer.domain.logboard.dao.dto.LogboardListQuery;

public interface CustomLogboardRepository {
	Slice<LogboardListQuery> findLogboardDtos(Pageable pageable, String searchType, Long writerNo, Long lastId);
	
	boolean existsLogboardByUserNoAndSchedulNo(@Param("userNo") Long userNo, @Param("scheduleNo") Long scheduleNo);
}
