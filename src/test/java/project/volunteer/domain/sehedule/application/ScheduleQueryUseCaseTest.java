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
import project.volunteer.domain.participation.domain.Participant;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.common.dto.StateResult;
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
    private final User writer = new User("test", "test", "test", "test@email.com", Gender.M, LocalDate.now(),
                    "http://", true, true, true, Role.USER, "kakao", "1234", null);
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
            .writer(writer)
            .build();

    @BeforeEach
    void setUp() {
        userRepository.save(writer);
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
        final User user = userRepository.save(
                new User("test", "test", "test", "test@email.com", Gender.M, LocalDate.now(),
                        "http://", true, true, true, Role.USER, "kakao", "1234", null));
        final Schedule schedule = scheduleRepository.save(
                new Schedule(new Timetable(LocalDate.of(2024, 1, 10), LocalDate.of(2024, 1, 12), HourFormat.PM,
                        LocalTime.now(), 10), "test", "test",
                        address, 10, IsDeleted.N, 8, recruitment));

        given(clock.instant()).willReturn(Instant.parse("2024-01-08T10:00:00Z"));

        // when
        final ScheduleDetailSearchResult result = scheduleQueryUseCase.searchScheduleDetail(user.getUserNo(),
                schedule.getScheduleNo());

        // then
        assertAll(
                () -> assertThat(result.getNo()).isEqualTo(schedule.getScheduleNo()),
                () -> assertThat(result.getHasData()).isTrue(),
                () -> assertThat(result.getState()).isEqualTo(StateResult.AVAILABLE.getId())
        );
    }

    @DisplayName("일정 상세 조회 간 마감된 경우, 요청한 회원 상태는 DONE이 된다.")
    @Test
    void searchScheduleDetailWithDone() {
        //given
        final User user = userRepository.save(
                new User("test", "test", "test", "test@email.com", Gender.M, LocalDate.now(),
                        "http://", true, true, true, Role.USER, "kakao", "1234", null));
        final Schedule schedule = scheduleRepository.save(
                new Schedule(new Timetable(LocalDate.of(2024, 1, 10), LocalDate.of(2024, 1, 12), HourFormat.PM,
                        LocalTime.now(), 10), "test", "test",
                        address, 10, IsDeleted.N, 5, recruitment));

        given(clock.instant()).willReturn(Instant.parse("2024-01-11T10:00:00Z"));

        // when
        final ScheduleDetailSearchResult result = scheduleQueryUseCase.searchScheduleDetail(user.getUserNo(),
                schedule.getScheduleNo());

        // then
        assertAll(
                () -> assertThat(result.getNo()).isEqualTo(schedule.getScheduleNo()),
                () -> assertThat(result.getHasData()).isTrue(),
                () -> assertThat(result.getState()).isEqualTo(StateResult.DONE.getId())
        );
    }

    @DisplayName("일정 상세 조회 간 참여 인원이 가득찬 경우, 요청한 회원 상태는 FULL이 된다.")
    @Test
    void searchScheduleDetailWithFull() {
        //given
        final User user = userRepository.save(
                new User("test", "test", "test", "test@email.com", Gender.M, LocalDate.now(),
                        "http://", true, true, true, Role.USER, "kakao", "1234", null));
        final Schedule schedule = scheduleRepository.save(
                new Schedule(new Timetable(LocalDate.of(2024, 1, 10), LocalDate.of(2024, 1, 12), HourFormat.PM,
                        LocalTime.now(), 10), "test", "test",
                        address, 10, IsDeleted.N, 10, recruitment));

        given(clock.instant()).willReturn(Instant.parse("2024-01-08T10:00:00Z"));

        // when
        final ScheduleDetailSearchResult result = scheduleQueryUseCase.searchScheduleDetail(user.getUserNo(),
                schedule.getScheduleNo());

        // then
        assertAll(
                () -> assertThat(result.getNo()).isEqualTo(schedule.getScheduleNo()),
                () -> assertThat(result.getHasData()).isTrue(),
                () -> assertThat(result.getState()).isEqualTo(StateResult.FULL.getId())
        );
    }

    @DisplayName("일정 상세 조회 간 참여 중인 회원일 경우, 회원 상태는 PARTICIPATING이 된다.")
    @Test
    void searchScheduleDetailWithParticipating() {
        //given
        final User user = userRepository.save(
                new User("test", "test", "test", "test@email.com", Gender.M, LocalDate.now(),
                        "http://", true, true, true, Role.USER, "kakao", "1234", null));
        final Schedule schedule = scheduleRepository.save(
                new Schedule(new Timetable(LocalDate.of(2024, 1, 10), LocalDate.of(2024, 1, 12), HourFormat.PM,
                        LocalTime.now(), 10), "test", "test",
                        address, 10, IsDeleted.N, 10, recruitment));

        createAndSaveScheduleParticipation(user, schedule, ParticipantState.PARTICIPATING);

        given(clock.instant()).willReturn(Instant.parse("2024-01-11T10:00:00Z"));

        // when
        final ScheduleDetailSearchResult result = scheduleQueryUseCase.searchScheduleDetail(user.getUserNo(),
                schedule.getScheduleNo());

        // then
        assertAll(
                () -> assertThat(result.getNo()).isEqualTo(schedule.getScheduleNo()),
                () -> assertThat(result.getHasData()).isTrue(),
                () -> assertThat(result.getState()).isEqualTo(StateResult.PARTICIPATING.getId())
        );
    }

    @DisplayName("삭제된 일정 정보를 상세 조회할 경우, 예외가 발생한다.")
    @Test
    void searchDeletedScheduleDetailWithException() {
        // given
        final User user = userRepository.save(
                new User("test", "test", "test", "test@email.com", Gender.M, LocalDate.now(),
                        "http://", true, true, true, Role.USER, "kakao", "1234", null));

        Schedule schedule = scheduleRepository.save(
                new Schedule(timetable, "test", "test", address, 10, IsDeleted.N, 8, recruitment));
        schedule.delete();

        // when & then
        assertThatThrownBy(() -> scheduleQueryUseCase.searchScheduleDetail(user.getUserNo(), schedule.getScheduleNo()))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.NOT_EXIST_SCHEDULE.name());
    }

    @DisplayName("존재하지 않는 일정 정보를 상세 조회할 경우, 예외가 발생한다.")
    @Test
    void searchNotExistedScheduleDetailWithException() {
        // given
        final User user = userRepository.save(
                new User("test", "test", "test", "test@email.com", Gender.M, LocalDate.now(),
                        "http://", true, true, true, Role.USER, "kakao", "1234", null));

        // when & then
        assertThatThrownBy(() -> scheduleQueryUseCase.searchScheduleDetail(user.getUserNo(), 999L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.NOT_EXIST_SCHEDULE.name());
    }

    @DisplayName("봉사 모집글에 존재하는 일정 중 참여가 가능 하면서, 가장 가까운 일정 정보를 상세 조회한다.")
    @Test
    void searchClosestScheduleDetail() {
        //given
        final User user = userRepository.save(
                new User("test", "test", "test", "test@email.com", Gender.M, LocalDate.now(),
                        "http://", true, true, true, Role.USER, "kakao", "1234", null));
        final Long scheduleNo1 = createAndSaveSchedule(LocalDate.of(2024, 1, 15));
        final Long scheduleNo2 = createAndSaveSchedule(LocalDate.of(2024, 1, 16));
        final Long scheduleNo3 = createAndSaveSchedule(LocalDate.of(2024, 1, 17));
        final Long scheduleNo4 = createAndSaveSchedule(LocalDate.of(2024, 1, 20));

        given(clock.instant()).willReturn(Instant.parse("2024-01-16T10:00:00Z"));

        //when
        ScheduleDetailSearchResult result = scheduleQueryUseCase.searchClosestScheduleDetail(user.getUserNo(),
                recruitment.getRecruitmentNo());

        //then
        assertAll(
                () -> assertThat(result.getHasData()).isTrue(),
                () -> assertThat(result.getNo()).isEqualTo(scheduleNo3),
                () -> assertThat(result.getState()).isEqualTo(StateResult.AVAILABLE.getId())
        );
    }

    @DisplayName("봉사 모집글에 존재하는 일정 중 모두 참여 불가능한 일정일 경우, hasData컬럼이 false가 된다.")
    @Test
    void notExistClosestSchedule() {
        //given
        final User user = userRepository.save(
                new User("test", "test", "test", "test@email.com", Gender.M, LocalDate.now(),
                        "http://", true, true, true, Role.USER, "kakao", "1234", null));
        final Long scheduleNo1 = createAndSaveSchedule(LocalDate.of(2024, 1, 15));
        final Long scheduleNo2 = createAndSaveSchedule(LocalDate.of(2024, 1, 16));

        final LocalDate now = LocalDate.of(2024, 1, 16);

        //when
        ScheduleDetailSearchResult result = scheduleQueryUseCase.searchClosestScheduleDetail(user.getUserNo(),
                recruitment.getRecruitmentNo());

        //then
        assertThat(result.getHasData()).isFalse();
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

    private void createAndSaveScheduleParticipation(User user, Schedule schedule, ParticipantState state) {
        Participant participant = participantRepository.save(new Participant(recruitment, user, ParticipantState.JOIN_APPROVAL));
        scheduleParticipationRepository.save(new ScheduleParticipation(schedule, participant, state));
    }

}