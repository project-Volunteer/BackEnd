package project.volunteer.domain.notice.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.confirmation.application.ConfirmationService;
import project.volunteer.domain.notice.api.dto.request.NoticeAdd;
import project.volunteer.domain.notice.api.dto.request.NoticeEdit;
import project.volunteer.domain.notice.application.NoticeService;
import project.volunteer.domain.notice.domain.Notice;
import project.volunteer.domain.recruitment.application.RecruitmentService;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.reply.application.ReplyService;
import project.volunteer.domain.user.application.UserService;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.RealWorkCode;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeFacade {
    private final UserService userService;
    private final RecruitmentService recruitmentService;
    private final NoticeService noticeService;
    private final ConfirmationService confirmationService;
    private final ReplyService replyService;

    @Transactional
    public Notice registerVolunteerPostNotice(Long recruitmentNo, NoticeAdd dto){
        Recruitment recruitment = recruitmentService.findActivatedRecruitment(recruitmentNo);

        return noticeService.addNotice(recruitment, dto);
    }

    @Transactional
    public void editVolunteerPostNotice(Long recruitmentNo, Long noticeNo, NoticeEdit dto){
        recruitmentService.findActivatedRecruitment(recruitmentNo);

        noticeService.editNotice(noticeNo, dto);
    }

    @Transactional
    public void deleteVolunteerPostNotice(Long recruitmentNo, Long noticeNo){
        recruitmentService.findActivatedRecruitment(recruitmentNo);

        noticeService.deleteNotice(noticeNo);
    }

    @Transactional
    public void readVolunteerPostNotice(Long userNo, Long recruitmentNo, Long noticeNo){
        User user = userService.findUser(userNo);

        recruitmentService.findActivatedRecruitment(recruitmentNo);

        //확인 저장
        confirmationService.addConfirmation(user, RealWorkCode.NOTICE, noticeNo);

        //낙관적 락 기반 공지사항 읽음 개수 늘림
        noticeService.increaseCheckNumWithOPTIMSTIC_LOCK(noticeNo);
    }

    @Transactional
    public void readCancelVolunteerPostNotice(Long userNo, Long recruitmentNo, Long noticeNo){
        recruitmentService.findActivatedRecruitment(recruitmentNo);

        //확인 삭제
        confirmationService.deleteConfirmation(userNo, RealWorkCode.NOTICE, noticeNo);

        //낙관적 락 기반 공지사항 읽음 개수 줄임
        noticeService.decreaseCheckNumWithOPTIMSTIC_LOCK(noticeNo);
    }

    @Transactional
    public void addVolunteerPostNoticeComment(Long userNo, Long recruitmentNo, Long noticeNo, String content){
        User user = userService.findUser(userNo);

        recruitmentService.findActivatedRecruitment(recruitmentNo);

        //댓글 저장
        replyService.addComment(user, RealWorkCode.NOTICE, noticeNo, content);

        //공지사항 댓글 개수 증가
        noticeService.increaseCommnetNum(noticeNo);
    }

    @Transactional
    public void addVolunteerPostNoticeCommentReply(Long userNo, Long recruitmentNo, Long noticeNo, Long parentNo, String content){
        User user = userService.findUser(userNo);

        recruitmentService.findActivatedRecruitment(recruitmentNo);

        //대댓글 저장
        replyService.addCommentReply(user, RealWorkCode.NOTICE, noticeNo, parentNo, content);

        //공지사항 댓글 개수 증가
        noticeService.increaseCommnetNum(noticeNo);
    }

    @Transactional
    public void editVolunteerPostNoticeCommentOrReply(Long userNo, Long recruitmentNo, Long replyNo, String content){
        recruitmentService.findActivatedRecruitment(recruitmentNo);

        //댓글 or 대댓글 수정
        replyService.editReply(replyNo, content);
    }

    @Transactional
    public void deleteVolunteerPostNoticeCommentOrReply(Long recruitmentNo, Long noticeNo, Long replyNo){
        recruitmentService.findActivatedRecruitment(recruitmentNo);

        //댓글 or 대댓글 삭제
        replyService.deleteReply(replyNo);

        //공지사항 댓글 개수 감소
        noticeService.decreaseCommentNum(noticeNo);
    }

}
