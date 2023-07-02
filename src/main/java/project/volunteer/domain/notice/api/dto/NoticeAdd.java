package project.volunteer.domain.notice.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.notice.domain.Notice;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NoticeAdd {

    private String content;

    public Notice toEntity(){
        Notice notice = Notice.createNotice(content);
        return notice;
    }
}
