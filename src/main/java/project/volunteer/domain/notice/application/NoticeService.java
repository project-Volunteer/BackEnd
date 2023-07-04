package project.volunteer.domain.notice.application;

import project.volunteer.domain.notice.api.dto.NoticeAdd;
import project.volunteer.domain.notice.api.dto.NoticeEdit;
import project.volunteer.domain.notice.domain.Notice;

public interface NoticeService {

    public Notice addNotice(Long recruitmentNo, NoticeAdd dto);
    public void editNotice(Long recruitmentNo, Long noticeNo, NoticeEdit dto);
    public void deleteNotice(Long recruitmentNo, Long noticeNo);

}
