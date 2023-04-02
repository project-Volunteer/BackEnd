package project.volunteer.domain.recruitment.api.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.domain.recruitment.application.dto.RecruitmentDto;
import project.volunteer.global.common.response.BaseResponse;

@Getter
@Setter
@NoArgsConstructor
public class RecruitmentResponse extends BaseResponse {
    private RecruitmentDto data;
    public RecruitmentResponse(String message, RecruitmentDto dto){
        super(message);
        this.data = dto;
    }
}
