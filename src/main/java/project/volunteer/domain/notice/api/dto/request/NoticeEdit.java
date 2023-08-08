package project.volunteer.domain.notice.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NoticeEdit {

    @NotEmpty
    @Length(max = 50)
    private String content;
}
