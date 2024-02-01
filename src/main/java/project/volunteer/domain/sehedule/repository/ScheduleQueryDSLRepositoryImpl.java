package project.volunteer.domain.sehedule.repository;

import static project.volunteer.domain.sehedule.domain.QSchedule.schedule;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.sehedule.application.dto.query.ScheduleCalendarSearchResult;
import project.volunteer.global.common.component.IsDeleted;

@Repository
@RequiredArgsConstructor
public class ScheduleQueryDSLRepositoryImpl implements ScheduleQueryDSLRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<ScheduleCalendarSearchResult> findScheduleDate(Recruitment recruitment, LocalDate toDate,
                                                               LocalDate fromDate) {
        return queryFactory.select(Projections.constructor(ScheduleCalendarSearchResult.class, schedule.scheduleNo,
                        schedule.scheduleTimeTable.startDay))
                .from(schedule)
                .where(schedule.isDeleted.eq(IsDeleted.N),
                        schedule.recruitment.eq(recruitment),
                        schedule.scheduleTimeTable.startDay.between(toDate, fromDate))
                .orderBy(schedule.scheduleTimeTable.startDay.asc())
                .fetch();
    }

}