package project.volunteer.domain.user.api.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class LogboardTempListResponse {
    private List<LogboardTempList> logboardTempLists;

    public LogboardTempListResponse(List<LogboardTempList> data) {
        this.logboardTempLists = data;
    }
}
