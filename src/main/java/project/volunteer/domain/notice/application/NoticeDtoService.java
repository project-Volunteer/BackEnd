package project.volunteer.domain.notice.application;

import project.volunteer.domain.notice.application.dto.NoticeDetails;
import java.util.List;

public interface NoticeDtoService {

    public List<NoticeDetails> findNoticeDtos(Long recruitmentNo, Long userNo);

    public NoticeDetails findNoticeDto(Long noticeNo, Long userNo);
}
