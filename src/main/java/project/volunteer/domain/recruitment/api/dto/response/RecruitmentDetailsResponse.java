package project.volunteer.domain.recruitment.api.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.domain.recruitment.application.dto.RecruitmentDetails;
import project.volunteer.global.common.response.BaseResponse;

@Getter
@Setter
@NoArgsConstructor
public class RecruitmentDetailsResponse extends BaseResponse {
    private RecruitmentDetails data;
    public RecruitmentDetailsResponse(String message, RecruitmentDetails dto){
        super(message);
        this.data = dto;
    }
}
