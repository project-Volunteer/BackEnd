package project.volunteer.domain.recruitment.api;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.image.application.ImageService;
import project.volunteer.domain.image.application.dto.ImageParam;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.dao.RepeatPeriodRepository;
import project.volunteer.domain.recruitment.domain.Period;
import project.volunteer.domain.recruitment.domain.RepeatPeriod;
import project.volunteer.domain.image.dao.StorageRepository;
import project.volunteer.global.common.component.*;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.domain.Day;
import project.volunteer.domain.recruitment.domain.Week;
import project.volunteer.domain.image.domain.Storage;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.infra.s3.FileService;
import project.volunteer.global.test.WithMockCustomUser;
import project.volunteer.restdocs.document.config.RestDocsConfiguration;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
class RecruitmentEditControllerTest {
    @Autowired UserRepository userRepository;
    @Autowired ImageService imageService;
    @Autowired StorageRepository storageRepository;
    @Autowired RecruitmentRepository recruitmentRepository;
    @Autowired RepeatPeriodRepository repeatPeriodRepository;
    @Autowired FileService fileService;
    @Autowired MockMvc mockMvc;
    @Autowired RestDocumentationResultHandler restDocs;

    final String AUTHORIZATION_HEADER = "accessToken";
    private Long saveRecruitmentNo;

    @BeforeEach
    public void setUp() throws IOException {
        //작성자 저장
        User writer = User.createUser("rctfe1234", "rctfe1234", "rctfe1234", "rctfe1234", Gender.M, LocalDate.now(), "picture",
                true, true, true, Role.USER, "kakao", "rctfe1234", null);
        userRepository.save(writer);

        //Embedded 값 세팅
        Address recruitmentAddress = Address.createAddress("1", "111", "test", "fullName");
        Timetable recruitmentTimetable = Timetable.createTimetable(LocalDate.now(), LocalDate.now(), HourFormat.AM, LocalTime.now(), 10);
        Coordinate coordinate = Coordinate.createCoordinate(3.2F, 3.2F);

        //봉사 모집글 저장
        Recruitment saveRecruitment = Recruitment.createRecruitment("test", "test", VolunteeringCategory.EDUCATION, VolunteeringType.REG,
                        VolunteerType.TEENAGER, 10, true, "test", recruitmentAddress, coordinate, recruitmentTimetable, true);
        saveRecruitment.setWriter(writer);
        saveRecruitmentNo = recruitmentRepository.save(saveRecruitment).getRecruitmentNo();

        //봉사 반복 주기 저장
        RepeatPeriod repeatPeriod = RepeatPeriod.createRepeatPeriod(Period.MONTH, Week.FIRST, Day.MON);
        repeatPeriod.setRecruitment(saveRecruitment);
        repeatPeriodRepository.save(repeatPeriod);

        //봉사 업로드 이미지 저장
        ImageParam staticImageDto = ImageParam.builder()
                .code(RealWorkCode.RECRUITMENT)
                .no(saveRecruitmentNo)
                .uploadImage(getMockMultipartFile())
                .build();
        imageService.addImage(staticImageDto);
    }
    @AfterEach
    void deleteUploadImage(){
        List<Storage> storages = storageRepository.findAll();
        storages.stream()
                .forEach(s -> fileService.deleteFile(s.getFakeImageName()));
    }

    @DisplayName("정기 모집글 삭제 테스트")
    @Test
    @WithUserDetails(value = "rctfe1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Rollback(value = false)
    public void deleteRecruitment() throws Exception {
        //given & when
        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.delete("/recruitment/{recruitmentNo}", saveRecruitmentNo)
                        .header(AUTHORIZATION_HEADER, "access Token")
                );

        //then
        result.andExpect(status().isOk())
                .andDo(print())
                .andDo(
                        restDocs.document(
                                requestHeaders(
                                        headerWithName(AUTHORIZATION_HEADER).description("JWT Access Token")
                                ),
                                pathParameters(
                                        parameterWithName("recruitmentNo").description("봉사 모집글 고유키 PK")
                                )
                        )
                );
    }

    @Disabled
    @DisplayName("정기 모집글 방장이 아닌 사용자가 삭제를 시도하다.")
    @Test
    @WithMockCustomUser()
    public void 정기모집글_삭제_실패_권한없음() throws Exception {
        mockMvc.perform(delete("/recruitment/{recruitmentNo}", saveRecruitmentNo))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Disabled
    @Test
    @WithUserDetails(value = "rctfe1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 정기모집글_삭제_실패_없는모집글() throws Exception {
        mockMvc.perform(delete("/recruitment/{recruitmentNo}", Long.MAX_VALUE))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    private MockMultipartFile getMockMultipartFile() throws IOException {
        return new MockMultipartFile(
                "file", "file.PNG", "image/jpg", new FileInputStream("src/main/resources/static/test/file.PNG"));
    }
}