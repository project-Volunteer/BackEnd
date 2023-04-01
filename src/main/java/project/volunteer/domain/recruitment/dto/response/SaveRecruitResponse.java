package project.volunteer.domain.recruitment.dto.response;

import lombok.*;
import project.volunteer.global.common.response.BaseResponse;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SaveRecruitResponse extends BaseResponse {

    private Long no;

    public SaveRecruitResponse(String message, Long no) {
        super(message);
        this.no = no;
    }
}
