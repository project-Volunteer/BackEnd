package project.volunteer.domain.user.api.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class JoinRecruitmentListResponse {
    private List<JoinRecruitmentList> recruitmentList;

    public JoinRecruitmentListResponse(List<JoinRecruitmentList> recruitmentList) {
        this.recruitmentList = recruitmentList;
    }
}
