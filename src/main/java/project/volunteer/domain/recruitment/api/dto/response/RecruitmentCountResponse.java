package project.volunteer.domain.recruitment.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.global.common.response.BaseResponse;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RecruitmentCountResponse extends BaseResponse {

    private Long totalCnt;

    public RecruitmentCountResponse(String message, Long totalCnt) {
        super(message);
        this.totalCnt = totalCnt;
    }
}
