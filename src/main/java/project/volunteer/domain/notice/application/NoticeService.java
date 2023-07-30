package project.volunteer.domain.notice.application;

import project.volunteer.domain.notice.api.dto.request.NoticeAdd;
import project.volunteer.domain.notice.api.dto.request.NoticeEdit;
import project.volunteer.domain.notice.domain.Notice;

public interface NoticeService {

    public Notice addNotice(Long recruitmentNo, NoticeAdd dto);
    public void editNotice(Long recruitmentNo, Long noticeNo, NoticeEdit dto);
    public void deleteNotice(Long recruitmentNo, Long noticeNo);

    public void readNotice(Long recruitmentNo, Long noticeNo, Long userNo);
    public void readCancelNotice(Long recruitmentNo, Long noticeNo, Long userNo);


    public void addNoticeComment(Long recruitmentNo, Long noticeNo, Long loginUserNo, String content);
    public void addNoticeCommentReply(Long recruitmentNo, Long noticeNo, Long loginUserNo, Long parentNo, String content);
    public void editNoticeReply(Long recruitmentNo, Long noticeNo, Long loginUserNo, Long replyNo, String content);
    public void deleteNoticeReply(Long recruitmentNo, Long noticeNo, Long replyNo);
}
