package project.volunteer.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.document.restdocs.config.RestDocsConfiguration;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.image.domain.Storage;
import project.volunteer.domain.recruitmentParticipation.repository.RecruitmentParticipationRepository;
import project.volunteer.domain.recruitmentParticipation.domain.RecruitmentParticipation;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Day;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Period;
import project.volunteer.domain.recruitment.domain.repeatPeriod.RepeatPeriod;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Week;
import project.volunteer.domain.recruitment.repository.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.repository.RepeatPeriodRepository;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;
import project.volunteer.domain.scheduleParticipation.repository.ScheduleParticipationRepository;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.domain.sehedule.repository.ScheduleRepository;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.Coordinate;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.global.common.component.Timetable;
import project.volunteer.global.jwt.util.JwtProvider;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
@Transactional
@ActiveProfiles("test")
public abstract class DocumentTest {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected RestDocumentationResultHandler restDocs;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtProvider jwtProvider;

    @SpyBean
    protected Clock clock;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected RecruitmentRepository recruitmentRepository;

    @Autowired
    protected RepeatPeriodRepository repeatPeriodRepository;

    @Autowired
    protected ScheduleRepository scheduleRepository;

    @Autowired
    protected RecruitmentParticipationRepository recruitmentParticipationRepository;

    @Autowired
    protected ScheduleParticipationRepository scheduleParticipationRepository;

    @Autowired
    protected ImageRepository imageRepository;


    protected String AUTHORIZATION_HEADER = "accessToken";
    protected String recruitmentOwnerAccessToken;
    protected String recruitmentTeamAccessToken1;
    protected String recruitmentTeamAccessToken2;
    protected String recruitmentTeamAccessToken3;
    protected String recruitmentTeamAccessToken4;
    protected String recruitmentTeamAccessToken5;
    protected String recruitmentTeamAccessToken6;
    protected String loginUserAccessToken;
    protected User ownerUser;
    protected User teamUser1;
    protected User teamUser2;
    protected User teamUser3;
    protected User teamUser4;
    protected User teamUser5;
    protected User teamUser6;
    protected User teamUser7;
    protected User basicUser;
    protected Recruitment recruitment1;
    protected Recruitment recruitment2;
    protected RecruitmentParticipation recruitmentParticipation1;
    protected RecruitmentParticipation recruitmentParticipation2;
    protected RecruitmentParticipation recruitmentParticipation3;
    protected RecruitmentParticipation recruitmentParticipation4;
    protected RecruitmentParticipation recruitmentParticipation5;
    protected RecruitmentParticipation recruitmentParticipation6;
    protected RecruitmentParticipation recruitmentParticipation7;
    protected Schedule schedule1;
    protected Schedule schedule2;
    protected Schedule schedule3;
    protected ScheduleParticipation scheduleParticipation4;
    protected ScheduleParticipation scheduleParticipation5;
    protected ScheduleParticipation scheduleParticipation6;
    protected ScheduleParticipation scheduleParticipation7;


    @BeforeEach
    void setUp() {
        saveBaseData();
        recruitmentOwnerAccessToken = jwtProvider.createAccessToken(ownerUser.getId());
        recruitmentTeamAccessToken1 = jwtProvider.createAccessToken(teamUser1.getId());
        recruitmentTeamAccessToken2 = jwtProvider.createAccessToken(teamUser2.getId());
        recruitmentTeamAccessToken3 = jwtProvider.createAccessToken(teamUser3.getId());
        recruitmentTeamAccessToken4 = jwtProvider.createAccessToken(teamUser4.getId());
        recruitmentTeamAccessToken5 = jwtProvider.createAccessToken(teamUser5.getId());
        recruitmentTeamAccessToken6 = jwtProvider.createAccessToken(teamUser6.getId());

        loginUserAccessToken = jwtProvider.createAccessToken(basicUser.getId());
    }

    private void saveBaseData() {
        // 봉사 모집글 정보 저장
        ownerUser = userRepository.save(
                new User("bonsik1234", "password", "bonsik", "test@email.com", Gender.M, LocalDate.of(1999, 7, 27),
                        "http://www...", true, true, true, Role.USER, "kakao", "kakao1234", null));

        recruitment1 = recruitmentRepository.save(
                new Recruitment("title1", "content", VolunteeringCategory.ADMINSTRATION_ASSISTANCE,
                        VolunteeringType.REG,
                        VolunteerType.TEENAGER, 9999, 1, true, "unicef",
                        new Address("11", "1111", "test", "test"),
                        new Coordinate(1.2F, 2.2F),
                        new Timetable(LocalDate.of(2024, 1, 10), LocalDate.of(2024, 3, 3), HourFormat.AM,
                                LocalTime.now(), 10),
                        0, 0, true, IsDeleted.N, ownerUser));
        repeatPeriodRepository.save(new RepeatPeriod(Period.WEEK, Week.NONE, Day.MON, recruitment1, IsDeleted.N));
        repeatPeriodRepository.save(new RepeatPeriod(Period.WEEK, Week.NONE, Day.TUES, recruitment1, IsDeleted.N));
        Storage storage1 = new Storage("http://www.s3...", "test", "test", "png");
        Image image1 = new Image(RealWorkCode.RECRUITMENT, recruitment1.getRecruitmentNo());
        image1.setStorage(storage1);
        imageRepository.save(image1);

        recruitment2 = recruitmentRepository.save(
                new Recruitment("2title2", "content", VolunteeringCategory.CULTURAL_EVENT, VolunteeringType.REG,
                        VolunteerType.TEENAGER, 9999, 0, true, "unicef",
                        new Address("11", "1111", "test", "test"),
                        new Coordinate(1.2F, 2.2F),
                        new Timetable(LocalDate.of(2024, 2, 10), LocalDate.of(2024, 4, 3), HourFormat.AM,
                                LocalTime.now(), 10),
                        0, 0, true, IsDeleted.N, ownerUser));
        repeatPeriodRepository.save(new RepeatPeriod(Period.MONTH, Week.FIRST, Day.MON, recruitment1, IsDeleted.N));
        repeatPeriodRepository.save(new RepeatPeriod(Period.MONTH, Week.FIRST, Day.TUES, recruitment1, IsDeleted.N));
        Storage storage2 = new Storage("http://www.s3...", "test", "test", "png");
        Image image2 = new Image(RealWorkCode.RECRUITMENT, recruitment2.getRecruitmentNo());
        image2.setStorage(storage2);
        imageRepository.save(image2);

        // 일정 정보 저장
        schedule1 = scheduleRepository.save(
                new Schedule(new Timetable(LocalDate.of(2024, 2, 10), LocalDate.of(2024, 2, 10), HourFormat.AM,
                        LocalTime.now(), 10),
                        "test", "test",
                        new Address("111", "11", "test", "test"),
                        100, IsDeleted.N, 0, recruitment1)
        );

        schedule2 = scheduleRepository.save(
                new Schedule(new Timetable(LocalDate.of(2024, 2, 20), LocalDate.of(2024, 2, 20), HourFormat.AM,
                        LocalTime.now(), 10),
                        "test", "test",
                        new Address("111", "11", "test", "test"),
                        100, IsDeleted.N, 0, recruitment1)
        );

        schedule3 = scheduleRepository.save(
                new Schedule(new Timetable(LocalDate.of(2024, 2, 15), LocalDate.of(2024, 2, 15), HourFormat.AM,
                        LocalTime.now(), 10),
                        "test", "test",
                        new Address("111", "11", "test", "test"),
                        100, IsDeleted.N, 0, recruitment1)
        );

        // 봉사 모집글 참여 유저 저장
        teamUser1 = userRepository.save(
                new User("soeun1234", "password", "soeun", "test@email.com", Gender.M, LocalDate.of(2001, 6, 27),
                        "http://www...", true, true, true, Role.USER, "kakao", "kakao1234", null));
        recruitmentParticipation1 = recruitmentParticipationRepository.save(
                new RecruitmentParticipation(recruitment1, teamUser1, ParticipantState.JOIN_APPROVAL));
        recruitment1.increaseParticipationNum(1);

        teamUser2 = userRepository.save(
                new User("chang1234", "password", "chang", "test@email.com", Gender.M, LocalDate.of(2005, 8, 27),
                        "http://www...", true, true, true, Role.USER, "kakao", "kakao1234", null));
        recruitmentParticipation2 = recruitmentParticipationRepository.save(
                new RecruitmentParticipation(recruitment1, teamUser2, ParticipantState.JOIN_REQUEST));

        teamUser3 = userRepository.save(
                new User("mong1234", "password", "mong", "test@email.com", Gender.M, LocalDate.of(2005, 8, 27),
                        "http://www...", true, true, true, Role.USER, "kakao", "kakao1234", null));
        recruitmentParticipation3 = recruitmentParticipationRepository.save(
                new RecruitmentParticipation(recruitment1, teamUser3, ParticipantState.JOIN_REQUEST));

        teamUser4 = userRepository.save(
                new User("sik1234", "password", "sik", "test@email.com", Gender.M, LocalDate.of(2005, 8, 27),
                        "http://www...", true, true, true, Role.USER, "kakao", "kakao1234", null));
        recruitmentParticipation4 = recruitmentParticipationRepository.save(
                new RecruitmentParticipation(recruitment1, teamUser4, ParticipantState.JOIN_APPROVAL));
        recruitment1.increaseParticipationNum(1);

        teamUser5 = userRepository.save(
                new User("kkk1234", "password", "kkk", "test@email.com", Gender.M, LocalDate.of(2005, 8, 27),
                        "http://www...", true, true, true, Role.USER, "kakao", "kakao1234", null));
        recruitmentParticipation5 = recruitmentParticipationRepository.save(
                new RecruitmentParticipation(recruitment1, teamUser5, ParticipantState.JOIN_APPROVAL));
        recruitment1.increaseParticipationNum(1);

        teamUser6 = userRepository.save(
                new User("zibra1234", "password", "zibra", "test@email.com", Gender.M, LocalDate.of(2005, 8, 27),
                        "http://www...", true, true, true, Role.USER, "kakao", "kakao1234", null));
        recruitmentParticipation6 = recruitmentParticipationRepository.save(
                new RecruitmentParticipation(recruitment1, teamUser6, ParticipantState.JOIN_APPROVAL));
        recruitment1.increaseParticipationNum(1);

        teamUser7 = userRepository.save(
                new User("umm1234", "password", "umm", "test@email.com", Gender.M, LocalDate.of(2005, 8, 27),
                        "http://www...", true, true, true, Role.USER, "kakao", "kakao1234", null));
        recruitmentParticipation7 = recruitmentParticipationRepository.save(
                new RecruitmentParticipation(recruitment1, teamUser7, ParticipantState.JOIN_APPROVAL));
        recruitment1.increaseParticipationNum(1);

        // 봉사 일정 참여 유저 정보 저장
        scheduleParticipation4 = scheduleParticipationRepository.save(
                new ScheduleParticipation(schedule1, recruitmentParticipation4, ParticipantState.PARTICIPATING));
        schedule1.increaseParticipationNum(1);

        scheduleParticipation5 = scheduleParticipationRepository.save(
                new ScheduleParticipation(schedule1, recruitmentParticipation5, ParticipantState.PARTICIPATION_CANCEL));
        schedule1.increaseParticipationNum(1);

        scheduleParticipation6 = scheduleParticipationRepository.save(
                new ScheduleParticipation(schedule1, recruitmentParticipation6, ParticipantState.PARTICIPATION_COMPLETE_UNAPPROVED));
        schedule1.increaseParticipationNum(1);

        scheduleParticipation7 = scheduleParticipationRepository.save(
                new ScheduleParticipation(schedule1, recruitmentParticipation7, ParticipantState.PARTICIPATION_COMPLETE_APPROVAL));
        schedule1.increaseParticipationNum(1);

        // 일반 로그인 유저 정보 저장
        basicUser = userRepository.save(
                new User("user1234", "password", "user", "test@email.com", Gender.M, LocalDate.of(2001, 6, 27),
                        "http://www...", true, true, true, Role.USER, "kakao", "kakao1234", null));
    }

    protected <T> String toJson(T data) throws JsonProcessingException {
        return objectMapper.writeValueAsString(data);
    }

}
