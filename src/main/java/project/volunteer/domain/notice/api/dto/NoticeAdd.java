package project.volunteer.domain.notice.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.notice.domain.Notice;

import javax.validation.constraints.NotEmpty;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NoticeAdd {

    @NotEmpty
    private String content;

    public Notice toEntity(){
        Notice notice = Notice.createNotice(content);
        return notice;
    }
}
