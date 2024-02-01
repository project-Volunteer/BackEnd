package project.volunteer.domain.sehedule.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import project.volunteer.common.RepositoryTest;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.sehedule.application.dto.query.ScheduleCalendarSearchResult;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.Coordinate;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.global.common.component.Timetable;

class ScheduleQueryDSLRepositoryTest extends RepositoryTest {
    private final Address address = new Address("111", "11", "test", "test");
    private final Coordinate coordinate = new Coordinate(1.2F, 2.2F);
    private final Timetable timetable = new Timetable(LocalDate.now(), LocalDate.now(), HourFormat.AM, LocalTime.now(),
            10);
    private final Recruitment recruitment = new Recruitment("title", "content", VolunteeringCategory.EDUCATION,
            VolunteeringType.REG, VolunteerType.ADULT, 999, true, "unicef", address, coordinate, timetable, true);

    @BeforeEach
    void setUp() {
        recruitmentRepository.save(recruitment);
    }

    @DisplayName("모집글 id와 기간에 속하는 모든 일정의 날짜를 조회한다.")
    @Test
    void findScheduleCalendar() {
        //given
        final LocalDate toDate = LocalDate.of(2024, 1, 1);
        final LocalDate fromDate = LocalDate.of(2024, 1, 13);

        Long scheduleNo1 = createAndSaveSchedule(LocalDate.of(2024, 1, 10));
        Long scheduleNo2 = createAndSaveSchedule(LocalDate.of(2024, 1, 15));
        Long scheduleNo3 = createAndSaveSchedule(LocalDate.of(2024, 1, 13));

        //when
        List<ScheduleCalendarSearchResult> scheduleCalendar = scheduleRepository.findScheduleDate(
                recruitment, toDate, fromDate);

        //then
        Assertions.assertThat(scheduleCalendar).hasSize(2)
                .extracting("scheduleNo")
                .contains(scheduleNo1, scheduleNo3);
    }

    private Long createAndSaveSchedule(LocalDate startDate) {
        Timetable time = new Timetable(startDate, startDate, HourFormat.PM, LocalTime.now(), 10);
        Schedule schedule = new Schedule(time, "test", "test", address, 10, IsDeleted.N, 8, recruitment);
        return scheduleRepository.save(schedule).getScheduleNo();
    }

}