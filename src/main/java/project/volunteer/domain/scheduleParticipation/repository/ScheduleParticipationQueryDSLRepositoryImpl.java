package project.volunteer.domain.scheduleParticipation.repository;

import static project.volunteer.domain.image.domain.QImage.image;
import static project.volunteer.domain.image.domain.QStorage.storage;
import static project.volunteer.domain.recruitmentParticipation.domain.QRecruitmentParticipation.recruitmentParticipation;
import static project.volunteer.domain.scheduleParticipation.domain.QScheduleParticipation.scheduleParticipation;
import static project.volunteer.domain.sehedule.domain.QSchedule.schedule;
import static project.volunteer.domain.user.domain.QUser.user;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import javax.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import project.volunteer.domain.scheduleParticipation.repository.dto.ScheduleParticipationDetail;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.common.component.RealWorkCode;

@Repository
@RequiredArgsConstructor
public class ScheduleParticipationQueryDSLRepositoryImpl implements ScheduleParticipationQueryDSLRepository {
    private final JPAQueryFactory factory;
    private final EntityManager em;

    @Override
    public void unApprovedCompleteOfAllFinishedScheduleParticipant(final LocalDate currentDate) {
        factory.update(scheduleParticipation)
                .set(scheduleParticipation.state, ParticipantState.PARTICIPATION_COMPLETE_UNAPPROVED)
                .where(scheduleParticipation.state.eq(ParticipantState.PARTICIPATING),
                        scheduleParticipation.schedule.scheduleNo.in(
                                JPAExpressions.select(schedule.scheduleNo)
                                        .from(schedule)
                                        .where(schedule.isDeleted.eq(IsDeleted.N),
                                                schedule.scheduleTimeTable.endDay.before(currentDate))
                        ))
                .execute();

        clear();
    }

    @Override
    public List<ScheduleParticipationDetail> findScheduleParticipationDetailBy(final Long scheduleNo,
                                                                               final List<ParticipantState> states) {
        return factory.select(
                        Projections.constructor(ScheduleParticipationDetail.class, scheduleParticipation.id, user.nickName,
                                user.email, storage.imagePath.coalesce(user.picture), scheduleParticipation.state))
                .from(scheduleParticipation)
                .join(scheduleParticipation.recruitmentParticipation, recruitmentParticipation)
                .join(recruitmentParticipation.user, user)
                .leftJoin(image)
                .on(image.imageNo.eq(user.userNo),
                        image.realWorkCode.eq(RealWorkCode.USER),
                        image.isDeleted.eq(IsDeleted.N))
                .leftJoin(image.storage, storage)
                .where(scheduleParticipation.schedule.scheduleNo.eq(scheduleNo),
                        scheduleParticipation.state.in(states))
                .fetch();
    }

    private void clear() {
        em.flush();
        em.clear();
    }
}
