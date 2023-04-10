package project.volunteer.domain.recruitment.api.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.global.common.response.BaseResponse;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RecruitmentListResponse extends BaseResponse {
    private List<RecruitmentListDto> data;
    private Boolean isLast;
    private Long lastId;

    public RecruitmentListResponse(String message, List<RecruitmentListDto> data, Boolean isLast, Long lastId){
        super(message);
        this.data =data;
        this.isLast = isLast;
        this.lastId = lastId;
    }

}
