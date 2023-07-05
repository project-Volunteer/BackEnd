package project.volunteer.domain.logboard.dao;


import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;
import static project.volunteer.domain.logboard.domain.QLogboard.logboard;
import static project.volunteer.domain.sehedule.domain.QSchedule.schedule;
import static project.volunteer.domain.user.domain.QUser.user;
import static project.volunteer.domain.like.domain.QLike.like;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import project.volunteer.domain.logboard.dao.dto.LogboardListQuery;
import project.volunteer.domain.logboard.dao.dto.QLogboardListQuery;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.global.common.component.LogboardSearchType;

@Repository
@RequiredArgsConstructor
public class CustomLogboardRepositoryImpl implements CustomLogboardRepository {

    private final JPAQueryFactory jpaQueryFactory;
    
    // TODO : 댓글 카운트 관련 쿼리 미완성
	@Override
	public Slice<LogboardListQuery> findLogboardDtos(Pageable pageable, String searchType, Long writerNo, Long lastId) {
		List<LogboardListQuery> results =
			jpaQueryFactory
			.select(
				new QLogboardListQuery(
					logboard.logboardNo, logboard.writer.userNo, logboard.writer.picture,
					logboard.writer.nickName, logboard.createdDate,
					schedule.recruitment.volunteeringCategory, logboard.content, 
					logboard.likeCount, like.likeOk.coalesce(false).as("isLikeMe"), 
					// 댓글 갯수 조회 쿼리 미완성 추후 추가
					logboard.viewCount
				)
			)
			.from(logboard)
			.innerJoin(schedule).on(logboard.schedule.eq(schedule))
			.innerJoin(user).on(logboard.writer.userNo.eq(user.userNo))
			.leftJoin(like).on(logboard.logboardNo.eq(like.likeNo))
			.where(
					ltlogboardNo(lastId),
					isSearchTypeMyLog(searchType, writerNo),
					logboard.isPublished.eq(Boolean.TRUE),
					logboard.isDeleted.eq(IsDeleted.N)
			)
			.limit(pageable.getPageSize() + 1)
			.orderBy(logboard.logboardNo.desc())
			.fetch();
		return checkEndPage(pageable, results);
	}
	

    // 내가 쓴 로그일 경우 조회
    private BooleanExpression isSearchTypeMyLog(String searchType, Long writerNo) {
         if (LogboardSearchType.isAll(searchType)) {
             return null;
         }
         return logboard.writer.userNo.eq(writerNo);
     }
	

    // no-offset 방식 처리하는 메서드
    private BooleanExpression ltlogboardNo(Long logboardNo) {
         if (logboardNo == null) {
             return null;
         }
         return logboard.logboardNo.lt(logboardNo);
     }
	
    // 무한 스크롤 방식 처리하는 메서드
    private Slice<LogboardListQuery> checkEndPage(Pageable pageable, List<LogboardListQuery> results) {
        boolean hasNext = false;

        if(results.size() > pageable.getPageSize()){ //다음 페이지가 존재하는 경우
        	results.remove(pageable.getPageSize()); //한개더 가져온 엔티티를 삭제
            hasNext = true;
        }
        return new SliceImpl<>(results, pageable, hasNext);
    }
    
	@Override
	public boolean existsLogboardByUserNoAndSchedulNo(Long userNo, Long scheduleNo) {
		Integer fetchOne = jpaQueryFactory
                .selectOne()
                .from(logboard)
                .where(
                		logboard.schedule.scheduleNo.eq(scheduleNo),
                		logboard.writer.userNo.eq(userNo))
                .fetchFirst();//limit 1

        return fetchOne != null;
	}
    
    
    
}
