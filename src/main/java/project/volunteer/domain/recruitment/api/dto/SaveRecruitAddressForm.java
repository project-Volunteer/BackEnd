package project.volunteer.domain.recruitment.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SaveRecruitAddressForm {

    @Length(min = 1, max = 5)
    @NotEmpty(message = "필수 입력값입니다.")
    private String sido; //시,구

    @Length(min=1, max = 10)
    @NotEmpty(message = "필수 입력값입니다.")
    private String sigungu; //시,군,구

    @Length(min = 1, max = 50)
    @NotEmpty(message = "필수 입력값입니다.")
    private String details;

    @NotEmpty(message = "필수 입력값입니다.")
    private Float latitude;

    @NotEmpty(message = "필수 입력값입니다.")
    private Float longitude;
}
