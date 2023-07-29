package project.volunteer.domain.user.api.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HistoryTimeInfo {
    int totalTime;
    int totalCnt;

    public HistoryTimeInfo(int totalTime, int totalCnt) {
        this.totalTime = totalTime;
        this.totalCnt = totalCnt;
    }
}
