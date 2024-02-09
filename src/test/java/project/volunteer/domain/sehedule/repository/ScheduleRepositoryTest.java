package project.volunteer.domain.sehedule.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import project.volunteer.support.RepositoryTest;
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

class ScheduleRepositoryTest extends RepositoryTest {
    private final Address address = new Address("111", "11", "test", "test");
    private final Coordinate coordinate = new Coordinate(1.2F, 2.2F);
    private final Timetable timetable = new Timetable(LocalDate.now(), LocalDate.now(), HourFormat.AM, LocalTime.now(),
            10);
    private final Recruitment recruitment = Recruitment.builder()
            .title("title")
            .content("content")
            .volunteeringCategory(VolunteeringCategory.EDUCATION)
            .volunteerType(VolunteerType.ADULT)
            .volunteeringType(VolunteeringType.IRREG)
            .maxParticipationNum(999)
            .currentVolunteerNum(0)
            .isIssued(true)
            .organizationName("organization")
            .address(address)
            .coordinate(coordinate)
            .timetable(timetable)
            .viewCount(0)
            .likeCount(0)
            .isPublished(true)
            .isDeleted(IsDeleted.N)
            .build();

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
        List<ScheduleCalendarSearchResult> scheduleCalendar = scheduleRepository.findScheduleDateBy(
                recruitment, toDate, fromDate);

        //then
        assertThat(scheduleCalendar).hasSize(2)
                .extracting("scheduleNo")
                .contains(scheduleNo1, scheduleNo3);
    }

    @DisplayName("일정 id로 삭제된 봉사 일정을 조회할 경우 null를 반환한다.")
    @Test
    void findDeletedSchedule() {
        //given
        Schedule schedule = scheduleRepository.save(
                new Schedule(timetable, "test", "test", address, 10, IsDeleted.N, 8, recruitment));
        schedule.delete();

        //when
        Optional<Schedule> result = scheduleRepository.findNotDeletedSchedule(schedule.getScheduleNo());

        //then
        assertThat(result).isEmpty();
    }

    @DisplayName("봉사 모집글에 존재하는 일정들 중, 참여 가능한 일정이 있는지 확인한다.")
    @Test
    void existSchedule() {
        //given
        final LocalDate currentDate = LocalDate.of(2024, 1, 10);

        final Long scheduleNo1 = createAndSaveSchedule(LocalDate.of(2024, 1, 9)); // 참여 불가능
        final Long scheduleNo2 = createAndSaveSchedule(LocalDate.of(2024, 1, 10)); // 참여 불가능

        //when
        Boolean result = scheduleRepository.existNearestSchedule(recruitment.getRecruitmentNo(), currentDate);

        //then
        assertThat(result).isFalse();
    }

    private Long createAndSaveSchedule(LocalDate startDate) {
        Timetable time = new Timetable(startDate, startDate, HourFormat.PM, LocalTime.now(), 10);
        Schedule schedule = new Schedule(time, "test", "test", address, 10, IsDeleted.N, 8, recruitment);
        return scheduleRepository.save(schedule).getScheduleNo();
    }

}