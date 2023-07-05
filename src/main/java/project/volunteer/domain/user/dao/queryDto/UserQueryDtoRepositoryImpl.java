package project.volunteer.domain.user.dao.queryDto;

import static project.volunteer.domain.storage.domain.QStorage.storage;

import java.util.List;

import static project.volunteer.domain.image.domain.QImage.image;
import static project.volunteer.domain.participation.domain.QParticipant.participant1;
import static project.volunteer.domain.recruitment.domain.QRecruitment.recruitment;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import project.volunteer.global.common.component.RealWorkCode;
//import project.volunteer.domain.user.api.dto.HistoryTimeInfo;
import project.volunteer.domain.user.dao.queryDto.dto.QUserRecruitingQuery;
import project.volunteer.domain.user.dao.queryDto.dto.QUserRecruitmentJoinRequestQuery;
import project.volunteer.domain.user.dao.queryDto.dto.UserRecruitingQuery;
import project.volunteer.domain.user.dao.queryDto.dto.UserRecruitmentJoinRequestQuery;
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
								recruitment.recruitmentNo, image.staticImageName, storage.imagePath,
								recruitment.VolunteeringTimeTable.startDay, recruitment.VolunteeringTimeTable.endDay,
								recruitment.title, recruitment.address.sido, recruitment.address.sigungu,
								recruitment.volunteeringCategory, recruitment.volunteeringType, recruitment.isIssued,
								recruitment.volunteerType))
				.from(recruitment)
				.innerJoin(participant1).on(participant1.recruitment.recruitmentNo.eq(recruitment.recruitmentNo))
				.innerJoin(image).on(recruitment.recruitmentNo.eq(image.no)) 
				.leftJoin(image.storage, storage).on(image.realWorkCode.eq(RealWorkCode.RECRUITMENT)) 
				.where(
						participant1.state.eq(ParticipantState.JOIN_REQUEST),
						participant1.participant.userNo.eq(userNo),
						recruitment.isPublished.eq(Boolean.TRUE),
						recruitment.isDeleted.eq(IsDeleted.N))
				.fetch();
	}
	
	
	
	@Override
	public List<UserRecruitingQuery> findUserRecruitingDto(Long userNo) {
		return jpaQueryFactory
				.select(new QUserRecruitingQuery(
							recruitment.recruitmentNo, image.staticImageName, storage.imagePath,
							recruitment.VolunteeringTimeTable.startDay, recruitment.VolunteeringTimeTable.endDay,
							recruitment.title, recruitment.address.sido, recruitment.address.sigungu,
							recruitment.volunteeringCategory, recruitment.volunteeringType, recruitment.isIssued,
							recruitment.volunteerType, recruitment.volunteerNum, recruitment.writer,
							ExpressionUtils.as(
								JPAExpressions
									.select(participant1.count())
									.from(participant1)
									.where(
											participant1.recruitment.eq(recruitment)
											, participant1.state.eq(ParticipantState.JOIN_APPROVAL)
								),
								"currentVolunteerNum")
				))
				.from(recruitment)
				.innerJoin(image).on(recruitment.recruitmentNo.eq(image.no))
				.leftJoin(image.storage, storage).on(image.realWorkCode.eq(RealWorkCode.RECRUITMENT))
				.where(
						recruitment.writer.userNo.eq(userNo),
						recruitment.isPublished.eq(Boolean.TRUE),
						recruitment.isDeleted.eq(IsDeleted.N))
				.fetch();
	}

}
