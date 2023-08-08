package project.volunteer.domain.notice.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import project.volunteer.domain.notice.domain.Notice;

import javax.validation.constraints.NotEmpty;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NoticeAdd {

    @NotEmpty
    @Length(max = 50)
    private String content;

    public Notice toEntity(){
        Notice notice = Notice.createNotice(content);
        return notice;
    }
}
