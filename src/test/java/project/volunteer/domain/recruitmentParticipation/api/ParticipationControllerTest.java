package project.volunteer.domain.recruitmentParticipation.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitmentParticipation.api.dto.request.ParticipantAddParam;
import project.volunteer.domain.recruitmentParticipation.api.dto.request.ParticipantRemoveParam;
import project.volunteer.domain.recruitmentParticipation.repository.ParticipantRepository;
import project.volunteer.domain.recruitmentParticipation.domain.Participant;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.repository.RecruitmentRepository;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.*;
import project.volunteer.document.restdocs.config.RestDocsConfiguration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
class ParticipationControllerTest {
    @Autowired ObjectMapper objectMapper;
    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired RecruitmentRepository recruitmentRepository;
    @Autowired ParticipantRepository participantRepository;
    @Autowired RestDocumentationResultHandler restDocs;

    final String AUTHORIZATION_HEADER = "accessToken";
    private User loginUser;
    private User writer;
    private Recruitment saveRecruitment;
    @BeforeEach
    public void init(){
        //로그인 사용자 저장
        User login = User.createUser("pct_login", "pct_login", "pct_login", "pct_login", Gender.M, LocalDate.now(), "picture",
                true, true, true, Role.USER, "kakao", "pct_login", null);
        loginUser = userRepository.save(login);

        //작성자 저장
        User writerUser = User.createUser("pct_writer", "pct_writer", "pct_writer", "pct_writer", Gender.M, LocalDate.now(), "picture",
                true, true, true, Role.USER, "kakao", "pct_writer", null);
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
    }


    @Test
    @WithUserDetails(value = "pct_login", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void joinRequestVolunteeringTeam() throws Exception {
        //given & when
        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.put("/recruitment/{recruitmentNo}/join", saveRecruitment.getRecruitmentNo())
                .header(AUTHORIZATION_HEADER, "access Token")
                .contentType(MediaType.APPLICATION_JSON));

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
    @Test
    @WithUserDetails(value = "pct_login", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 봉사모집글_팀신청_실패_없는모집글() throws Exception {
        //given
        final Long recruitmentNo = Long.MAX_VALUE;

        //when & then
        mockMvc.perform(put("/recruitment/{recruitmentNo}/join", recruitmentNo)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Disabled
    @Test
    @WithUserDetails(value = "pct_login", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 봉사모집글_팀신청_실패_종료된모집글() throws Exception {
        //given
        Timetable newTime = Timetable.builder()
                .hourFormat(HourFormat.AM)
                .progressTime(3)
                .startTime(LocalTime.now())
                .startDay(LocalDate.of(2023, 5, 13))
                .endDay(LocalDate.of(2023, 5, 14)) //봉사 활동 종료
                .build();
        recruitmentRepository.findById(saveRecruitment.getRecruitmentNo()).get().setTimetable(newTime);
        final Long recruitmentNo = saveRecruitment.getRecruitmentNo();

        //when & then
        mockMvc.perform(put("/recruitment/{recruitmentNo}/join", recruitmentNo)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Disabled
    @Test
    @WithUserDetails(value = "pct_login", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 봉사모집글_팀신청_실패_중복신청() throws Exception {
        //given
        참여자_상태_등록(loginUser, ParticipantState.JOIN_REQUEST);
        final Long recruitmentNo = saveRecruitment.getRecruitmentNo();

        //when & then
        mockMvc.perform(put("/recruitment/{recruitmentNo}/join", recruitmentNo)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Disabled
    @Test
    @WithUserDetails(value = "pct_login", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 봉사모집글_팀신청_실패_참가가능인원초과() throws Exception {
        //given
        Recruitment findRecruitment = 저장된_모집글_가져오기();
        User saveUser1 = 사용자_등록("koo","koo","koo@naver.com");
        User saveUser2 = 사용자_등록("bon","bon","bon@naver.com");
        User saveUser3 = 사용자_등록("siK", "sik", "sik@naver.com");
        참여자_상태_등록(saveUser1, ParticipantState.JOIN_APPROVAL);
        findRecruitment.increaseTeamMember();
        참여자_상태_등록(saveUser2, ParticipantState.JOIN_APPROVAL);
        findRecruitment.increaseTeamMember();
        참여자_상태_등록(saveUser3, ParticipantState.JOIN_APPROVAL);
        findRecruitment.increaseTeamMember();


        //when & then
        mockMvc.perform(put("/recruitment/{recruitmentNo}/join", findRecruitment.getRecruitmentNo())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @WithUserDetails(value = "pct_login", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void joinCancellationVolunteeringTeam() throws Exception {
        //given
        참여자_상태_등록(loginUser, ParticipantState.JOIN_REQUEST);

        //when
        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.put("/recruitment/{recruitmentNo}/cancel", saveRecruitment.getRecruitmentNo())
                .header(AUTHORIZATION_HEADER, "access Token")
                .contentType(MediaType.APPLICATION_JSON)
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
    @Test
    @WithUserDetails(value = "pct_login", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 봉사모집글_팀신청취소_실패_잘못된상태() throws Exception {
        //given
        참여자_상태_등록(loginUser, ParticipantState.JOIN_APPROVAL);
        final Long recruitmentNo = saveRecruitment.getRecruitmentNo();
        //when & then
        mockMvc.perform(put("/recruitment/{recruitmentNo}/cancel", recruitmentNo)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @WithUserDetails(value = "pct_writer", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void joinApprovalVolunteeringTeam() throws Exception {
        //given
        참여자_상태_등록(loginUser, ParticipantState.JOIN_REQUEST);
        User saveUser = 사용자_등록("siKa", "sika", "sika@naver.com");
        참여자_상태_등록(saveUser, ParticipantState.JOIN_REQUEST);

        ParticipantAddParam dto = new ParticipantAddParam(List.of(loginUser.getUserNo(), saveUser.getUserNo()));

        //when
        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.put("/recruitment/{recruitmentNo}/approval", saveRecruitment.getRecruitmentNo())
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
                                        parameterWithName("recruitmentNo").description("봉사 모집글 고유키 PK")
                                ),
                                requestFields(
                                        fieldWithPath("recruitmentParticipationNos").type(JsonFieldType.ARRAY).description("봉사 모집글 참여자 고유키 PK")
                                )
                        )
                );
    }

    @Disabled
    @Test
    @WithUserDetails(value = "pct_login", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 봉사모집글_팀신청승인_실패_권한없음() throws Exception {
        //given
        final Long recruitmentNo = saveRecruitment.getRecruitmentNo();
        참여자_상태_등록(loginUser, ParticipantState.JOIN_REQUEST);
        ParticipantAddParam dto = new ParticipantAddParam(List.of(loginUser.getUserNo()));

        //when & then
        mockMvc.perform(put("/recruitment/{recruitmentNo}/approval", recruitmentNo)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dto)))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Disabled
    @Test
    @WithUserDetails(value = "pct_writer", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 봉사모집글_팀신청승인_실패_잘못된상태() throws Exception {
        //given
        final Long recruitmentNo = saveRecruitment.getRecruitmentNo();
        참여자_상태_등록(loginUser, ParticipantState.JOIN_CANCEL);
        ParticipantAddParam dto = new ParticipantAddParam(List.of(loginUser.getUserNo()));

        //when & then
        mockMvc.perform(put("/recruitment/{recruitmentNo}/approval", recruitmentNo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Disabled
    @Test
    @WithUserDetails(value = "pct_writer", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 봉사모집글_팀신청승인_실패_승인가능인원초과() throws Exception {
        //given
        //승인 가능인원 1명
        Recruitment findRecruitment = 저장된_모집글_가져오기();
        User saveUser1 = 사용자_등록("koo","koo","koo@naver.com");
        User saveUser2 = 사용자_등록("bon","bon","bon@naver.com");
        User saveUser3 = 사용자_등록("siK", "sik", "sik@naver.com");
        User saveUser4 = 사용자_등록("bog", "bog", "bog@naver.com");
        참여자_상태_등록(saveUser1, ParticipantState.JOIN_APPROVAL);
        findRecruitment.increaseTeamMember();
        참여자_상태_등록(saveUser2, ParticipantState.JOIN_APPROVAL);
        findRecruitment.increaseTeamMember();

        참여자_상태_등록(saveUser3, ParticipantState.JOIN_REQUEST);
        참여자_상태_등록(saveUser4, ParticipantState.JOIN_REQUEST);
        List<Long> requestNos = List.of(saveUser3.getUserNo(), saveUser4.getUserNo());

        ParticipantAddParam dto = new ParticipantAddParam(requestNos);
        //when & then
        mockMvc.perform(put("/recruitment/{recruitmentNo}/approval", findRecruitment.getRecruitmentNo())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @WithUserDetails(value = "pct_writer", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void kickVolunteeringTeam() throws Exception {
        //given
        final Long recruitmentNo = saveRecruitment.getRecruitmentNo();
        final Participant participant = 참여자_상태_등록(loginUser, ParticipantState.JOIN_APPROVAL);
        final ParticipantRemoveParam dto = new ParticipantRemoveParam(participant.getParticipantNo());

        //when
        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.put("/recruitment/{recruitmentNo}/kick", recruitmentNo)
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
                                        parameterWithName("recruitmentNo").description("봉사 모집글 고유키 PK")
                                ),
                                requestFields(
                                        fieldWithPath("recruitmentParticipationNo").type(JsonFieldType.NUMBER).description("봉사 모집글 참여자 고유키 PK")
                                )
                        )
                );
    }

    @Disabled
    @Test
    @WithUserDetails(value = "pct_writer", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 봉사모집글_팀원강제탈퇴_실패_잘못된상태() throws Exception {
        //given
        final Long recruitmentNo = saveRecruitment.getRecruitmentNo();
        final Participant participant = 참여자_상태_등록(loginUser, ParticipantState.JOIN_REQUEST);
        final ParticipantRemoveParam dto = new ParticipantRemoveParam(participant.getParticipantNo());

        //then & then
        mockMvc.perform(put("/recruitment/{recruitmentNo}/kick", recruitmentNo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    //TODO: Valid 테스트 분리 생각해보기
//    @Test
//    @WithUserDetails(value = "pct_writer", setupBefore = TestExecutionEvent.TEST_EXECUTION)
//    public void notNull_valid_테스트() throws Exception {
//        //given
//        final Long recruitmentNo = saveRecruitment.getRecruitmentNo();
//        ParticipantRemoveParam dto = new ParticipantRemoveParam(null);
//
//        //when & then
//        mockMvc.perform(put("/recruitment/{recruitmentNo}/kick", recruitmentNo)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(toJson(dto)))
//                .andExpect(status().isBadRequest())
//                .andDo(print());
//    }
//    @Test
//    @WithUserDetails(value = "pct_writer", setupBefore = TestExecutionEvent.TEST_EXECUTION)
//    public void notEmpty_valid_테스트() throws Exception {
//        //given
//        final Long recruitmentNo = saveRecruitment.getRecruitmentNo();
//        ParticipantAddParam dto = new ParticipantAddParam(new ArrayList<>());
//
//        //when & then
//        mockMvc.perform(put("/recruitment/{recruitmentNo}/approval", recruitmentNo)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(toJson(dto)))
//                .andExpect(status().isBadRequest())
//                .andDo(print());
//    }


    private User 사용자_등록(String id, String nickname, String email){
        User createUser = User.createUser(id, id, nickname, email, Gender.M, LocalDate.now(), "http://picture.jpg",
                true, true, true, Role.USER, "kakao", id, null);
        return userRepository.save(createUser);
    }
    private Participant 참여자_상태_등록(User user, ParticipantState state){
        Participant participant = Participant.createParticipant(saveRecruitment, user, state);
        return participantRepository.save(participant);
    }
    private Recruitment 저장된_모집글_가져오기(){
        return recruitmentRepository.findById(saveRecruitment.getRecruitmentNo()).get();
    }
    private <T> String toJson(T data) throws JsonProcessingException {
        return objectMapper.writeValueAsString(data);
    }
}