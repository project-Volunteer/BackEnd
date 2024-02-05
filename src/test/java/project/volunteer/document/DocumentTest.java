package project.volunteer.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.document.restdocs.config.RestDocsConfiguration;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
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
import project.volunteer.global.common.component.Timetable;
import project.volunteer.global.jwt.util.JwtProvider;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
@Transactional
public abstract class DocumentTest {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected RestDocumentationResultHandler restDocs;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected RecruitmentRepository recruitmentRepository;

    @Autowired
    protected ScheduleRepository scheduleRepository;


    protected String AUTHORIZATION_HEADER = "accessToken";
    protected String recruitmentOwnerAccessToken;
    protected User user;
    protected Recruitment recruitment;
    protected Schedule schedule;


    @BeforeEach
    void setUp() {

        saveBaseData();
        recruitmentOwnerAccessToken = jwtProvider.createAccessToken(user.getId());

    }

    private void saveBaseData() {
        user = userRepository.save(
                new User("bonsik1234", "password", "bonsik", "test@email.com", Gender.M, LocalDate.of(1999, 7, 27),
                        "http://www...", true, true, true, Role.USER, "kakao", "kakao1234", null));

        recruitment = recruitmentRepository.save(
                new Recruitment("title", "content", VolunteeringCategory.EDUCATION, VolunteeringType.REG,
                        VolunteerType.ADULT, 9999, true, "unicef",
                        new Address("111", "11", "test", "test"),
                        new Coordinate(1.2F, 2.2F),
                        new Timetable(LocalDate.of(2024, 1, 10), LocalDate.of(2024, 3, 3), HourFormat.AM,
                                LocalTime.now(), 10),
                        true, user));

        schedule = scheduleRepository.save(
                new Schedule(new Timetable(LocalDate.of(2024, 2, 10), LocalDate.of(2024, 2, 10), HourFormat.AM,
                        LocalTime.now(), 10),
                        "test", "test",
                        new Address("111", "11", "test", "test"),
                        100, IsDeleted.N, 8, recruitment)
        );

    }

    protected <T> String toJson(T data) throws JsonProcessingException {
        return objectMapper.writeValueAsString(data);
    }

}
