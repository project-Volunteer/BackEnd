package project.volunteer.domain.recruitment.api.dto.response;

import lombok.*;
import project.volunteer.global.common.response.BaseResponse;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RecruitmentSaveResponse extends BaseResponse {

    private Long no;

    public RecruitmentSaveResponse(String message, Long no) {
        super(message);
        this.no = no;
    }
}
