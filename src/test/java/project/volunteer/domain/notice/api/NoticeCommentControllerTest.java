package project.volunteer.domain.notice.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.notice.dao.NoticeRepository;
import project.volunteer.domain.notice.domain.Notice;
import project.volunteer.domain.recruitmentParticipation.domain.RecruitmentParticipation;
import project.volunteer.domain.recruitmentParticipation.repository.ParticipantRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.repository.RecruitmentRepository;
import project.volunteer.domain.reply.dao.ReplyRepository;
import project.volunteer.domain.reply.domain.Reply;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.*;
import project.volunteer.global.common.dto.CommentContentParam;
import project.volunteer.document.restdocs.config.RestDocsConfiguration;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
public class NoticeCommentControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired RecruitmentRepository recruitmentRepository;
    @Autowired NoticeRepository noticeRepository;
    @Autowired ParticipantRepository participantRepository;
    @Autowired ReplyRepository replyRepository;
    @Autowired RestDocumentationResultHandler restDocs;

    final String AUTHORIZATION_HEADER = "accessToken";
    final String adminUserName = "nrt_admin";
    final String teamAndReplyWriterName = "nrt_team_user";
    Recruitment saveRecruitment;
    Notice saveNotice;
    User replyWriter;
    @BeforeEach
    void init(){
        //작성자 저장
        User writerUser = User.createUser(adminUserName, "password", adminUserName, "email", Gender.M, LocalDate.now(), "picture",
                true, true, true, Role.USER, "kakao", adminUserName, null);
        userRepository.save(writerUser);

        //모집글 저장
        Recruitment createRecruitment = new Recruitment("title", "content", VolunteeringCategory.EDUCATION, VolunteeringType.REG,
                VolunteerType.ADULT, 9999,0,true, "unicef",
                new Address("111", "11", "test", "test"),
                new Coordinate(1.2F, 2.2F),
                new Timetable(LocalDate.of(2024, 1, 10), LocalDate.of(2024, 3, 3), HourFormat.AM,
                        LocalTime.now(), 10),
                0, 0, true, IsDeleted.N, writerUser);
        saveRecruitment = recruitmentRepository.save(createRecruitment);

        //공지사항 저장
        Notice createNotice = Notice.createNotice("test notice");
        createNotice.setRecruitment(saveRecruitment);
        saveNotice = noticeRepository.save(createNotice);

        //봉사 모집글 팀 사용자 추가 및 댓글 작성자
        User teamUser = User.createUser(teamAndReplyWriterName, "password", teamAndReplyWriterName, "email", Gender.M, LocalDate.now(), "picture",
                true, true, true, Role.USER, "kakao", teamAndReplyWriterName, null);
        replyWriter = userRepository.save(teamUser);
        RecruitmentParticipation participant = RecruitmentParticipation.createParticipant(saveRecruitment, replyWriter, ParticipantState.JOIN_APPROVAL);
        participantRepository.save(participant);
    }

    @Test
    @DisplayName("봉사 공지사항 댓글 작성에 성공하다.")
    @WithUserDetails(value = teamAndReplyWriterName, setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void saveCommentNotice() throws Exception {
        //given
        final CommentContentParam dto = new CommentContentParam("test comment");

        //when
        ResultActions result = mockMvc.perform(post("/recruitment/{recruitmentNo}/notice/{noticeNo}/comment", saveRecruitment.getRecruitmentNo(), saveNotice.getNoticeNo())
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, "access Token")
                .content(toJson(dto))
        );

        //then
        result.andExpect(status().isCreated())
                .andDo(print())
                .andDo(
                        restDocs.document(
                                requestHeaders(
                                        headerWithName(AUTHORIZATION_HEADER).description("JWT Access Token")
                                ),
                                pathParameters(
                                        parameterWithName("recruitmentNo").description("봉사 모집글 고유키 PK"),
                                        parameterWithName("noticeNo").description("봉사 공지사항 고유키 PK")
                                ),
                                requestFields(
                                        fieldWithPath("content").type(JsonFieldType.STRING)
                                                .attributes(key("constraints").value("1이상 255이하")).description("댓글 내용")
                                )
                        )
                );
    }
    @Test
    @DisplayName("봉사 공지사항 대댓글 작성에 성공하다.")
    @WithUserDetails(value = teamAndReplyWriterName, setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void saveCommentReplyNotice() throws Exception {
        //given
        final CommentContentParam dto = new CommentContentParam("test comment reply");
        Long commentNo = 댓글_추가(saveNotice.getNoticeNo(), "test reply", replyWriter);

        //when
        ResultActions result = mockMvc.perform(post("/recruitment/{recruitmentNo}/notice/{noticeNo}/comment/{parentNo}/reply",
                saveRecruitment.getRecruitmentNo(), saveNotice.getNoticeNo(), commentNo)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, "access Token")
                .content(toJson(dto))
        );

        //then
        result.andExpect(status().isCreated())
                .andDo(print())
                .andDo(
                        restDocs.document(
                                requestHeaders(
                                        headerWithName(AUTHORIZATION_HEADER).description("JWT Access Token")
                                ),
                                pathParameters(
                                        parameterWithName("recruitmentNo").description("봉사 모집글 고유키 PK"),
                                        parameterWithName("noticeNo").description("봉사 공지사항 고유키 PK"),
                                        parameterWithName("parentNo").description("댓글 고유키 PK")
                                ),
                                requestFields(
                                        fieldWithPath("content").type(JsonFieldType.STRING)
                                                .attributes(key("constraints").value("1이상 255이하")).description("대댓글 내용")
                                )
                        )
                );
    }
    @Test
    @DisplayName("봉사 공지사항 댓글 수정에 성공하다.")
    @WithUserDetails(value = teamAndReplyWriterName, setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void editCommentNotice() throws Exception {
        //given
        final CommentContentParam dto = new CommentContentParam("test edit comment");
        Long commentNo = 댓글_추가(saveNotice.getNoticeNo(), "test comment", replyWriter);

        //when
        ResultActions result = mockMvc.perform(put("/recruitment/{recruitmentNo}/notice/{noticeNo}/comment/{replyNo}",
                saveRecruitment.getRecruitmentNo(), saveNotice.getNoticeNo(), commentNo)
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
                                        parameterWithName("noticeNo").description("봉사 공지사항 고유키 PK"),
                                        parameterWithName("replyNo").description("댓글/대댓글 고유키 PK")
                                ),
                                requestFields(
                                        fieldWithPath("content").type(JsonFieldType.STRING)
                                                .attributes(key("constraints").value("1이상 255이하")).description("댓글/대댓글 수정 내용")
                                )
                        )
                );
    }

    @Disabled
    @Test
    @DisplayName("봉사 공지사항 댓글 작성자가 아닌 다른 사용자가 수정을 시도하다.")
    @WithUserDetails(value = adminUserName, setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void noticeReplyEdit_forbidden() throws Exception {
        //given
        final CommentContentParam dto = new CommentContentParam("test edit comment");
        Long commentNo = 댓글_추가(saveNotice.getNoticeNo(), "test comment", replyWriter);

        //when & then
        mockMvc.perform(put("/recruitment/{recruitmentNo}/notice/{noticeNo}/comment/{replyNo}",
                        saveRecruitment.getRecruitmentNo(), saveNotice.getNoticeNo(), commentNo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @DisplayName("봉사 공지사항 댓글 삭제에 성공하다.")
    @WithUserDetails(value = teamAndReplyWriterName, setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void deleteCommentNotice() throws Exception {
        //given
        Long commentNo = 댓글_추가(saveNotice.getNoticeNo(), "test comment", replyWriter);

        //when
        ResultActions result = mockMvc.perform(delete("/recruitment/{recruitmentNo}/notice/{noticeNo}/comment/{replyNo}",
                saveRecruitment.getRecruitmentNo(), saveNotice.getNoticeNo(), commentNo)
                .contentType(MediaType.APPLICATION_JSON)
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
                                        parameterWithName("noticeNo").description("봉사 공지사항 고유키 PK"),
                                        parameterWithName("replyNo").description("댓글/대댓글 고유키 PK")
                                )
                        )
                );
    }


    private Long 댓글_추가(Long no, String content, User writer){
        Reply comment = Reply.createComment(RealWorkCode.NOTICE, no, content);
        comment.setWriter(writer);
        return replyRepository.save(comment).getReplyNo();
    }
    private <T> String toJson(T data) throws JsonProcessingException {
        return objectMapper.writeValueAsString(data);
    }
}
