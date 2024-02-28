package project.volunteer.domain.scheduleParticipation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitmentParticipation.domain.RecruitmentParticipation;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;
import project.volunteer.domain.scheduleParticipation.service.dto.CancelledParticipantsSearchResult;
import project.volunteer.domain.scheduleParticipation.service.dto.ParticipatingParticipantList;
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
import project.volunteer.support.ServiceTest;

class ScheduleParticipationQueryUseCaseTest extends ServiceTest {
    private final Timetable timetable = new Timetable(LocalDate.now(), LocalDate.now(), HourFormat.AM, LocalTime.now(),
            10);
    private final Address address = new Address("1111", "111", "삼성 아파트", "대구광역시 북구 삼성 아파트");
    private final Coordinate coordinate = new Coordinate(1.2F, 2.2F);
    private final User writer = new User("test", "test", "test", "test@email.com", Gender.M, LocalDate.now(),
            "http://...", true, true, true, Role.USER, "kakao", "1234", null);
    private final User firstUser = new User("test1", "test", "test", "test@email.com", Gender.M, LocalDate.now(),
            "http://first...", true, true, true, Role.USER, "kakao", "1234", null);
    private final User secondUser = new User("test2", "test", "test", "test@email.com", Gender.M, LocalDate.now(),
            "http://second...", true, true, true, Role.USER, "kakao", "1234", null);
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
    private final RecruitmentParticipation firstRecruitmentParticipation = new RecruitmentParticipation(recruitment,
            firstUser, ParticipantState.JOIN_APPROVAL);
    private final RecruitmentParticipation secondRecruitmentParticipation = new RecruitmentParticipation(recruitment,
            secondUser, ParticipantState.JOIN_APPROVAL);
    private final Schedule schedule = new Schedule(timetable, "test", "unicef", address, 100, IsDeleted.N, 0,
            recruitment);

    @BeforeEach
    void setUp() {
        userRepository.save(writer);
        userRepository.save(firstUser);
        userRepository.save(secondUser);

        recruitmentRepository.save(recruitment);

        recruitmentParticipationRepository.save(firstRecruitmentParticipation);
        recruitmentParticipationRepository.save(secondRecruitmentParticipation);

        scheduleRepository.save(schedule);
    }

    @DisplayName("일정 참여자 리스트를 조회한다.")
    @Test
    void searchParticipantList() {
        //given
        final ScheduleParticipation scheduleParticipation1 = new ScheduleParticipation(schedule,
                firstRecruitmentParticipation, ParticipantState.PARTICIPATING);
        final ScheduleParticipation scheduleParticipation2 = new ScheduleParticipation(schedule,
                secondRecruitmentParticipation, ParticipantState.PARTICIPATING);

        scheduleParticipationRepository.saveAll(List.of(scheduleParticipation1, scheduleParticipation2));

        //when
        List<ParticipatingParticipantList> participatingParticipantLists = scheduleParticipationQueryUseCase.searchParticipatingList(
                schedule.getScheduleNo());

        //then
        assertThat(participatingParticipantLists).hasSize(2)
                .extracting("nickname", "email", "profile")
                .containsExactlyInAnyOrder(
                        tuple(firstUser.getNickName(), firstUser.getEmail(), firstUser.getPicture()),
                        tuple(secondUser.getNickName(), secondUser.getEmail(), secondUser.getPicture())
                );
    }

    @DisplayName("일정 참여를 취소한 리스트를 조회한다.")
    @Test
    void searchCancelledParticipantList() {
        //given
        final ScheduleParticipation scheduleParticipation1 = new ScheduleParticipation(schedule,
                firstRecruitmentParticipation, ParticipantState.PARTICIPATION_CANCEL);
        final ScheduleParticipation scheduleParticipation2 = new ScheduleParticipation(schedule,
                secondRecruitmentParticipation, ParticipantState.PARTICIPATION_CANCEL);

        scheduleParticipationRepository.saveAll(List.of(scheduleParticipation1, scheduleParticipation2));

        //when
        CancelledParticipantsSearchResult result = scheduleParticipationQueryUseCase.searchCancelledParticipationList(
                schedule.getScheduleNo());

        //then
        assertThat(result.getCancelling()).hasSize(2)
                .extracting("scheduleParticipationNo")
                .containsExactlyInAnyOrder(scheduleParticipation1.getId(), scheduleParticipation2.getId());
    }

}