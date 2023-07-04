package project.volunteer.domain.notice.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.notice.application.dto.NoticeDetails;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NoticeListResponse {
    List<NoticeDetails> noticeList;
}
