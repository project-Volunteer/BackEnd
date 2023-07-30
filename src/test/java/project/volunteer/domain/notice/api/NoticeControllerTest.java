package project.volunteer.domain.notice.api;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.confirmation.dao.ConfirmationRepository;
import project.volunteer.domain.confirmation.domain.Confirmation;
import project.volunteer.domain.notice.api.dto.request.NoticeAdd;
import project.volunteer.domain.notice.api.dto.request.NoticeEdit;
import project.volunteer.domain.notice.dao.NoticeRepository;
import project.volunteer.domain.notice.domain.Notice;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.reply.dao.ReplyRepository;
import project.volunteer.domain.reply.domain.Reply;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.*;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class NoticeControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired RecruitmentRepository recruitmentRepository;
    @Autowired NoticeRepository noticeRepository;
    @Autowired ConfirmationRepository confirmationRepository;
    @Autowired ReplyRepository replyRepository;

    User writer;
    Recruitment saveRecruitment;
    @BeforeEach
    void init(){
        //작성자 저장
        User writerUser = User.createUser("nct_1234", "nct_1234", "nct_1234", "nct_1234", Gender.M, LocalDate.now(), "picture",
                true, true, true, Role.USER, "kakao", "nct_1234", null);
        writer = userRepository.save(writerUser);

        //모집글 저장
        Recruitment createRecruitment = Recruitment.createRecruitment("title", "content", VolunteeringCategory.CULTURAL_EVENT, VolunteeringType.IRREG,
                VolunteerType.TEENAGER, 3, true, "organization",
                Address.createAddress("11", "1111","details"), Coordinate.createCoordinate(3.2F, 3.2F),
                Timetable.createTimetable(LocalDate.now(), LocalDate.now().plusMonths(3), HourFormat.AM, LocalTime.now(), 3), true);
        createRecruitment.setWriter(writer);
        saveRecruitment = recruitmentRepository.save(createRecruitment);
    }

    @Test
    @DisplayName("봉사 공지사항 등록 요청에 성공하다.")
    @WithUserDetails(value = "nct_1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void noticeAddRequest() throws Exception {
        //given
        final String addNoticeContent = "add";
        NoticeAdd dto = new NoticeAdd(addNoticeContent);

        //when & then
        mockMvc.perform(post("/recruitment/{recruitmentNo}/notice", saveRecruitment.getRecruitmentNo())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(toJson(dto)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("봉사 공지사항 수정 요청에 성공하다.")
    @WithUserDetails(value = "nct_1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void noticeEditRequest() throws Exception {
        //given
        final String addNoticeContent = "add";
        final String editNoticeContent = "edit";
        Notice saveNotice = 공지사항_등록(addNoticeContent, saveRecruitment);
        NoticeEdit dto = new NoticeEdit(editNoticeContent);

        //when & then
        mockMvc.perform(put("/recruitment/{recruitmentNo}/notice/{noticeNo}", saveRecruitment.getRecruitmentNo(), saveNotice.getNoticeNo())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(toJson(dto)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("봉사 공지사항 삭제 요청에 성공하다.")
    @WithUserDetails(value = "nct_1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void noticeDeleteRequest() throws Exception {
        //given
        final String addNoticeContent = "add";
        Notice saveNotice = 공지사항_등록(addNoticeContent, saveRecruitment);

        //when & then
        mockMvc.perform(delete("/recruitment/{recruitmentNo}/notice/{noticeNo}", saveRecruitment.getRecruitmentNo(), saveNotice.getNoticeNo())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("봉사 공지사항 상세 조회 요청에 성공하다.")
    @WithUserDetails(value = "nct_1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void noticeDetailsRequest() throws Exception {
        //given
        Notice saveNotice = 공지사항_등록("add", saveRecruitment);

        User user1 = 사용자_등록("user1");
        User user2 = 사용자_등록("user2");
        User user3 = 사용자_등록("user3");
        User user4 = 사용자_등록("user4");

        Reply parent1 = 댓글_등록(RealWorkCode.NOTICE, saveNotice.getNoticeNo(), "parent1", user1);
        saveNotice.increaseCommentNum();
        Reply children1_1 = 대댓글_등록(parent1, RealWorkCode.NOTICE, saveNotice.getNoticeNo(), "children1-1", user2);
        saveNotice.increaseCommentNum();
        Reply children1_2 = 대댓글_등록(parent1, RealWorkCode.NOTICE, saveNotice.getNoticeNo(), "children1-2", user3);
        saveNotice.increaseCommentNum();
        Reply parent2 = 댓글_등록(RealWorkCode.NOTICE, saveNotice.getNoticeNo(), "parent2", user4);
        saveNotice.increaseCommentNum();

        //when & then
        mockMvc.perform(get("/recruitment/{recruitmentNo}/notice/{noticeNo}", saveRecruitment.getRecruitmentNo(), saveNotice.getNoticeNo())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notice.no").value(saveNotice.getNoticeNo()))
                .andExpect(jsonPath("$.notice.content").value(saveNotice.getContent()))
                .andExpect(jsonPath("$.notice.checkCnt").value(0))
                .andExpect(jsonPath("$.notice.commentsCnt").value(4))
                .andExpect(jsonPath("$.notice.isChecked").value(false))
                .andExpect(jsonPath("$.commentsList[0].no").value(parent1.getReplyNo()))
                .andExpect(jsonPath("$.commentsList[0].profile").value(user1.getPicture()))
                .andExpect(jsonPath("$.commentsList[0].nickName").value(user1.getNickName()))
                .andExpect(jsonPath("$.commentsList[0].content").value(parent1.getContent()))
                .andExpect(jsonPath("$.commentsList[0].commentsCnt").value(2))
                .andExpect(jsonPath("$.commentsList[0].replies[0].no").value(children1_1.getReplyNo()))
                .andExpect(jsonPath("$.commentsList[0].replies[0].profile").value(user2.getPicture()))
                .andExpect(jsonPath("$.commentsList[0].replies[0].nickName").value(user2.getNickName()))
                .andExpect(jsonPath("$.commentsList[0].replies[0].content").value(children1_1.getContent()))
                .andExpect(jsonPath("$.commentsList[0].replies[1].no").value(children1_2.getReplyNo()))
                .andExpect(jsonPath("$.commentsList[0].replies[1].profile").value(user3.getPicture()))
                .andExpect(jsonPath("$.commentsList[0].replies[1].nickName").value(user3.getNickName()))
                .andExpect(jsonPath("$.commentsList[0].replies[1].content").value(children1_2.getContent()))
                .andExpect(jsonPath("$.commentsList[1].no").value(parent2.getReplyNo()))
                .andExpect(jsonPath("$.commentsList[1].profile").value(user4.getPicture()))
                .andExpect(jsonPath("$.commentsList[1].nickName").value(user4.getNickName()))
                .andExpect(jsonPath("$.commentsList[1].content").value(parent2.getContent()))
                .andExpect(jsonPath("$.commentsList[1].commentsCnt").value(0))
                .andDo(print());
    }

    @Test
    @DisplayName("봉사 공지사항 리스트 조회 요청에 성공하다.")
    @WithUserDetails(value = "nct_1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void noticeListRequest() throws Exception {
        //given
        final String addNoticeContent1 = "add1";
        final String addNoticeContent2 = "add2";
        final String addNoticeContent3 = "add3";
        Notice saveNotice1 = 공지사항_등록(addNoticeContent1, saveRecruitment);
        Notice saveNotice2 = 공지사항_등록(addNoticeContent2, saveRecruitment);
        Notice saveNotice3 = 공지사항_등록(addNoticeContent3, saveRecruitment);

        //when
        ResultActions resultActions = mockMvc.perform(get("/recruitment/{recruitmentNo}/notice", saveRecruitment.getRecruitmentNo())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print());

        //then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.noticeList[0].content").value(addNoticeContent1))
                .andExpect(jsonPath("$.noticeList[1].content").value(addNoticeContent2))
                .andExpect(jsonPath("$.noticeList[2].content").value(addNoticeContent3));
    }

    @Test
    @DisplayName("봉사 공지사항 읽음 확인에 성공하다.")
    @WithUserDetails(value = "nct_1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void noticeRead() throws Exception {
        //given
        final String addNoticeContent = "add";
        Notice saveNotice = 공지사항_등록(addNoticeContent, saveRecruitment);

        //when & then
        mockMvc.perform(post("/recruitment/{recruitmentNo}/notice/{noticeNo}/read", saveRecruitment.getRecruitmentNo(),saveNotice.getNoticeNo())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("봉사 공지사항 읽음 해제에 성공하다.")
    @WithUserDetails(value = "nct_1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void noticeReaCancel() throws Exception {
        //given
        final String addNoticeContent = "add";
        Notice saveNotice = 공지사항_등록(addNoticeContent, saveRecruitment);
        읽음_등록(RealWorkCode.NOTICE, saveNotice.getNoticeNo(), writer);
        saveNotice.increaseCheckNum();

        //when & then
        mockMvc.perform(delete("/recruitment/{recruitmentNo}/notice/{noticeNo}/cancel", saveRecruitment.getRecruitmentNo(),saveNotice.getNoticeNo())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    private Notice 공지사항_등록(String content, Recruitment recruitment){
        Notice createNotice = Notice.createNotice(content);
        createNotice.setRecruitment(recruitment);
        return noticeRepository.save(createNotice);
    }
    private Confirmation 읽음_등록(RealWorkCode code, Long no, User user){
        Confirmation createConfirmation = Confirmation.createConfirmation(code, no);
        createConfirmation.setUser(user);
        return confirmationRepository.save(createConfirmation);
    }
    private User 사용자_등록(String value){
        User user = User.createUser(value, "password", value, "email", Gender.M, LocalDate.now(), "picture",
                true, true, true, Role.USER, "kakao", value, null);
        return userRepository.save(user);
    }
    private Reply 댓글_등록(RealWorkCode code, Long no, String content, User writer){
        Reply comment = Reply.createComment(code, no, content);
        comment.setWriter(writer);
        return replyRepository.save(comment);
    }
    private Reply 대댓글_등록(Reply parent, RealWorkCode code, Long no, String content, User writer){
        Reply reply = Reply.createCommentReply(parent, code, no, content);
        reply.setWriter(writer);
        return replyRepository.save(reply);
    }
    private <T> String toJson(T data) throws JsonProcessingException {
        return objectMapper.writeValueAsString(data);
    }
}