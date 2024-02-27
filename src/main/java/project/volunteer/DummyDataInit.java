package project.volunteer;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.participation.dao.ParticipantRepository;
import project.volunteer.domain.recruitment.repository.RecruitmentRepository;
import project.volunteer.domain.recruitment.repository.RepeatPeriodRepository;
import project.volunteer.domain.scheduleParticipation.repository.ScheduleParticipationRepository;
import project.volunteer.domain.sehedule.repository.ScheduleRepository;
import project.volunteer.domain.image.dao.StorageRepository;
import project.volunteer.domain.user.dao.UserRepository;

@RequiredArgsConstructor
@Transactional
public class DummyDataInit {

    private final UserRepository userRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final RepeatPeriodRepository repeatPeriodRepository;
    private final ImageRepository imageRepository;
    private final StorageRepository storageRepository;
    private final ParticipantRepository participantRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleParticipationRepository scheduleParticipationRepository;

    /**
     * <더미데이터 요약></더미데이터>
     *
     * 사용자 10명(이미지 없음)
     * 단기 봉사 모집글 9개(봉사 카테고리 별 1개)
     * 정적 이미지 9개(봉사 모집글 별)
     * 팀원 9명(신청 4명, 승인 5명, 봉사 모집글1)
     * 일정 2개(봉사 모집글1)
     * 일정 참여자 5명(봉사 모집글1, 승인5명)
     */
    @EventListener(ApplicationReadyEvent.class)
    public void dummyDate(){
        //사용자 더미 데이터 세팅
//        final User dummyUser1 = User.createUser("11111", "11111", "rnqhstlr", "rnqhstlr@naver.com", Gender.M,
//                LocalDate.of(1999,7,27), null,true, true, true, Role.USER,
//                "kakao", "11111", null);
//        final User dummyUser2 = User.createUser("22222", "22222", "didthdms", "didthdms@naver.com", Gender.W,
//                LocalDate.of(2000,6,27), null,true, true, true, Role.USER,
//                "kakao", "22222", null);
//        final User dummyUser3 = User.createUser("33333", "33333", "skatmdduq", "skqtmdduq@naver.com", Gender.M,
//                LocalDate.of(1996,7,27), null,true, true, true, Role.USER,
//                "kakao", "33333", null);
//        final User dummyUser4 = User.createUser("44444", "44444", "rlackdgks", "rlackdgks@naver.com", Gender.M,
//                LocalDate.of(1989,7,27), null,true, true, true, Role.USER,
//                "kakao", "44444", null);
//        final User dummyUser5 = User.createUser("55555", "55555", "rlarlwn", "rlarlwn@naver.com", Gender.W,
//                LocalDate.of(1979,7,27), null,true, true, true, Role.USER,
//                "kakao", "55555", null);
//        final User dummyUser6 = User.createUser("66666", "66666", "qkrqudtjs", "qkrqudtjs@naver.com", Gender.M,
//                LocalDate.of(2001,7,27), null,true, true, true, Role.USER,
//                "kakao", "66666", null);
//        final User dummyUser7 = User.createUser("77777", "77777", "dlckdguq", "dlckdguq@naver.com", Gender.M,
//                LocalDate.of(1999,8,27), null,true, true, true, Role.USER,
//                "kakao", "77777", null);
//        final User dummyUser8 = User.createUser("88888", "88888", "rlaehgus", "rlaehgus@naver.com", Gender.M,
//                LocalDate.of(1999,3,13), null,true, true, true, Role.USER,
//                "kakao", "88888", null);
//        final User dummyUser9 = User.createUser("99999", "99999", "chltjrdnjs", "chltjrdnjs@naver.com", Gender.M,
//                LocalDate.of(1999,1,17), null,true, true, true, Role.USER,
//                "kakao", "99999", null);
//        final User dummyUser10 = User.createUser("10000", "10000", "dlwngus", "dlwngus@naver.com", Gender.M,
//                LocalDate.of(1999,8,13), null,true, true, true, Role.USER,
//                "kakao", "10000", null);
//        userRepository.save(dummyUser1);
//        userRepository.save(dummyUser2);
//        userRepository.save(dummyUser3);
//        userRepository.save(dummyUser4);
//        userRepository.save(dummyUser5);
//        userRepository.save(dummyUser6);
//        userRepository.save(dummyUser7);
//        userRepository.save(dummyUser8);
//        userRepository.save(dummyUser9);
//        userRepository.save(dummyUser10);
//
//        //단기 모집글 더미 데이터 세팅
//        Recruitment dummyRecruitment1= Recruitment.create("recruitment1", "recruitment1", VolunteeringCategory.ADMINSTRATION_ASSISTANCE,
//                VolunteeringType.IRREG, VolunteerType.TEENAGER, 15, true, "organization1",
//                Address.createAddress("11", "11010", "details1", "fullName1"),
//                Coordinate.createCoordinate(3.2F, 3.2F),
//                new Timetable(LocalDate.now(), LocalDate.now().plusMonths(9), HourFormat.AM, LocalTime.now(), 10), true);
//        dummyRecruitment1.setWriter(dummyUser1);
//        Recruitment dummyRecruitment2= Recruitment.create("recruitment2", "recruitment2", VolunteeringCategory.CULTURAL_EVENT,
//                VolunteeringType.IRREG, VolunteerType.TEENAGER, 10, true, "organization2",
//                Address.createAddress("11", "11020", "details2", "fullName2"),
//                Coordinate.createCoordinate(3.2F, 3.2F),
//                new Timetable(LocalDate.now(), LocalDate.now().plusMonths(2), HourFormat.AM, LocalTime.now(), 10), true);
//        dummyRecruitment2.setWriter(dummyUser2);
//        Recruitment dummyRecruitment3= Recruitment.create("recruitment3", "recruitment3", VolunteeringCategory.RESIDENTIAL_ENV,
//                VolunteeringType.IRREG, VolunteerType.TEENAGER, 10, true, "organization3",
//                Address.createAddress("11", "11030", "details3", "fullName3"),
//                Coordinate.createCoordinate(3.2F, 3.2F),
//                new Timetable(LocalDate.now(), LocalDate.now().plusMonths(3), HourFormat.AM, LocalTime.now(), 10), true);
//        dummyRecruitment3.setWriter(dummyUser3);
//        Recruitment dummyRecruitment4 = Recruitment.create("recruitment4", "recruitment4", VolunteeringCategory.HOMELESS_DOG,
//                VolunteeringType.IRREG, VolunteerType.ADULT, 10, true, "organization4",
//                Address.createAddress("11", "11040", "detail4", "fullName4"),
//                Coordinate.createCoordinate(3.2F, 3.2F),
//                new Timetable(LocalDate.now(), LocalDate.now().plusMonths(4), HourFormat.AM, LocalTime.now(), 10), true);
//        dummyRecruitment4.setWriter(dummyUser4);
//        Recruitment dummyRecruitment5= Recruitment.create("recruitment5", "recruitment5", VolunteeringCategory.FRAM_VILLAGE,
//                VolunteeringType.IRREG, VolunteerType.ADULT, 10, true, "organization5",
//                Address.createAddress("11", "11050", "details5", "fullName5"),
//                Coordinate.createCoordinate(3.2F, 3.2F),
//                new Timetable(LocalDate.now(), LocalDate.now().plusMonths(5), HourFormat.AM, LocalTime.now(), 10), true);
//        dummyRecruitment5.setWriter(dummyUser5);
//        Recruitment dummyRecruitment6= Recruitment.create("recruitment6", "recruitment6", VolunteeringCategory.HEALTH_MEDICAL,
//                VolunteeringType.IRREG, VolunteerType.ADULT, 10, true, "organization6",
//                Address.createAddress("11", "11060", "details6", "fullName6"),
//                Coordinate.createCoordinate(3.2F, 3.2F),
//                new Timetable(LocalDate.now(), LocalDate.now().plusMonths(6), HourFormat.AM, LocalTime.now(), 10), true);
//        dummyRecruitment6.setWriter(dummyUser6);
//        Recruitment dummyRecruitment7= Recruitment.create("recruitment7", "recruitment7", VolunteeringCategory.EDUCATION,
//                VolunteeringType.IRREG, VolunteerType.ALL, 10, true, "organization7",
//                Address.createAddress("11", "11070", "details7", "fullName7"),
//                Coordinate.createCoordinate(3.2F, 3.2F),
//                new Timetable(LocalDate.now(), LocalDate.now().plusMonths(7), HourFormat.AM, LocalTime.now(), 10), true);
//        dummyRecruitment7.setWriter(dummyUser7);
//        Recruitment dummyRecruitment8= Recruitment.create("recruitment8", "recruitment8", VolunteeringCategory.DISASTER,
//                VolunteeringType.IRREG, VolunteerType.ALL, 10, true, "organization8",
//                Address.createAddress("11", "11080", "details8", "fullName8"),
//                Coordinate.createCoordinate(3.2F, 3.2F),
//                new Timetable(LocalDate.now(), LocalDate.now().plusMonths(8), HourFormat.AM, LocalTime.now(), 10), true);
//        dummyRecruitment8.setWriter(dummyUser8);
//        Recruitment dummyRecruitment9= Recruitment.create("recruitment9", "recruitment9", VolunteeringCategory.FOREIGN_COUNTRY,
//                VolunteeringType.IRREG, VolunteerType.ALL, 10, true, "organization9",
//                Address.createAddress("11", "11090", "details9", "fullName9"),
//                Coordinate.createCoordinate(3.2F, 3.2F),
//                new Timetable(LocalDate.now(), LocalDate.now().plusMonths(8), HourFormat.AM, LocalTime.now(), 10), true);
//        dummyRecruitment9.setWriter(dummyUser9);
//        recruitmentRepository.save(dummyRecruitment1);
//        recruitmentRepository.save(dummyRecruitment2);
//        recruitmentRepository.save(dummyRecruitment3);
//        recruitmentRepository.save(dummyRecruitment4);
//        recruitmentRepository.save(dummyRecruitment5);
//        recruitmentRepository.save(dummyRecruitment6);
//        recruitmentRepository.save(dummyRecruitment7);
//        recruitmentRepository.save(dummyRecruitment8);
//        recruitmentRepository.save(dummyRecruitment9);
//
//        //팀원 더미데이터 세팅
//        Participant dummyParticipant1 = Participant.createParticipant(dummyRecruitment1, dummyUser2, ParticipantState.JOIN_REQUEST);
//        Participant dummyParticipant2 = Participant.createParticipant(dummyRecruitment1, dummyUser3, ParticipantState.JOIN_REQUEST);
//        Participant dummyParticipant3 = Participant.createParticipant(dummyRecruitment1, dummyUser4, ParticipantState.JOIN_REQUEST);
//        Participant dummyParticipant4 = Participant.createParticipant(dummyRecruitment1, dummyUser5, ParticipantState.JOIN_REQUEST);
//        Participant dummyParticipant5 = Participant.createParticipant(dummyRecruitment1, dummyUser6, ParticipantState.JOIN_APPROVAL);
//        Participant dummyParticipant6 = Participant.createParticipant(dummyRecruitment1, dummyUser7, ParticipantState.JOIN_APPROVAL);
//        Participant dummyParticipant7 = Participant.createParticipant(dummyRecruitment1, dummyUser8, ParticipantState.JOIN_APPROVAL);
//        Participant dummyParticipant8 = Participant.createParticipant(dummyRecruitment1, dummyUser9, ParticipantState.JOIN_APPROVAL);
//        Participant dummyParticipant9 = Participant.createParticipant(dummyRecruitment1, dummyUser10, ParticipantState.JOIN_APPROVAL);
//        participantRepository.save(dummyParticipant1);
//        participantRepository.save(dummyParticipant2);
//        participantRepository.save(dummyParticipant3);
//        participantRepository.save(dummyParticipant4);
//
//        participantRepository.save(dummyParticipant5);
//        participantRepository.save(dummyParticipant6);
//        participantRepository.save(dummyParticipant7);
//        participantRepository.save(dummyParticipant8);
//        participantRepository.save(dummyParticipant9);
//
//        //스케줄 더미데이터 세팅
//        Schedule dummySchedule1 = Schedule.create(dummyRecruitment1, new Timetable(
//                        LocalDate.now().plusMonths(2), LocalDate.now().plusMonths(2), HourFormat.AM, LocalTime.now(), 5),
//                "content1", "organization1", Address.createAddress("11", "11010", "details1", "fullName1"), 10);
//        Schedule dummySchedule2 = Schedule.create(dummyRecruitment1, new Timetable(
//                        LocalDate.now().plusMonths(3), LocalDate.now().plusMonths(3), HourFormat.AM, LocalTime.now(), 5),
//                "content2", "organization1", Address.createAddress("11", "11010", "details1", "fullName2"), 10);
//        scheduleRepository.save(dummySchedule1);
//        scheduleRepository.save(dummySchedule2);
//
//        //일정 참여자 더미 데이터 세팅
//        ScheduleParticipation dummySp1 = ScheduleParticipation.createScheduleParticipation(dummySchedule1, dummyParticipant5, ParticipantState.PARTICIPATING);
//        ScheduleParticipation dummySp2 = ScheduleParticipation.createScheduleParticipation(dummySchedule1, dummyParticipant6, ParticipantState.PARTICIPATING);
//        ScheduleParticipation dummySp3 = ScheduleParticipation.createScheduleParticipation(dummySchedule1, dummyParticipant7, ParticipantState.PARTICIPATING);
//        ScheduleParticipation dummySp4 = ScheduleParticipation.createScheduleParticipation(dummySchedule1, dummyParticipant8, ParticipantState.PARTICIPATING);
//        ScheduleParticipation dummySp5 = ScheduleParticipation.createScheduleParticipation(dummySchedule1, dummyParticipant9, ParticipantState.PARTICIPATING);
//        scheduleParticipationRepository.save(dummySp1);
//        scheduleParticipationRepository.save(dummySp2);
//        scheduleParticipationRepository.save(dummySp3);
//        scheduleParticipationRepository.save(dummySp4);
//        scheduleParticipationRepository.save(dummySp5);

    }
}
