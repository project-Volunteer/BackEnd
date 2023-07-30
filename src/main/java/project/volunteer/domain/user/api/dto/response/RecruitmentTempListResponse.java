package project.volunteer.domain.user.api.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RecruitmentTempListResponse {
    private List<RecruitmentTempList> recruitmentTempList;

    public RecruitmentTempListResponse(List<RecruitmentTempList> data) {
        this.recruitmentTempList = data;
    }
}
