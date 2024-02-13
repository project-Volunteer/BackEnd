package project.volunteer.domain.sehedule.repository;

import static project.volunteer.domain.sehedule.domain.QSchedule.schedule;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.sehedule.application.dto.query.ScheduleCalendarSearchResult;
import project.volunteer.domain.sehedule.application.dto.query.ScheduleDetailSearchResult;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

@Repository
@RequiredArgsConstructor
public class ScheduleQueryDSLRepositoryImpl implements ScheduleQueryDSLRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<ScheduleCalendarSearchResult> findScheduleDateBy(Recruitment recruitment, LocalDate toDate,
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

    @Override
    public ScheduleDetailSearchResult findScheduleDetailBy(Long scheduleNo) {
        ScheduleDetailSearchResult result = queryFactory.select(
                        Projections.constructor(ScheduleDetailSearchResult.class, schedule.scheduleNo, schedule.address.sido,
                                schedule.address.sigungu, schedule.address.details, schedule.address.fullName,
                                schedule.scheduleTimeTable.startDay, schedule.scheduleTimeTable.startTime,
                                schedule.scheduleTimeTable.hourFormat, schedule.scheduleTimeTable.progressTime,
                                schedule.volunteerNum, schedule.content, schedule.currentVolunteerNum))
                .from(schedule)
                .where(schedule.isDeleted.eq(IsDeleted.N),
                        schedule.scheduleNo.eq(scheduleNo))
                .fetchFirst();

        if(Objects.isNull(result)) {
            throw new BusinessException(ErrorCode.NOT_EXIST_SCHEDULE, String.format("ScheduleNo=[%d]", scheduleNo));
        }

        return result;
    }

    @Override
    public ScheduleDetailSearchResult findNearestScheduleDetailBy(Long recruitmentNo, LocalDate currentDate) {
        return queryFactory.select(
                        Projections.constructor(ScheduleDetailSearchResult.class, schedule.scheduleNo, schedule.address.sido,
                                schedule.address.sigungu, schedule.address.details, schedule.address.fullName,
                                schedule.scheduleTimeTable.startDay, schedule.scheduleTimeTable.startTime,
                                schedule.scheduleTimeTable.hourFormat, schedule.scheduleTimeTable.progressTime,
                                schedule.volunteerNum, schedule.content, schedule.currentVolunteerNum))
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