package project.volunteer.domain.scheduleParticipation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.amazonaws.services.kms.model.NotFoundException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import project.volunteer.domain.recruitmentParticipation.domain.RecruitmentParticipation;
import project.volunteer.support.RepositoryTest;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
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

class ScheduleParticipationRepositoryTest extends RepositoryTest {
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
    private final User user1 = new User("test1", "test1", "test1", "test1@test.com", Gender.M, LocalDate.now(), "test",
            true, true, true, Role.USER, "test", "test", null);
    private final User user2 = new User("test2", "test2", "test2", "test2@test.com", Gender.M, LocalDate.now(), "test",
            true, true, true, Role.USER, "test", "test", null);
    private final RecruitmentParticipation recruitmentParticipant1 = new RecruitmentParticipation(recruitment, user1,
            ParticipantState.JOIN_APPROVAL);
    private final RecruitmentParticipation recruitmentParticipant2 = new RecruitmentParticipation(recruitment, user2,
            ParticipantState.JOIN_APPROVAL);

    @BeforeEach
    void setUp() {
        recruitmentRepository.save(recruitment);

        userRepository.save(user1);
        userRepository.save(user2);

        participantRepository.save(recruitmentParticipant1);
        participantRepository.save(recruitmentParticipant2);
    }

    @DisplayName("모집 종료된 일정 참여자들의 상태를 참여 중에서 미승인 참여 완료 상태로 변경한다.")
    @Test
    void updateParticipantStateFinishedSchedule() {
        //given
        final LocalDate currentDate = LocalDate.of(2023, 1, 6);
        final Schedule finishedSchedule = createAndSaveSchedule(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 5));

        final Long scheduleParticipationNo1 = createAndSaveScheduleParticipation(finishedSchedule,
                recruitmentParticipant1);
        final Long scheduleParticipationNo2 = createAndSaveScheduleParticipation(finishedSchedule,
                recruitmentParticipant2);

        //when
        scheduleParticipationRepository.unApprovedCompleteOfAllFinishedScheduleParticipant(currentDate);

        //then
        assertThat(findBy(scheduleParticipationNo1).getState()).isEqualByComparingTo(
                ParticipantState.PARTICIPATION_COMPLETE_UNAPPROVED);
        assertThat(findBy(scheduleParticipationNo2).getState()).isEqualByComparingTo(
                ParticipantState.PARTICIPATION_COMPLETE_UNAPPROVED);
    }

    @DisplayName("모집 중인 일정에 참가자들의 상태는 업데이트 없이 참여중 상태이다.")
    @Test
    void notUpdateParticipantStateRecruitingSchedule() {
        //given
        final LocalDate currentDate = LocalDate.of(2023, 1, 6);
        final Schedule recruitingSchedule = createAndSaveSchedule(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 7));

        final Long scheduleParticipationNo1 = createAndSaveScheduleParticipation(recruitingSchedule,
                recruitmentParticipant1);
        final Long scheduleParticipationNo2 = createAndSaveScheduleParticipation(recruitingSchedule,
                recruitmentParticipant2);

        //when
        scheduleParticipationRepository.unApprovedCompleteOfAllFinishedScheduleParticipant(currentDate);

        //then
        assertThat(findBy(scheduleParticipationNo1).getState()).isEqualByComparingTo(ParticipantState.PARTICIPATING);
        assertThat(findBy(scheduleParticipationNo2).getState()).isEqualByComparingTo(ParticipantState.PARTICIPATING);
    }

    @DisplayName("scheduleNo에 속한 모든 일정 참여 정보의 연관관계를 끊는다.")
    @Test
    void bulkUpdateDetachByScheduleNo() {
        //given
        final Schedule schedule = scheduleRepository.save(
                new Schedule(timetable, "test", "unicef", address, 10, IsDeleted.N, 3, recruitment));

        scheduleParticipationRepository.save(
                new ScheduleParticipation(schedule, recruitmentParticipant1, ParticipantState.PARTICIPATING));
        scheduleParticipationRepository.save(
                new ScheduleParticipation(schedule, recruitmentParticipant2, ParticipantState.PARTICIPATION_CANCEL));

        //when
        scheduleParticipationRepository.bulkUpdateDetachByScheduleNo(schedule.getScheduleNo());

        //then
        List<ScheduleParticipation> scheduleParticipations = scheduleParticipationRepository.findAll();
        assertThat(scheduleParticipations).hasSize(2)
                .allSatisfy(scheduleParticipation -> {
                    assertThat(scheduleParticipation.getSchedule()).isNull();
                    assertThat(scheduleParticipation.getRecruitmentParticipation()).isNull();
                });
    }

    private Schedule createAndSaveSchedule(LocalDate fromDate, LocalDate toDate) {
        Schedule schedule = Schedule.builder()
                .timetable(new Timetable(fromDate, toDate, HourFormat.AM, LocalTime.now(), 10))
                .content("test")
                .organizationName("test")
                .address(address)
                .participationNum(10)
                .isDeleted(IsDeleted.N)
                .currentVolunteerNum(8)
                .recruitment(recruitment)
                .build();
        return scheduleRepository.save(schedule);
    }

    private Long createAndSaveScheduleParticipation(Schedule schedule, RecruitmentParticipation participant) {
        ScheduleParticipation scheduleParticipation = new ScheduleParticipation(schedule, participant,
                ParticipantState.PARTICIPATING);
        return scheduleParticipationRepository.save(scheduleParticipation).getId();
    }

    private ScheduleParticipation findBy(Long scheduleParticipationNo) {
        return scheduleParticipationRepository.findById(scheduleParticipationNo)
                .orElseThrow(() -> new NotFoundException("일정 참여 정보가 존재하지 않습니다."));
    }

}