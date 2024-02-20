package project.volunteer.domain.scheduleParticipation.repository;

import static project.volunteer.domain.scheduleParticipation.domain.QScheduleParticipation.scheduleParticipation;
import static project.volunteer.domain.sehedule.domain.QSchedule.schedule;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import javax.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.global.common.component.ParticipantState;

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

    private void clear() {
        em.flush();
        em.clear();
    }
}
