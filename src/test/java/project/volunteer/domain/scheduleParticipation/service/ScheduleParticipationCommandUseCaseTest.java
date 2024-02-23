package project.volunteer.domain.scheduleParticipation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitmentParticipation.domain.RecruitmentParticipation;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.Coordinate;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.common.component.Timetable;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;
import project.volunteer.support.ServiceTest;

class ScheduleParticipationCommandUseCaseTest extends ServiceTest {
    private final Timetable timetable = new Timetable(LocalDate.now(), LocalDate.now(), HourFormat.AM, LocalTime.now(),
            10);
    private final Address address = new Address("1111", "111", "삼성 아파트", "대구광역시 북구 삼성 아파트");
    private final Coordinate coordinate = new Coordinate(1.2F, 2.2F);
    private final User writer = new User("test", "test", "test", "test@email.com", Gender.M, LocalDate.now(),
            "http://...", true, true, true, Role.USER, "kakao", "1234", null);
    private final User firstUser = new User("test1", "test", "test", "test@email.com", Gender.M, LocalDate.now(),
            "http://...", true, true, true, Role.USER, "kakao", "1234", null);
    private final User secondUser = new User("test2", "test", "test", "test@email.com", Gender.M, LocalDate.now(),
            "http://...", true, true, true, Role.USER, "kakao", "1234", null);
    private final Recruitment recruitment = Recruitment.builder()
            .title("title")
            .content("content")
            .volunteeringCategory(VolunteeringCategory.EDUCATION)
            .volunteerType(VolunteerType.ADULT)
            .volunteeringType(VolunteeringType.IRREG)
            .maxParticipationNum(9999)
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
        userRepository.save(firstUser);
        userRepository.save(secondUser);
        recruitmentRepository.save(recruitment);
    }

    @DisplayName("일정 첫 참여에 성공하고, 일정 참여 인원을 늘린다.")
    @Test
    void participate() {
        //given
        final RecruitmentParticipation recruitmentParticipation = recruitmentParticipationRepository.save(
                new RecruitmentParticipation(recruitment, firstUser, ParticipantState.JOIN_APPROVAL));
        final Schedule schedule = scheduleRepository.save(
                new Schedule(timetable, "test", "unicef", address, 10, IsDeleted.N, 0, recruitment));

        //when
        Long id = scheduleParticipationCommandUseCase.participate(schedule, recruitmentParticipation);

        //then
        ScheduleParticipation scheduleParticipation = findScheduleParticipation(id);
        assertAll(
                () -> assertThat(scheduleParticipation.getState()).isEqualByComparingTo(ParticipantState.PARTICIPATING),
                () -> assertThat(schedule.getCurrentVolunteerNum()).isEqualTo(1)
        );
    }

    @DisplayName("일정 참여 인원이 가득찬 경우 예외를 발생시킨다.")
    @Test
    void participateFullSchedule() {
        //given
        final RecruitmentParticipation recruitmentParticipation = recruitmentParticipationRepository.save(
                new RecruitmentParticipation(recruitment, firstUser, ParticipantState.JOIN_APPROVAL));
        final Schedule schedule = scheduleRepository.save(
                new Schedule(timetable, "test", "unicef", address, 10, IsDeleted.N, 10, recruitment));

        //when & then
        assertThatThrownBy(() -> scheduleParticipationCommandUseCase.participate(schedule, recruitmentParticipation))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INSUFFICIENT_CAPACITY.name());
    }

    @DisplayName("중복 참여를 할 경우 예외를 발생시킨다.")
    @Test
    void duplicationParticipate() {
        //given
        final RecruitmentParticipation recruitmentParticipation = recruitmentParticipationRepository.save(
                new RecruitmentParticipation(recruitment, firstUser, ParticipantState.JOIN_APPROVAL));
        final Schedule schedule = scheduleRepository.save(
                new Schedule(timetable, "test", "unicef", address, 10, IsDeleted.N, 0, recruitment));

        scheduleParticipationRepository.save(
                new ScheduleParticipation(schedule, recruitmentParticipation, ParticipantState.PARTICIPATING));

        //when & then
        assertThatThrownBy(() -> scheduleParticipationCommandUseCase.participate(schedule, recruitmentParticipation))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.DUPLICATE_SCHEDULE_PARTICIPATION.name());
    }

    @DisplayName("일정 재 참여에 성공하고, 일정 참여 인원을 늘린다.")
    @Test
    void reParticipate() {
        //given
        final RecruitmentParticipation recruitmentParticipation = recruitmentParticipationRepository.save(
                new RecruitmentParticipation(recruitment, firstUser, ParticipantState.JOIN_APPROVAL));
        final Schedule schedule = scheduleRepository.save(
                new Schedule(timetable, "test", "unicef", address, 10, IsDeleted.N, 0, recruitment));

        scheduleParticipationRepository.save(
                new ScheduleParticipation(schedule, recruitmentParticipation,
                        ParticipantState.PARTICIPATION_CANCEL_APPROVAL));

        //when
        Long id = scheduleParticipationCommandUseCase.participate(schedule, recruitmentParticipation);

        //then
        ScheduleParticipation scheduleParticipation = findScheduleParticipation(id);
        assertAll(
                () -> assertThat(scheduleParticipation.getState()).isEqualByComparingTo(ParticipantState.PARTICIPATING),
                () -> assertThat(schedule.getCurrentVolunteerNum()).isEqualTo(1)
        );
    }

    @DisplayName("일점 참여 취소에 성공한다.")
    @Test
    void cancelParticipation() {
        //given
        final RecruitmentParticipation recruitmentParticipation = recruitmentParticipationRepository.save(
                new RecruitmentParticipation(recruitment, firstUser, ParticipantState.JOIN_APPROVAL));
        final Schedule schedule = scheduleRepository.save(
                new Schedule(timetable, "test", "unicef", address, 10, IsDeleted.N, 0, recruitment));
        final ScheduleParticipation scheduleParticipation = scheduleParticipationRepository.save(
                new ScheduleParticipation(schedule, recruitmentParticipation, ParticipantState.PARTICIPATING));

        //when
        scheduleParticipationCommandUseCase.cancelParticipation(schedule, recruitmentParticipation);

        //then
        ScheduleParticipation findScheduleParticipation = findScheduleParticipation(scheduleParticipation.getId());
        assertThat(findScheduleParticipation.getState()).isEqualByComparingTo(ParticipantState.PARTICIPATION_CANCEL);
    }

    @DisplayName("일정 참여 취소 전 상태가 PARTICIPATING가 아닐 경우, 예외를 발생시킨다.")
    @Test
    void cancelParticipationInvalidState() {
        //given
        final RecruitmentParticipation recruitmentParticipation = recruitmentParticipationRepository.save(
                new RecruitmentParticipation(recruitment, firstUser, ParticipantState.JOIN_APPROVAL));
        final Schedule schedule = scheduleRepository.save(
                new Schedule(timetable, "test", "unicef", address, 10, IsDeleted.N, 0, recruitment));
        scheduleParticipationRepository.save(
                new ScheduleParticipation(schedule, recruitmentParticipation, ParticipantState.PARTICIPATION_CANCEL));

        //when & then
        assertThatThrownBy(
                () -> scheduleParticipationCommandUseCase.cancelParticipation(schedule, recruitmentParticipation))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_STATE.name());
    }

    @DisplayName("취소를 승인하고, 일정 참여 인원을 줄인다.")
    @Test
    void approveCancellation() {
        //given
        final RecruitmentParticipation recruitmentParticipation1 = recruitmentParticipationRepository.save(
                new RecruitmentParticipation(recruitment, firstUser, ParticipantState.JOIN_APPROVAL));
        final RecruitmentParticipation recruitmentParticipation2 = recruitmentParticipationRepository.save(
                new RecruitmentParticipation(recruitment, secondUser, ParticipantState.JOIN_APPROVAL));

        final Schedule schedule = scheduleRepository.save(
                new Schedule(timetable, "test", "unicef", address, 10, IsDeleted.N, 3, recruitment));

        final List<Long> scheduleParticipationIds = List.of(
                scheduleParticipationRepository.save(
                                new ScheduleParticipation(schedule, recruitmentParticipation1, ParticipantState.PARTICIPATION_CANCEL))
                        .getId(),
                scheduleParticipationRepository.save(
                                new ScheduleParticipation(schedule, recruitmentParticipation2, ParticipantState.PARTICIPATION_CANCEL))
                        .getId());

        //when
        scheduleParticipationCommandUseCase.approvalCancellation(schedule, scheduleParticipationIds);

        //then
        List<ScheduleParticipation> scheduleParticipants = scheduleParticipationRepository.findAll();
        assertAll(
                () -> assertThat(schedule.getCurrentVolunteerNum()).isEqualTo(1),
                () -> assertThat(scheduleParticipants).hasSize(2)
                        .extracting("state")
                        .containsExactlyInAnyOrder(ParticipantState.PARTICIPATION_CANCEL_APPROVAL,
                                ParticipantState.PARTICIPATION_CANCEL_APPROVAL)
        );
    }

    @DisplayName("취소 승인전 상태가 PARTICIPATION_CANCEL이 아닐 경우, 예외를 발생시킨다.")
    @Test
    void approveCancellationWithInvalidState() {
        //given
        final RecruitmentParticipation recruitmentParticipation1 = recruitmentParticipationRepository.save(
                new RecruitmentParticipation(recruitment, firstUser, ParticipantState.JOIN_APPROVAL));
        final RecruitmentParticipation recruitmentParticipation2 = recruitmentParticipationRepository.save(
                new RecruitmentParticipation(recruitment, secondUser, ParticipantState.JOIN_APPROVAL));

        final Schedule schedule = scheduleRepository.save(
                new Schedule(timetable, "test", "unicef", address, 10, IsDeleted.N, 3, recruitment));

        final List<Long> scheduleParticipationIds = List.of(
                scheduleParticipationRepository.save(
                                new ScheduleParticipation(schedule, recruitmentParticipation1, ParticipantState.PARTICIPATING))
                        .getId(),
                scheduleParticipationRepository.save(
                                new ScheduleParticipation(schedule, recruitmentParticipation2, ParticipantState.PARTICIPATING))
                        .getId());

        //when
        assertThatThrownBy(() -> scheduleParticipationCommandUseCase.approvalCancellation(schedule, scheduleParticipationIds))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_STATE.name());

    }

    private ScheduleParticipation findScheduleParticipation(Long id) {
        return scheduleParticipationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("일정 참여 정보가 존재하지 않습니다."));
    }

}