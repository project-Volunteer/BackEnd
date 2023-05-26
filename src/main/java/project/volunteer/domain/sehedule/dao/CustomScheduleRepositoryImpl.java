package project.volunteer.domain.sehedule.dao;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.global.common.component.IsDeleted;

import java.time.LocalDate;
import java.util.Optional;

import static project.volunteer.domain.sehedule.domain.QSchedule.schedule;

@Repository
@RequiredArgsConstructor
public class CustomScheduleRepositoryImpl implements CustomScheduleRepository{

    private final JPAQueryFactory jpaQueryFactory;
    @Override
    public Optional<Schedule> findNearestSchedule(Long recruitmentNo) {
        return Optional.ofNullable(jpaQueryFactory.select(schedule)
                .from(schedule)
                .where(
                        schedule.recruitment.recruitmentNo.eq(recruitmentNo),
                        schedule.scheduleTimeTable.startDay.after(LocalDate.now()),
                        schedule.isDeleted.eq(IsDeleted.N)
                )
                .orderBy(schedule.scheduleTimeTable.startDay.asc())
                .fetchFirst());
    }
}
