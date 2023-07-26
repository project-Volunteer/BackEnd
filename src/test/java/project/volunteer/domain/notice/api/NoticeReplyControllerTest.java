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
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.notice.dao.NoticeRepository;
import project.volunteer.domain.notice.domain.Notice;
import project.volunteer.domain.participation.dao.ParticipantRepository;
import project.volunteer.domain.participation.domain.Participant;
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
import project.volunteer.global.common.dto.CommentContentParam;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class NoticeReplyControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired RecruitmentRepository recruitmentRepository;
    @Autowired NoticeRepository noticeRepository;
    @Autowired ParticipantRepository participantRepository;
    @Autowired ReplyRepository replyRepository;

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
        Recruitment createRecruitment = Recruitment.createRecruitment("title", "content", VolunteeringCategory.CULTURAL_EVENT, VolunteeringType.IRREG,
                VolunteerType.TEENAGER, 3, true, "organization",
                Address.createAddress("11", "1111","details"), Coordinate.createCoordinate(3.2F, 3.2F),
                Timetable.createTimetable(LocalDate.now(), LocalDate.now().plusMonths(3), HourFormat.AM, LocalTime.now(), 3), true);
        createRecruitment.setWriter(writerUser);
        saveRecruitment = recruitmentRepository.save(createRecruitment);

        //공지사항 저장
        Notice createNotice = Notice.createNotice("test notice");
        createNotice.setRecruitment(saveRecruitment);
        saveNotice = noticeRepository.save(createNotice);

        //봉사 모집글 팀 사용자 추가 및 댓글 작성자
        User teamUser = User.createUser(teamAndReplyWriterName, "password", teamAndReplyWriterName, "email", Gender.M, LocalDate.now(), "picture",
                true, true, true, Role.USER, "kakao", teamAndReplyWriterName, null);
        replyWriter = userRepository.save(teamUser);
        Participant participant = Participant.createParticipant(saveRecruitment, replyWriter, ParticipantState.JOIN_APPROVAL);
        participantRepository.save(participant);
    }

    @Test
    @DisplayName("봉사 공지사항 댓글 작성에 성공하다.")
    @WithUserDetails(value = teamAndReplyWriterName, setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void noticeCommentAdd() throws Exception {
        //given
        final CommentContentParam dto = new CommentContentParam("test comment");

        //when & then
        mockMvc.perform(post("/recruitment/{recruitmentNo}/notice/{noticeNo}/comment", saveRecruitment.getRecruitmentNo(), saveNotice.getNoticeNo())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isCreated())
                .andDo(print());
    }
    @Test
    @DisplayName("봉사 공지사항 대댓글 작성에 성공하다.")
    @WithUserDetails(value = teamAndReplyWriterName, setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void noticeCommentReplyAdd() throws Exception {
        //given
        final CommentContentParam dto = new CommentContentParam("test comment reply");
        Long commentNo = 댓글_추가(saveNotice.getNoticeNo(), "test reply", replyWriter);

        //when & then
        mockMvc.perform(post("/recruitment/{recruitmentNo}/notice/{noticeNo}/comment/{parentNo}/reply",
                        saveRecruitment.getRecruitmentNo(), saveNotice.getNoticeNo(), commentNo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isCreated())
                .andDo(print());
    }
    @Test
    @DisplayName("봉사 공지사항 댓글 수정에 성공하다.")
    @WithUserDetails(value = teamAndReplyWriterName, setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void noticeReplyEdit() throws Exception {
        //given
        final CommentContentParam dto = new CommentContentParam("test edit comment");
        Long commentNo = 댓글_추가(saveNotice.getNoticeNo(), "test comment", replyWriter);

        //when & then
        mockMvc.perform(put("/recruitment/{recruitmentNo}/notice/{noticeNo}/comment/{replyNo}",
                        saveRecruitment.getRecruitmentNo(), saveNotice.getNoticeNo(), commentNo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isOk())
                .andDo(print());
    }
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
    public void noticeReplyDelete() throws Exception {
        //given
        Long commentNo = 댓글_추가(saveNotice.getNoticeNo(), "test comment", replyWriter);

        //when & then
        mockMvc.perform(delete("/recruitment/{recruitmentNo}/notice/{noticeNo}/comment/{replyNo}",
                        saveRecruitment.getRecruitmentNo(), saveNotice.getNoticeNo(), commentNo)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }


    private Long 댓글_추가(Long no, String content, User writer){
        Reply comment = Reply.createComment(RealWorkCode.NOTICE, no, content, writer.getUserNo());
        comment.setWriter(writer);
        return replyRepository.save(comment).getReplyNo();
    }
    private <T> String toJson(T data) throws JsonProcessingException {
        return objectMapper.writeValueAsString(data);
    }
}
