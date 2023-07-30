package project.volunteer.domain.user.api.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserDashboardResponse {
    UserInfo userInfo;
    HistoryTimeInfo historyTimeInfo;
    ActivityInfo activityInfo;

    public UserDashboardResponse(UserInfo userInfo, HistoryTimeInfo historyTimeInfo, ActivityInfo activityInfo) {
        this.userInfo = userInfo;
        this.historyTimeInfo = historyTimeInfo;
        this.activityInfo = activityInfo;
    }
}
