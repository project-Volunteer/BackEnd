package project.volunteer.domain.recruitment.api.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RecruitmentListResponse {
    private List<RecruitmentList> recruitmentList;
    private Boolean isLast;
    private Long lastId;

    public RecruitmentListResponse(List<RecruitmentList> data, Boolean isLast, Long lastId){
        this.recruitmentList =data;
        this.isLast = isLast;
        this.lastId = lastId;
    }

}
