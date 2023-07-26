package project.volunteer.domain.notice.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.notice.application.dto.NoticeDetails;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class NoticeDetailsResponse {

    NoticeDetails notice;
    //댓글 details dto 추가 필요

}
