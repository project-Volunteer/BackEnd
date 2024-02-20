package project.volunteer.domain.user.dao.queryDto;

import java.util.List;

import static project.volunteer.domain.image.domain.QImage.image;
import static project.volunteer.domain.image.domain.QStorage.storage;
import static project.volunteer.domain.recruitment.domain.QRecruitment.recruitment;
import static project.volunteer.domain.recruitmentParticipation.domain.QRecruitmentParticipation.recruitmentParticipation;
import static project.volunteer.domain.scheduleParticipation.domain.QScheduleParticipation.scheduleParticipation;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import project.volunteer.domain.recruitmentParticipation.domain.QRecruitmentParticipation;
import project.volunteer.domain.user.dao.queryDto.dto.*;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.global.common.component.ParticipantState;

@Repository
@RequiredArgsConstructor
public class UserQueryDtoRepositoryImpl implements UserQueryDtoRepository{
    private final JPAQueryFactory jpaQueryFactory;

	@Override
	public List<UserRecruitmentJoinRequestQuery> findUserRecruitmentJoinRequestDto(Long userNo) {
		return jpaQueryFactory
				.select(
						new QUserRecruitmentJoinRequestQuery(
								recruitment.recruitmentNo, storage.imagePath,
								recruitment.timetable.startDay, recruitment.timetable.endDay,
								recruitment.title, recruitment.address.sido, recruitment.address.sigungu,
								recruitment.volunteeringCategory, recruitment.volunteeringType, recruitment.isIssued,
								recruitment.volunteerType))
				.from(recruitment)
				.innerJoin(recruitmentParticipation).on(recruitmentParticipation.recruitment.recruitmentNo.eq(recruitment.recruitmentNo))
				.leftJoin(image).on(recruitment.recruitmentNo.eq(image.no))
				.leftJoin(image.storage, storage).on(image.realWorkCode.eq(RealWorkCode.RECRUITMENT)) 
				.where(
						recruitmentParticipation.state.eq(ParticipantState.JOIN_REQUEST),
						recruitmentParticipation.user.userNo.eq(userNo),
						recruitment.isPublished.eq(Boolean.TRUE),
						recruitment.isDeleted.eq(IsDeleted.N))
				.fetch();
	}
	
	@Override
	public List<UserRecruitingQuery> findUserRecruitingDto(Long userNo) {
		return jpaQueryFactory
				.select(new QUserRecruitingQuery(
							recruitment.recruitmentNo, storage.imagePath,
							recruitment.timetable.startDay, recruitment.timetable.endDay,
							recruitment.title, recruitment.address.sido, recruitment.address.sigungu,
							recruitment.volunteeringCategory, recruitment.volunteeringType, recruitment.isIssued,
							recruitment.volunteerType, recruitment.maxParticipationNum,
							ExpressionUtils.as(
								JPAExpressions
									.select(recruitmentParticipation.count())
									.from(recruitmentParticipation)
									.where(
											recruitmentParticipation.recruitment.eq(recruitment)
											, recruitmentParticipation.state.eq(ParticipantState.JOIN_APPROVAL)
								),
								"currentVolunteerNum")
				))
				.from(recruitment)
				.leftJoin(image).on(recruitment.recruitmentNo.eq(image.no))
				.leftJoin(image.storage, storage).on(image.realWorkCode.eq(RealWorkCode.RECRUITMENT))
				.where(
						recruitment.writer.userNo.eq(userNo),
						recruitment.isPublished.eq(Boolean.TRUE),
						recruitment.isDeleted.eq(IsDeleted.N))
				.fetch();
	}

	@Override
	public Slice<UserHistoryQuery> findHistoryDtos(Long loginUserNo, Pageable pageable, Long lastId) {
		List<UserHistoryQuery> results =
			jpaQueryFactory
				.select(new QUserHistoryQuery(
					scheduleParticipation.id, storage.imagePath,
					scheduleParticipation.schedule.scheduleTimeTable.endDay, scheduleParticipation.recruitmentParticipation.recruitment.title,
					scheduleParticipation.schedule.address.sido, scheduleParticipation.schedule.address.sigungu,
					scheduleParticipation.recruitmentParticipation.recruitment.volunteeringCategory, scheduleParticipation.recruitmentParticipation.recruitment.volunteeringType,
					scheduleParticipation.recruitmentParticipation.recruitment.isIssued, scheduleParticipation.recruitmentParticipation.recruitment.volunteerType,
					scheduleParticipation.schedule.scheduleTimeTable.progressTime
				))
				.from(scheduleParticipation)
				.leftJoin(image).on(scheduleParticipation.schedule.recruitment.recruitmentNo.eq(image.no))
				.leftJoin(image.storage, storage).on(image.realWorkCode.eq(RealWorkCode.RECRUITMENT))
				.where(
						ltscheduleParticipationNo(lastId)
						, scheduleParticipation.state.eq(ParticipantState.PARTICIPATION_COMPLETE_APPROVAL)
						, scheduleParticipation.recruitmentParticipation.user.userNo.eq(loginUserNo)
				)
				.limit(pageable.getPageSize() + 1)
				.orderBy(scheduleParticipation.id.desc())
				.fetch();
		return checkEndPage(pageable, results);
	}

	// no-offset 방식 처리하는 메서드
	private BooleanExpression ltscheduleParticipationNo(Long scheduleParticipationNo) {
		return (scheduleParticipationNo != null)
				?scheduleParticipation.id.lt(scheduleParticipationNo)
				:null;
	}

	// 무한 스크롤 방식 처리하는 메서드
	private Slice<UserHistoryQuery> checkEndPage(Pageable pageable, List<UserHistoryQuery> results) {
		boolean hasNext = false;

		if(results.size() > pageable.getPageSize()){ //다음 페이지가 존재하는 경우
			results.remove(pageable.getPageSize()); //한개더 가져온 엔티티를 삭제
			hasNext = true;
		}
		return new SliceImpl<>(results, pageable, hasNext);
	}
}
