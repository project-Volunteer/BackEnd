package project.volunteer.domain.scheduleParticipation.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.image.application.ImageService;
import project.volunteer.domain.image.application.dto.ImageParam;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.recruitment.repository.RecruitmentRepository;
import project.volunteer.domain.recruitmentParticipation.domain.RecruitmentParticipation;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.domain.recruitmentParticipation.repository.RecruitmentParticipationRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.scheduleParticipation.api.dto.CancelApproval;
import project.volunteer.domain.scheduleParticipation.api.dto.CompleteApproval;
import project.volunteer.domain.scheduleParticipation.repository.ScheduleParticipationRepository;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;
import project.volunteer.domain.scheduleParticipation.service.ScheduleParticipationService;
import project.volunteer.domain.sehedule.repository.ScheduleRepository;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.domain.image.domain.Storage;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.*;
import project.volunteer.global.infra.s3.FileService;
import project.volunteer.global.test.WithMockCustomUser;
import project.volunteer.document.restdocs.config.RestDocsConfiguration;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
class ScheduleParticipantControllerTest {

    @Autowired MockMvc mockMvc;
    @PersistenceContext EntityManager em;
    @Autowired UserRepository userRepository;
    @Autowired RecruitmentRepository recruitmentRepository;
    @Autowired ScheduleRepository scheduleRepository;
    @Autowired
    RecruitmentParticipationRepository participantRepository;
    @Autowired ScheduleParticipationRepository scheduleParticipationRepository;
    @Autowired ScheduleParticipationService spService;
    @Autowired ImageService imageService;
    @Autowired ImageRepository imageRepository;
    @Autowired FileService fileService;
    @Autowired ObjectMapper objectMapper;
    @Autowired RestDocumentationResultHandler restDocs;

    final String AUTHORIZATION_HEADER = "accessToken";
    private User writer;
    private User loginUser;
    private Recruitment saveRecruitment;
    private Schedule saveSchedule;
    private List<Long> deleteImageNo = new ArrayList<>();
    @BeforeEach
    void init(){
        //작성자 저장
        User writerUser = User.createUser("spct1234", "spct1234", "spct1234", "spct1234", Gender.M, LocalDate.now(), "picture",
                true, true, true, Role.USER, "kakao", "spct1234", null);
        writer = userRepository.save(writerUser);

        //모집글 저장
        Recruitment createRecruitment = new Recruitment("title", "content", VolunteeringCategory.EDUCATION, VolunteeringType.REG,
                VolunteerType.ADULT, 9999,0,true, "unicef",
                new Address("111", "11", "test", "test"),
                new Coordinate(1.2F, 2.2F),
                new Timetable(LocalDate.of(2024, 1, 10), LocalDate.of(2024, 3, 3), HourFormat.AM,
                        LocalTime.now(), 10),
                0, 0, true, IsDeleted.N, writerUser);
        saveRecruitment = recruitmentRepository.save(createRecruitment);

        //일정 저장
        Schedule createSchedule = Schedule.create(
                saveRecruitment,
                new Timetable(
                        LocalDate.now().plusMonths(1), LocalDate.now().plusMonths(1),
                        HourFormat.AM, LocalTime.now(), 3),
                "content", "organization",
                Address.createAddress("11", "1111", "details", "fullName"), 3);
        saveSchedule = scheduleRepository.save(createSchedule);

        //로그인 유저 저장
        User login = User.createUser("spct_test", "spct_test", "spct_test", "spct_test", Gender.M, LocalDate.now(), "picture",
                true, true, true, Role.USER, "kakao", "spct_test", null);
        loginUser = userRepository.save(login);
    }
    @AfterEach
    public void deleteS3Image() { //S3에 테스트를 위해 저장한 이미지 삭제
        for(Long id : deleteImageNo){
            Image image = imageRepository.findById(id).get();
            Storage storage = image.getStorage();
            fileService.deleteFile(storage.getFakeImageName());
        }
    }

    @Test
    @DisplayName("일정 참가 신청에 성공하다.")
    @Transactional
    @WithUserDetails(value = "spct_test", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void participateSchedule() throws Exception {
        //given
        봉사모집글_팀원_등록(saveRecruitment, loginUser);

        //when
        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/join",
                        saveRecruitment.getRecruitmentNo(), saveSchedule.getScheduleNo())
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
                                        parameterWithName("recruitmentNo").description("봉사 모집글 고유키 PK"),
                                        parameterWithName("scheduleNo").description("봉사 일정 고유키 PK")
                                )
                        )
                );
    }

    @Disabled
    @Test
    @DisplayName("팀원이 아닌 사용자가 일정 참가 신청을 시도하다.")
    @Transactional
    @WithMockCustomUser(tempValue = "spct_forbidden")
    public void schedule_participate_forbidden() throws Exception {

        mockMvc.perform(put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/join", saveRecruitment.getRecruitmentNo(), saveSchedule.getScheduleNo()))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @DisplayName("일정 참가 취소 요청에 성공하다.")
    @Transactional
    @WithUserDetails(value = "spct_test", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void cancelParticipationSchedule() throws Exception {
        //given
        RecruitmentParticipation participant = 봉사모집글_팀원_등록(saveRecruitment, loginUser);
        일정_참여상태_추가(saveSchedule, participant, ParticipantState.PARTICIPATING);

        //when
        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/cancel", saveRecruitment.getRecruitmentNo(), saveSchedule.getScheduleNo())
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
                                        parameterWithName("recruitmentNo").description("봉사 모집글 고유키 PK"),
                                        parameterWithName("scheduleNo").description("봉사 일정 고유키 PK")
                                )
                        )
                );
    }


    @Test
    @DisplayName("일정 참가 취소 요청 승인에 성공하다.")
    @Transactional
    @WithUserDetails(value = "spct1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void approveCancellationSchedule() throws Exception {
        //given
        RecruitmentParticipation newParticipant = 봉사모집글_팀원_등록(saveRecruitment, loginUser);
        ScheduleParticipation newSp = 일정_참여상태_추가(saveSchedule, newParticipant, ParticipantState.PARTICIPATION_CANCEL);
        CancelApproval dto = new CancelApproval(newSp.getScheduleParticipationNo());

        //when
        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/cancelling",
                        saveRecruitment.getRecruitmentNo(), saveSchedule.getScheduleNo())
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, "access Token")
                .content(toJson(dto))
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
                                        parameterWithName("recruitmentNo").description("봉사 모집글 고유키 PK"),
                                        parameterWithName("scheduleNo").description("봉사 일정 고유키 PK")
                                ),
                                requestFields(
                                        fieldWithPath("no").type(JsonFieldType.NUMBER).description("취소 요청자 고유키 PK")
                                )
                        )
                );
    }

    @Disabled
    @Test
    @DisplayName("방장이 아닌 사용자가 일정 참가 취소 요청 승인을 시도하다.")
    @Transactional
    @WithUserDetails(value = "spct_test", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void cancelApprove_forbidden() throws Exception {
        //given
        RecruitmentParticipation newParticipant = 봉사모집글_팀원_등록(saveRecruitment, loginUser);
        ScheduleParticipation newSp = 일정_참여상태_추가(saveSchedule, newParticipant, ParticipantState.PARTICIPATION_CANCEL);
        CancelApproval dto = new CancelApproval(newSp.getScheduleParticipationNo());

        //when & then
        mockMvc.perform(put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/cancelling", saveRecruitment.getRecruitmentNo(), saveSchedule.getScheduleNo())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Disabled
    @Test
    @DisplayName("일정 참가 취소 요청 승인시 필수 파라미터를 누락하다.")
    @Transactional
    @WithUserDetails(value = "spct1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void cancelApprove_notValid() throws Exception {
        //given
        RecruitmentParticipation newParticipant = 봉사모집글_팀원_등록(saveRecruitment, loginUser);
        ScheduleParticipation newSp = 일정_참여상태_추가(saveSchedule, newParticipant, ParticipantState.PARTICIPATION_CANCEL);
        CancelApproval dto = new CancelApproval(null); //필수 파라미터 누락

        //when & then
        mockMvc.perform(put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/cancelling", saveRecruitment.getRecruitmentNo(), saveSchedule.getScheduleNo())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("일정 참가 완료 승인에 성공하다.")
    @Transactional
    @WithUserDetails(value = "spct1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void approvalCompletionSchedule() throws Exception {
        //given
        RecruitmentParticipation newParticipant = 봉사모집글_팀원_등록(saveRecruitment, loginUser);
        ScheduleParticipation newSp = 일정_참여상태_추가(saveSchedule, newParticipant, ParticipantState.PARTICIPATION_COMPLETE_UNAPPROVED);
        CompleteApproval dto = new CompleteApproval(List.of(newSp.getScheduleParticipationNo()));

        //when
        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/complete",
                        saveRecruitment.getRecruitmentNo(), saveSchedule.getScheduleNo())
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, "access Token")
                .content(toJson(dto))
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
                                        parameterWithName("recruitmentNo").description("봉사 모집글 고유키 PK"),
                                        parameterWithName("scheduleNo").description("봉사 일정 고유키 PK")
                                ),
                                requestFields(
                                        fieldWithPath("completedList").type(JsonFieldType.ARRAY).description("참가 완료자 고유키 PK")
                                )
                        )
                );
    }


    @Test
    @DisplayName("일정 참가자 리스트 조회에 성공하다.")
    @Transactional
    @WithUserDetails(value = "spct1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void findListParticipantSchedule() throws Exception {
        //given
        User test1 = 사용자_등록("test1", "test1", "test1@naver.com");
        RecruitmentParticipation participant1 = 봉사모집글_팀원_등록(saveRecruitment, test1);
        일정_참여상태_추가(saveSchedule, participant1, ParticipantState.PARTICIPATING);

        User test2 = 사용자_등록("test2", "test2", "test2@naver.com");
        RecruitmentParticipation participant2 = 봉사모집글_팀원_등록(saveRecruitment, test2);
        일정_참여상태_추가(saveSchedule, participant2, ParticipantState.PARTICIPATING);

        //when
        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.get("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/participating",
                        saveRecruitment.getRecruitmentNo(), saveSchedule.getScheduleNo())
                .header(AUTHORIZATION_HEADER, "access Token")
        );

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.participating[0].nickname").value(test1.getNickName()))
                .andExpect(jsonPath("$.participating[0].email").value(test1.getEmail()))
                .andExpect(jsonPath("$.participating[0].profile").value(test1.getPicture()))
                .andExpect(jsonPath("$.participating[1].nickname").value(test2.getNickName()))
                .andExpect(jsonPath("$.participating[1].email").value(test2.getEmail()))
                .andExpect(jsonPath("$.participating[1].profile").value(test2.getPicture()))
                .andDo(print())
                .andDo(
                        restDocs.document(
                                requestHeaders(
                                        headerWithName(AUTHORIZATION_HEADER).description("JWT Access Token")
                                ),
                                pathParameters(
                                        parameterWithName("recruitmentNo").description("봉사 모집글 고유키 PK"),
                                        parameterWithName("scheduleNo").description("봉사 일정 고유키 PK")
                                ),
                                responseFields(
                                        fieldWithPath("participating[].nickname").type(JsonFieldType.STRING).description("참여자 닉네임"),
                                        fieldWithPath("participating[].email").type(JsonFieldType.STRING).description("참여자 이메일"),
                                        fieldWithPath("participating[].profile").type(JsonFieldType.STRING).description("참여자 프로필 URL")
                                )
                        )
                );
    }

    @Test
    @DisplayName("일정 참여 취소 요청자 리스트 조회에 성공하다.")
    @Transactional
    @WithUserDetails(value = "spct1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void findListCancellationRequesterSchedule() throws Exception {
        //given
        User test1 = 사용자_등록("test1", "test1", "test1@naver.com");
        RecruitmentParticipation participant1 = 봉사모집글_팀원_등록(saveRecruitment, test1);
        ScheduleParticipation scheduleParticipation1 = 일정_참여상태_추가(saveSchedule, participant1,
                ParticipantState.PARTICIPATION_CANCEL);

        User test2 = 사용자_등록("test2", "test2", "test2@naver.com");
        RecruitmentParticipation participant2 = 봉사모집글_팀원_등록(saveRecruitment, test2);
        ScheduleParticipation scheduleParticipation2 =
                일정_참여상태_추가(saveSchedule, participant2, ParticipantState.PARTICIPATION_CANCEL);

        //when
        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.get("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/cancelling",
                        saveRecruitment.getRecruitmentNo(), saveSchedule.getScheduleNo())
                .header(AUTHORIZATION_HEADER, "access Token")
        );

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.cancelling[0].scheduleParticipationNo").value(scheduleParticipation1.getScheduleParticipationNo()))
                .andExpect(jsonPath("$.cancelling[0].nickname").value(test1.getNickName()))
                .andExpect(jsonPath("$.cancelling[0].email").value(test1.getEmail()))
                .andExpect(jsonPath("$.cancelling[0].profile").value(test1.getPicture()))
                .andExpect(jsonPath("$.cancelling[1].scheduleParticipationNo").value(scheduleParticipation2.getScheduleParticipationNo()))
                .andExpect(jsonPath("$.cancelling[1].nickname").value(test2.getNickName()))
                .andExpect(jsonPath("$.cancelling[1].email").value(test2.getEmail()))
                .andExpect(jsonPath("$.cancelling[1].profile").value(test2.getPicture()))
                .andDo(print())
                .andDo(
                        restDocs.document(
                                requestHeaders(
                                        headerWithName(AUTHORIZATION_HEADER).description("JWT Access Token")
                                ),
                                pathParameters(
                                        parameterWithName("recruitmentNo").description("봉사 모집글 고유키 PK"),
                                        parameterWithName("scheduleNo").description("봉사 일정 고유키 PK")
                                ),
                                responseFields(
                                        fieldWithPath("cancelling[].scheduleParticipationNo").type(JsonFieldType.NUMBER).description("취소 요청자 고유키 PK"),
                                        fieldWithPath("cancelling[].nickname").type(JsonFieldType.STRING).description("취소 요청자 닉네임"),
                                        fieldWithPath("cancelling[].email").type(JsonFieldType.STRING).description("취소 요청자 이메일"),
                                        fieldWithPath("cancelling[].profile").type(JsonFieldType.STRING).description("취소 요청자 프로필 URL")
                                )
                        )
                );
    }

    @Test
    @DisplayName("일정 참가 완료자 리스트 조회")
    @Transactional
    @WithUserDetails(value = "spct1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void findListParticipantCompletedSchedule() throws Exception {
        //given
        User test1 = 사용자_등록("test1", "test1", "test1@naver.com");
        RecruitmentParticipation participant1 = 봉사모집글_팀원_등록(saveRecruitment, test1);
        일정_참여상태_추가(saveSchedule, participant1, ParticipantState.PARTICIPATION_COMPLETE_APPROVAL);

        User test2 = 사용자_등록("test2", "test2", "test2@naver.com");
        Image uploadImage = 업로드_이미지_등록(test2.getUserNo(), RealWorkCode.USER);
        RecruitmentParticipation participant2 = 봉사모집글_팀원_등록(saveRecruitment, test2);
        일정_참여상태_추가(saveSchedule, participant2, ParticipantState.PARTICIPATION_COMPLETE_UNAPPROVED);

        //when
        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.get("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/completion", saveRecruitment.getRecruitmentNo(), saveSchedule.getScheduleNo())
                .header(AUTHORIZATION_HEADER, "access Token")
        );

        //then
        result.andExpect(status().isOk())
                /*.andExpect(jsonPath("$.done[0].no").value(test1.getUserNo()))
                .andExpect(jsonPath("$.done[0].nickname").value(test1.getNickName()))
                .andExpect(jsonPath("$.done[0].email").value(test1.getEmail()))
                .andExpect(jsonPath("$.done[0].profile").value(test1.getPicture()))
                .andExpect(jsonPath("$.done[0].status").value(StateResponse.COMPLETE_APPROVED.getId()))
                .andExpect(jsonPath("$.done[1].no").value(test2.getUserNo()))
                .andExpect(jsonPath("$.done[1].nickname").value(test2.getNickName()))
                .andExpect(jsonPath("$.done[1].email").value(test2.getEmail()))
                .andExpect(jsonPath("$.done[1].profile").value(uploadImage.getStorage().getImagePath()))
                .andExpect(jsonPath("$.done[1].status").value(StateResponse.COMPLETE_UNAPPROVED.getId()))*/
                .andDo(print())
                .andDo(
                        restDocs.document(
                                requestHeaders(
                                        headerWithName(AUTHORIZATION_HEADER).description("JWT Access Token")
                                ),
                                pathParameters(
                                        parameterWithName("recruitmentNo").description("봉사 모집글 고유키 PK"),
                                        parameterWithName("scheduleNo").description("봉사 일정 고유키 PK")
                                ),
                                responseFields(
                                        fieldWithPath("done[].scheduleParticipationNo").type(JsonFieldType.NUMBER).description("참가 완료자 고유키 PK"),
                                        fieldWithPath("done[].nickname").type(JsonFieldType.STRING).description("참가 완료자 닉네임"),
                                        fieldWithPath("done[].email").type(JsonFieldType.STRING).description("참가 완료자 이메일"),
                                        fieldWithPath("done[].profile").type(JsonFieldType.STRING).description("참가 완료자 프로필 URL"),
                                        fieldWithPath("done[].status").type(JsonFieldType.STRING).description("Code ClientState 참고바람.")
                                )
                        )
                );
    }

    private User 사용자_등록(String id, String nickname, String email){
        User createUser = User.createUser(id, id, nickname, email, Gender.M, LocalDate.now(), "http://picture.jpg",
                true, true, true, Role.USER, "kakao", id, null);
        return userRepository.save(createUser);
    }
    private RecruitmentParticipation 봉사모집글_팀원_등록(Recruitment recruitment, User user){
        RecruitmentParticipation participant = RecruitmentParticipation.createParticipant(recruitment, user, ParticipantState.JOIN_APPROVAL);
        return participantRepository.save(participant);
    }
    private ScheduleParticipation 일정_참여상태_추가(Schedule schedule, RecruitmentParticipation participant, ParticipantState state){
        ScheduleParticipation sp = ScheduleParticipation.createScheduleParticipation(saveSchedule, participant, state);
        return scheduleParticipationRepository.save(sp);
    }
    private <T> String toJson(T data) throws JsonProcessingException {
        return objectMapper.writeValueAsString(data);
    }
    private Image 업로드_이미지_등록(Long no, RealWorkCode code) throws IOException {
        ImageParam imageDto = ImageParam.builder()
                .code(code)
                .no(no)
                .uploadImage(getMockMultipartFile())
                .build();
        Long imageNo = imageService.addImage(imageDto);
        deleteImageNo.add(imageNo);

        return imageRepository.findById(imageNo).get();
    }
    private MockMultipartFile getMockMultipartFile() throws IOException {
        return new MockMultipartFile(
                "file", "file.PNG", "image/jpg", new FileInputStream("src/main/resources/static/test/file.PNG"));
    }
}