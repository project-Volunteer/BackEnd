package project.volunteer.domain.sehedule.repository;

import static project.volunteer.domain.sehedule.domain.QSchedule.schedule;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import project.volunteer.domain.sehedule.application.dto.query.ScheduleCalendarSearchResult;
import project.volunteer.domain.sehedule.repository.dao.ScheduleDetail;
import project.volunteer.global.common.component.IsDeleted;

@Repository
@RequiredArgsConstructor
public class ScheduleQueryDSLRepositoryImpl implements ScheduleQueryDSLRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<ScheduleCalendarSearchResult> findScheduleDateBy(Long recruitmentNo, LocalDate toDate,
                                                                 LocalDate fromDate) {
        return queryFactory.select(Projections.constructor(ScheduleCalendarSearchResult.class, schedule.scheduleNo,
                        schedule.scheduleTimeTable.startDay))
                .from(schedule)
                .where(schedule.isDeleted.eq(IsDeleted.N),
                        schedule.recruitment.recruitmentNo.eq(recruitmentNo),
                        schedule.scheduleTimeTable.startDay.between(toDate, fromDate))
                .orderBy(schedule.scheduleTimeTable.startDay.asc())
                .fetch();
    }

    @Override
    public Optional<ScheduleDetail> findScheduleDetailBy(Long scheduleNo) {
        return Optional.ofNullable(queryFactory
                .select(Projections.constructor(ScheduleDetail.class, schedule.scheduleNo, schedule.content,
                        schedule.volunteerNum, schedule.currentVolunteerNum, schedule.address,
                        schedule.scheduleTimeTable))
                .from(schedule)
                .where(schedule.isDeleted.eq(IsDeleted.N),
                        schedule.scheduleNo.eq(scheduleNo))
                .fetchOne());
    }

    @Override
    public ScheduleDetail findNearestScheduleDetailBy(Long recruitmentNo, LocalDate currentDate) {
        return queryFactory.select(
                        Projections.constructor(ScheduleDetail.class, schedule.scheduleNo, schedule.content,
                                schedule.volunteerNum, schedule.currentVolunteerNum, schedule.address,
                                schedule.scheduleTimeTable))
                .from(schedule)
                .where(schedule.isDeleted.eq(IsDeleted.N),
                        schedule.recruitment.recruitmentNo.eq(recruitmentNo),
                        schedule.scheduleTimeTable.startDay.after(currentDate))
                .orderBy(schedule.scheduleTimeTable.startDay.asc())
                .fetchFirst();
    }

    @Override
    public Boolean existNearestSchedule(Long recruitmentNo, LocalDate currentDate) {
        Integer isExist = queryFactory.selectOne()
                .from(schedule)
                .where(schedule.isDeleted.eq(IsDeleted.N),
                        schedule.recruitment.recruitmentNo.eq(recruitmentNo),
                        schedule.scheduleTimeTable.startDay.after(currentDate))
                .fetchFirst();

        return isExist != null;
    }


}