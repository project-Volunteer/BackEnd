package project.volunteer.domain.user.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogboardTempList {
    private Long no;
    private String content;
    private String writeTime;
    private String writeDay;
}
