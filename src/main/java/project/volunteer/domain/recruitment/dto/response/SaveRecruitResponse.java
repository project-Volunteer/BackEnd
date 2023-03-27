package project.volunteer.domain.recruitment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.volunteer.global.common.response.BaseResponse;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaveRecruitResponse extends BaseResponse {

    private Long no;

    public SaveRecruitResponse(String message, Long no) {
        super(message);
        this.no = no;
    }
}
