package project.volunteer;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Day;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Period;
import project.volunteer.domain.recruitment.domain.repeatPeriod.RepeatPeriod;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Week;
import project.volunteer.domain.recruitmentParticipation.domain.RecruitmentParticipation;
import project.volunteer.domain.recruitmentParticipation.repository.RecruitmentParticipationRepository;
import project.volunteer.domain.recruitment.repository.RecruitmentRepository;
import project.volunteer.domain.recruitment.repository.RepeatPeriodRepository;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;
import project.volunteer.domain.scheduleParticipation.repository.ScheduleParticipationRepository;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.domain.sehedule.repository.ScheduleRepository;
import project.volunteer.domain.image.dao.StorageRepository;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.Coordinate;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.common.component.Timetable;
import project.volunteer.global.jwt.util.JwtProvider;

@RequiredArgsConstructor
@Transactional
@Slf4j
public class DummyDataInit {
    private final UserRepository userRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final RepeatPeriodRepository repeatPeriodRepository;
    private final ImageRepository imageRepository;
    private final StorageRepository storageRepository;
    private final RecruitmentParticipationRepository participantRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleParticipationRepository scheduleParticipationRepository;

    private final JwtProvider jwtProvider;

    @EventListener(ApplicationReadyEvent.class)
    public void dummyDate() {
        // 사용자 데이터
        User dummyUser1 = userRepository.save(
                new User("dummy_user1", "dummy_user1", "dummy_user1", "dummy_user1@email.com", Gender.M,
                        LocalDate.of(1999, 7, 27),
                        "http://www...", true, true, true, Role.USER, "kakao", "kakao1234", null));
        User dummyUser2 = userRepository.save(
                new User("dummy_user2", "dummy_user2", "dummy_user2", "dummy_user2@email.com", Gender.M,
                        LocalDate.of(1999, 7, 27),
                        "http://www...", true, true, true, Role.USER, "kakao", "kakao1234", null));
        User dummyUser3 = userRepository.save(
                new User("dummy_user3", "dummy_user3", "dummy_user3", "dummy_user3@email.com", Gender.M,
                        LocalDate.of(1999, 7, 27),
                        "http://www...", true, true, true, Role.USER, "kakao", "kakao1234", null));
        User dummyUser4 = userRepository.save(
                new User("dummy_user4", "dummy_user4", "dummy_user4", "dummy_user4@email.com", Gender.M,
                        LocalDate.of(1999, 7, 27),
                        "http://www...", true, true, true, Role.USER, "kakao", "kakao1234", null));

        // 봉사 모집글 데이터
        Recruitment dummyRecruitment1 = recruitmentRepository.save(
                new Recruitment("dummy_recruitment1", "dummy_recruitment1",
                        VolunteeringCategory.ADMINSTRATION_ASSISTANCE, VolunteeringType.IRREG,
                        VolunteerType.TEENAGER, 100, 0, true, "unicef",
                        new Address("11", "1111", "test", "test"),
                        new Coordinate(1.2F, 2.2F),
                        new Timetable(LocalDate.of(2024, 1, 10), LocalDate.of(2024, 6, 1), HourFormat.AM,
                                LocalTime.of(13, 10, 0), 10),
                        0, 0, true, IsDeleted.N, dummyUser1));
        Recruitment dummyRecruitment2 = recruitmentRepository.save(
                new Recruitment("dummy_recruitment2", "dummy_recruitment2", VolunteeringCategory.CULTURAL_EVENT,
                        VolunteeringType.IRREG,
                        VolunteerType.TEENAGER, 100, 0, true, "unicef",
                        new Address("11", "1111", "test", "test"),
                        new Coordinate(1.2F, 2.2F),
                        new Timetable(LocalDate.of(2024, 1, 10), LocalDate.now(), HourFormat.AM,
                                LocalTime.now(), 10),
                        0, 0, true, IsDeleted.N, dummyUser1));
        Recruitment dummyRecruitment3 = recruitmentRepository.save(
                new Recruitment("dummy_recruitment3", "dummy_recruitment3", VolunteeringCategory.RESIDENTIAL_ENV,
                        VolunteeringType.IRREG,
                        VolunteerType.TEENAGER, 100, 0, true, "unicef",
                        new Address("11", "1111", "test", "test"),
                        new Coordinate(1.2F, 2.2F),
                        new Timetable(LocalDate.of(2024, 1, 10), LocalDate.now(), HourFormat.AM,
                                LocalTime.now(), 10),
                        0, 0, true, IsDeleted.N, dummyUser1));
        Recruitment dummyRecruitment4 = recruitmentRepository.save(
                new Recruitment("dummy_recruitment4", "dummy_recruitment4", VolunteeringCategory.HOMELESS_DOG,
                        VolunteeringType.IRREG,
                        VolunteerType.ALL, 100, 0, true, "unicef",
                        new Address("11", "1111", "test", "test"),
                        new Coordinate(1.2F, 2.2F),
                        new Timetable(LocalDate.of(2024, 1, 10), LocalDate.now(), HourFormat.AM,
                                LocalTime.now(), 10),
                        0, 0, true, IsDeleted.N, dummyUser1));
        Recruitment dummyRecruitment5 = recruitmentRepository.save(
                new Recruitment("dummy_recruitment5", "dummy_recruitment5", VolunteeringCategory.FRAM_VILLAGE,
                        VolunteeringType.IRREG,
                        VolunteerType.ALL, 100, 0, true, "unicef",
                        new Address("11", "1111", "test", "test"),
                        new Coordinate(1.2F, 2.2F),
                        new Timetable(LocalDate.of(2024, 1, 10), LocalDate.now(), HourFormat.AM,
                                LocalTime.now(), 10),
                        0, 0, true, IsDeleted.N, dummyUser1));
        Recruitment dummyRecruitment6 = recruitmentRepository.save(
                new Recruitment("dummy_recruitment6", "dummy_recruitment6", VolunteeringCategory.HEALTH_MEDICAL,
                        VolunteeringType.IRREG,
                        VolunteerType.ALL, 100, 0, true, "unicef",
                        new Address("11", "1111", "test", "test"),
                        new Coordinate(1.2F, 2.2F),
                        new Timetable(LocalDate.of(2024, 1, 10), LocalDate.now(), HourFormat.AM,
                                LocalTime.now(), 10),
                        0, 0, true, IsDeleted.N, dummyUser1));
        Recruitment dummyRecruitment7 = recruitmentRepository.save(
                new Recruitment("dummy_recruitment7", "dummy_recruitment7", VolunteeringCategory.EDUCATION,
                        VolunteeringType.IRREG,
                        VolunteerType.ALL, 100, 0, true, "unicef",
                        new Address("11", "1111", "test", "test"),
                        new Coordinate(1.2F, 2.2F),
                        new Timetable(LocalDate.of(2024, 1, 10), LocalDate.now(), HourFormat.AM,
                                LocalTime.now(), 10),
                        0, 0, true, IsDeleted.N, dummyUser1));
        Recruitment dummyRecruitment8 = recruitmentRepository.save(
                new Recruitment("dummy_recruitment8", "dummy_recruitment8", VolunteeringCategory.DISASTER,
                        VolunteeringType.IRREG,
                        VolunteerType.ADULT, 100, 0, true, "unicef",
                        new Address("11", "1111", "test", "test"),
                        new Coordinate(1.2F, 2.2F),
                        new Timetable(LocalDate.of(2024, 1, 10), LocalDate.now(), HourFormat.AM,
                                LocalTime.now(), 10),
                        0, 0, true, IsDeleted.N, dummyUser1));
        Recruitment dummyRecruitment9 = recruitmentRepository.save(
                new Recruitment("dummy_recruitment9", "dummy_recruitment9", VolunteeringCategory.FOREIGN_COUNTRY,
                        VolunteeringType.IRREG,
                        VolunteerType.ADULT, 100, 0, true, "unicef",
                        new Address("11", "1111", "test", "test"),
                        new Coordinate(1.2F, 2.2F),
                        new Timetable(LocalDate.of(2024, 1, 10), LocalDate.now(), HourFormat.AM,
                                LocalTime.now(), 10),
                        0, 0, true, IsDeleted.N, dummyUser1));

        // 봉사 모집글 주기 데이터
        RepeatPeriod dummyRepeatPeriod = new RepeatPeriod(Period.WEEK, Week.NONE, Day.SAT, dummyRecruitment1,
                IsDeleted.N);
        dummyRecruitment1.setRepeatPeriods(List.of(dummyRepeatPeriod));

        // 일정 데이터
        Schedule dummySchedule1 = scheduleRepository.save(new Schedule(
                new Timetable(LocalDate.of(2024, 6, 10), LocalDate.of(2024, 6, 10), HourFormat.AM,
                        LocalTime.of(13, 10, 0), 10),
                "content", "unicef",
                new Address("11", "1111", "test", "test"),
                10, IsDeleted.N, 0, dummyRecruitment1));

        // 모집글 팀원 데이터
        RecruitmentParticipation dummyRecruitmentParticipation1 = participantRepository.save(
                new RecruitmentParticipation(dummyRecruitment1, dummyUser2, ParticipantState.JOIN_REQUEST));
        RecruitmentParticipation dummyRecruitmentParticipation2 = participantRepository.save(
                new RecruitmentParticipation(dummyRecruitment1, dummyUser3, ParticipantState.JOIN_APPROVAL));
        RecruitmentParticipation dummyRecruitmentParticipation3 = participantRepository.save(
                new RecruitmentParticipation(dummyRecruitment1, dummyUser4, ParticipantState.JOIN_APPROVAL));
        dummyRecruitment1.increaseParticipationNum(2);

        // 일정 참여 데이터
        scheduleParticipationRepository.save(new ScheduleParticipation(dummySchedule1, dummyRecruitmentParticipation2, ParticipantState.PARTICIPATING));
        dummySchedule1.increaseParticipant();
        scheduleParticipationRepository.save(new ScheduleParticipation(dummySchedule1, dummyRecruitmentParticipation3, ParticipantState.PARTICIPATION_CANCEL));
        dummySchedule1.increaseParticipant();

        // jwt 토큰 테이터 (로그)
        String dummyAccessToken = jwtProvider.createAccessToken(dummyUser1.getId());
        log.info("[Dummy Access Token1] = {}", dummyAccessToken);
    }
}
