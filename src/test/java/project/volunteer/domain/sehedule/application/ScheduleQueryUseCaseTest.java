package project.volunteer.domain.sehedule.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import project.volunteer.support.ServiceTest;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.sehedule.application.dto.query.ScheduleCalendarSearchResult;
import project.volunteer.domain.sehedule.application.dto.query.ScheduleDetailSearchResult;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.Coordinate;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.global.common.component.Timetable;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

class ScheduleQueryUseCaseTest extends ServiceTest {
    private final Address address = new Address("111", "11", "test", "test");
    private final Coordinate coordinate = new Coordinate(1.2F, 2.2F);
    private final Timetable timetable = new Timetable(LocalDate.now(), LocalDate.now(), HourFormat.AM, LocalTime.now(),
            10);
    private final Recruitment recruitment = new Recruitment("title", "content", VolunteeringCategory.EDUCATION,
            VolunteeringType.REG, VolunteerType.ADULT, 999, true, "unicef", address, coordinate, timetable, true, null);

    @BeforeEach
    void setUp() {
        recruitmentRepository.save(recruitment);
    }

    @DisplayName("2024년 2월에 존재하는 봉사 모집글 일정 날짜를 모두 조회한다.")
    @Test
    void searchScheduleCalender() {
        //given
        final LocalDate toDate = LocalDate.of(2024, 2, 1);
        final LocalDate fromDate = toDate.with(TemporalAdjusters.lastDayOfMonth());

        final Long scheduleNo1 = createAndSaveSchedule(LocalDate.of(2024, 2, 10));
        final Long scheduleNo2 = createAndSaveSchedule(LocalDate.of(2024, 2, 15));
        final Long scheduleNo3 = createAndSaveSchedule(LocalDate.of(2024, 2, 29));
        final Long scheduleNo4 = createAndSaveSchedule(LocalDate.of(2024, 1, 31));

        //when
        final List<ScheduleCalendarSearchResult> result = scheduleQueryUseCase.searchScheduleCalender(
                recruitment, toDate, fromDate);

        //then
        assertThat(result).hasSize(3)
                .extracting("scheduleNo")
                .contains(scheduleNo1, scheduleNo2, scheduleNo3);
    }

    @DisplayName("일정 정보를 상세 조회한다.")
    @Test
    void searchScheduleDetail() {
        //given
        final Schedule schedule = scheduleRepository.save(
                new Schedule(timetable, "test", "test", address, 10, IsDeleted.N, 8, recruitment));

        // when
        final ScheduleDetailSearchResult result = scheduleQueryUseCase.searchScheduleDetail(
                schedule.getScheduleNo());

        // then
        assertAll(
                () -> assertThat(result.getNo()).isEqualTo(schedule.getScheduleNo()),
                () -> assertThat(result.getAddress().getSido()).isEqualTo(schedule.getAddress().getSido()),
                () -> assertThat(result.getAddress().getSigungu()).isEqualTo(schedule.getAddress().getSigungu()),
                () -> assertThat(result.getAddress().getDetails()).isEqualTo(schedule.getAddress().getDetails()),
                () -> assertThat(result.getAddress().getFullName()).isEqualTo(schedule.getAddress().getFullName()),
                () -> assertThat(result.getStartDate()).isEqualTo(schedule.getScheduleTimeTable().getStartDay()),
                () -> assertThat(result.getHourFormat()).isEqualByComparingTo(
                        schedule.getScheduleTimeTable().getHourFormat()),
                () -> assertThat(result.getProgressTime()).isEqualTo(schedule.getScheduleTimeTable().getProgressTime()),
                () -> assertThat(result.getVolunteerNum()).isEqualTo(schedule.getVolunteerNum()),
                () -> assertThat(result.getActiveVolunteerNum()).isEqualTo(schedule.getCurrentVolunteerNum()),
                () -> assertThat(result.getContent()).isEqualTo(schedule.getContent())
        );
    }

    @DisplayName("삭제된 일정 정보를 조회할 경우, 예외가 발생한다.")
    @Test
    void throwExceptionWhenDeletedSchedule() {
        // given
        Schedule schedule = scheduleRepository.save(
                new Schedule(timetable, "test", "test", address, 10, IsDeleted.N, 8, recruitment));
        schedule.delete();

        // when & then
        assertThatThrownBy(() -> scheduleQueryUseCase.searchScheduleDetail(schedule.getScheduleNo()))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.NOT_EXIST_SCHEDULE.name());
    }

    @DisplayName("봉사 모집글에 존재하는 일정 중 참여가 가능 하면서, 가장 가까운 일정 정보를 상세 조회한다.")
    @Test
    void searchClosestScheduleDetail() {
        //given
        final Long scheduleNo1 = createAndSaveSchedule(LocalDate.of(2024, 1, 15));
        final Long scheduleNo2 = createAndSaveSchedule(LocalDate.of(2024, 1, 16));
        final Long scheduleNo3 = createAndSaveSchedule(LocalDate.of(2024, 1, 17));
        final Long scheduleNo4 = createAndSaveSchedule(LocalDate.of(2024, 1, 20));

        given(clock.instant()).willReturn(Instant.parse("2024-01-16T10:00:00Z"));

        //when
        ScheduleDetailSearchResult result = scheduleQueryUseCase.searchClosestScheduleDetail(
                recruitment.getRecruitmentNo());

        //then
        assertAll(
                () -> assertThat(result.getHasData()).isTrue(),
                () -> assertThat(result.getNo()).isEqualTo(scheduleNo3)
        );
    }

    @DisplayName("봉사 모집글에 존재하는 일정 중 모두 참여 불가능한 일정일 경우, hasData컬럼이 false가 된다.")
    @Test
    void notExistClosestSchedule() {
        //given
        final Long scheduleNo1 = createAndSaveSchedule(LocalDate.of(2024, 1, 15));
        final Long scheduleNo2 = createAndSaveSchedule(LocalDate.of(2024, 1, 16));

        given(clock.instant()).willReturn(Instant.parse("2024-01-16T10:00:00Z"));

        //when
        ScheduleDetailSearchResult result = scheduleQueryUseCase.searchClosestScheduleDetail(
                recruitment.getRecruitmentNo());

        //then
        assertThat(result.hasData()).isFalse();
    }

    @DisplayName("모집 기간이 지난 일정을 조회할 경우 예외가 발생한다.")
    @Test
    void searchDoneScheduleWithException() {
        //given
        final Long scheduleNo = createAndSaveSchedule(LocalDate.of(2024, 1, 16));
        given(clock.instant()).willReturn(Instant.parse("2024-01-17T10:00:00Z"));

        //when & then
        assertThatThrownBy(() -> scheduleQueryUseCase.findScheduleInProgress(scheduleNo))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.EXPIRED_PERIOD_SCHEDULE.name());
    }

    private Long createAndSaveSchedule(LocalDate startDate) {
        Timetable time = new Timetable(startDate, startDate, HourFormat.PM, LocalTime.now(), 10);
        Schedule schedule = new Schedule(time, "test", "test", address, 10, IsDeleted.N, 8, recruitment);
        return scheduleRepository.save(schedule).getScheduleNo();
    }

}