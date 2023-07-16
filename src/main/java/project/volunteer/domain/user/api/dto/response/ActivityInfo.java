package project.volunteer.domain.user.api.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ActivityInfo {
    int joinApprovalCnt;
    int joinRequestCnt;
    int recruitingCnt;
    int tempSavingCnt;

    public ActivityInfo(int joinApprovalCnt, int joinRequestCnt, int recruitingCnt, int tempSavingCnt) {
        this.joinApprovalCnt = joinApprovalCnt;
        this.joinRequestCnt = joinRequestCnt;
        this.recruitingCnt = recruitingCnt;
        this.tempSavingCnt = tempSavingCnt;
    }
}
