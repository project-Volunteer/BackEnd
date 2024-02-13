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
import project.volunteer.domain.participation.dao.ParticipantRepository;
import project.volunteer.domain.participation.domain.Participant;
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
    protected ParticipantRepository participantRepository;

    @Autowired
    protected ImageRepository imageRepository;


    protected String AUTHORIZATION_HEADER = "accessToken";
    protected String recruitmentOwnerAccessToken;
    protected String recruitmentTeamAccessToken;
    protected User ownerUser;
    protected User teamUser1;
    protected User teamUser2;
    protected Recruitment recruitment1;
    protected Recruitment recruitment2;
    protected Schedule schedule1;
    protected Schedule schedule2;
    protected Schedule schedule3;


    @BeforeEach
    void setUp() {
        saveBaseData();
        recruitmentOwnerAccessToken = jwtProvider.createAccessToken(ownerUser.getId());
        recruitmentTeamAccessToken = jwtProvider.createAccessToken(teamUser1.getId());
    }

    private void saveBaseData() {
        ownerUser = userRepository.save(
                new User("bonsik1234", "password", "bonsik", "test@email.com", Gender.M, LocalDate.of(1999, 7, 27),
                        "http://www...", true, true, true, Role.USER, "kakao", "kakao1234", null));

        recruitment1 = recruitmentRepository.save(
                new Recruitment("title", "content", VolunteeringCategory.ADMINSTRATION_ASSISTANCE, VolunteeringType.REG,
                        VolunteerType.TEENAGER, 9999,0,true, "unicef",
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
                new Recruitment("title", "content", VolunteeringCategory.CULTURAL_EVENT, VolunteeringType.REG,
                        VolunteerType.TEENAGER, 9999,0,true, "unicef",
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

        teamUser1 = userRepository.save(
                new User("soeun1234", "password", "soeun", "test@email.com", Gender.M, LocalDate.of(2001, 6, 27),
                        "http://www...", true, true, true, Role.USER, "kakao", "kakao1234", null));
        participantRepository.save(new Participant(recruitment1, teamUser1, ParticipantState.JOIN_APPROVAL));

        teamUser2 = userRepository.save(
                new User("chang1234", "password", "chang", "test@email.com", Gender.M, LocalDate.of(2005, 8, 27),
                        "http://www...", true, true, true, Role.USER, "kakao", "kakao1234", null));
        participantRepository.save(new Participant(recruitment1, teamUser2, ParticipantState.JOIN_REQUEST));

    }

    protected <T> String toJson(T data) throws JsonProcessingException {
        return objectMapper.writeValueAsString(data);
    }

}
