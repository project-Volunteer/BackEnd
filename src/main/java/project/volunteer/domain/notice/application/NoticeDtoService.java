package project.volunteer.domain.notice.application;

import project.volunteer.domain.notice.application.dto.NoticeDetails;
import project.volunteer.domain.notice.application.dto.NoticeList;

import java.util.List;

public interface NoticeDtoService {

    public List<NoticeList> findNoticeDtos(Long recruitmentNo, Long userNo);

    public NoticeDetails findNoticeDto(Long recruitmentNo, Long noticeNo, Long userNo);
}
