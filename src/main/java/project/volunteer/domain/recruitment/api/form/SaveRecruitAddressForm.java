package project.volunteer.domain.recruitment.api.form;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SaveRecruitAddressForm {
    private String sido; //시,구
    private String sigungu; //시,군,구
    private String details;
    private Float latitude;
    private Float longitude;
}
