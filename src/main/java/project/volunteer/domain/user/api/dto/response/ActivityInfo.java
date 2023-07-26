package project.volunteer.domain.user.api.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class ActivityInfo {
    int joinApprovalCnt;
    int joinRequestCnt;
    int recruitingCnt;
    int tempSavingCnt;

    public static ActivityInfo makeActivityInfo(int joinApprovalCnt, int joinRequestCnt, int recruitingCnt, int tempSavingCnt) {
        ActivityInfo activityInfo = new ActivityInfo();
        activityInfo.joinApprovalCnt = joinApprovalCnt;
        activityInfo.joinRequestCnt = joinRequestCnt;
        activityInfo.recruitingCnt = recruitingCnt;
        activityInfo.tempSavingCnt = tempSavingCnt;
        return activityInfo;
    }
}
