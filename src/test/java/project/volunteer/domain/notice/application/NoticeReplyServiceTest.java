package project.volunteer.domain.notice.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import project.volunteer.domain.confirmation.dao.ConfirmationRepository;
import project.volunteer.domain.notice.dao.NoticeRepository;
import project.volunteer.domain.notice.domain.Notice;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.reply.application.ReplyService;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.global.common.component.RealWorkCode;


import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

/**
 * TDD 테스트 코드
 */
@ExtendWith(MockitoExtension.class)
public class NoticeReplyServiceTest {

    @Mock UserRepository userRepository;
    @Mock ConfirmationRepository confirmationRepository;
    @Mock RecruitmentRepository recruitmentRepository;
    @Mock NoticeRepository noticeRepository;
    @Mock ReplyService replyService;
    @InjectMocks NoticeServiceImpl noticeService;

    @Test
    @DisplayName("공지사항 댓글 등록에 성공하다.")
    void addNoticeComment(){
        //given
        final String content = "test";
        Recruitment recruitment = mock(Recruitment.class);
        Notice notice = mock(Notice.class);

        given(recruitmentRepository.findPublishedByRecruitmentNo(1L)).willReturn(Optional.of(recruitment));
        given(recruitment.isDoneDate()).willReturn(false);
        given(noticeRepository.findValidNotice(1L)).willReturn(Optional.of(notice));
        given(replyService.addComment(1L, RealWorkCode.NOTICE, 1L, content)).willReturn(1L);

        //when
        noticeService.addNoticeComment(1L, 1L, 1L, content);

        //then
        verify(recruitmentRepository,times(1)).findPublishedByRecruitmentNo(1L);
        verify(recruitment, times(1)).isDoneDate();
        verify(noticeRepository, times(1)).findValidNotice(1L);
        verify(replyService,times(1)).addComment(1L, RealWorkCode.NOTICE, 1L, content);
        verify(notice,times(1)).increaseCommentNum();
    }

    @Test
    @DisplayName("공지사항 대댓글 등록에 성공하다.")
    void addNoticeCommentReply(){
        //given
        final String content = "test";
        Recruitment recruitment = mock(Recruitment.class);
        Notice notice = mock(Notice.class);

        given(recruitmentRepository.findPublishedByRecruitmentNo(1L)).willReturn(Optional.of(recruitment));
        given(recruitment.isDoneDate()).willReturn(false);
        given(noticeRepository.findValidNotice(1L)).willReturn(Optional.of(notice));
        given(replyService.addCommentReply(1L, RealWorkCode.NOTICE, 1L, 1L, content)).willReturn(1L);

        //when
        noticeService.addNoticeCommentReply(1L, 1L, 1L, 1L, content);

        //then
        verify(recruitmentRepository,times(1)).findPublishedByRecruitmentNo(1L);
        verify(recruitment, times(1)).isDoneDate();
        verify(noticeRepository, times(1)).findValidNotice(1L);
        verify(replyService,times(1)).addCommentReply(1L, RealWorkCode.NOTICE, 1L, 1L, content);
        verify(notice,times(1)).increaseCommentNum();
    }

    @Test
    @DisplayName("공지사항 댓글 수정에 성공하다.")
    void editNoticeReply(){
        //given
        final String editContent = "test";
        Recruitment recruitment = mock(Recruitment.class);
        Notice notice = mock(Notice.class);

        given(recruitmentRepository.findPublishedByRecruitmentNo(1L)).willReturn(Optional.of(recruitment));
        given(recruitment.isDoneDate()).willReturn(false);
        given(noticeRepository.findValidNotice(1L)).willReturn(Optional.of(notice));
        willDoNothing().given(replyService).editReply(1L,1L,editContent);

        //when
        noticeService.editNoticeReply(1L, 1L, 1L, 1L, editContent);

        //then
        verify(recruitmentRepository,times(1)).findPublishedByRecruitmentNo(1L);
        verify(recruitment, times(1)).isDoneDate();
        verify(noticeRepository, times(1)).findValidNotice(1L);
        verify(replyService,times(1)).editReply(1L, 1L, editContent);
    }

    @Test
    @DisplayName("공지사항 댓글 삭제 성공하다.")
    void deleteNoticeReply(){
        //given
        Recruitment recruitment = mock(Recruitment.class);
        Notice notice = mock(Notice.class);

        given(recruitmentRepository.findPublishedByRecruitmentNo(1L)).willReturn(Optional.of(recruitment));
        given(recruitment.isDoneDate()).willReturn(false);
        given(noticeRepository.findValidNotice(1L)).willReturn(Optional.of(notice));
        willDoNothing().given(replyService).deleteReply(1L);

        //when
        noticeService.deleteNoticeReply(1L, 1L, 1L);

        //then
        verify(recruitmentRepository,times(1)).findPublishedByRecruitmentNo(1L);
        verify(recruitment, times(1)).isDoneDate();
        verify(noticeRepository, times(1)).findValidNotice(1L);
        verify(replyService,times(1)).deleteReply(1L);
        verify(notice,times(1)).decreaseCommentNum();
    }
}
