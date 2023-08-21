package project.volunteer.domain.notice.application;

import project.volunteer.domain.notice.api.dto.request.NoticeAdd;
import project.volunteer.domain.notice.api.dto.request.NoticeEdit;
import project.volunteer.domain.notice.domain.Notice;
import project.volunteer.domain.recruitment.domain.Recruitment;

import java.util.List;

public interface NoticeService {

    public Notice addNotice(Recruitment recruitment, NoticeAdd dto);
    public void editNotice(Long noticeNo, NoticeEdit dto);
    public void deleteNotice(Long noticeNo);
    public List<Long> deleteAllNotice(Long recruitmentNo);

    public void increaseCheckNumWithOPTIMSTIC_LOCK(Long noticeNo);
    public void decreaseCheckNumWithOPTIMSTIC_LOCK(Long noticeNo);


    public void increaseCommnetNum(Long noticeNo);

    public void decreaseCommentNum(Long noticeNo);
}
