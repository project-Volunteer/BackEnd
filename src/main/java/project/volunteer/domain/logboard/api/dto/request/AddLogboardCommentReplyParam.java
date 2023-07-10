package project.volunteer.domain.logboard.api.dto.request;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddLogboardCommentReplyParam {
    @NotNull
	private Long logNo;

    @NotNull
	private Long parentNo;
    
    @NotNull
    @Length(min = 1, max = 255)
	private String content;

}
