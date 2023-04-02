package project.volunteer.domain.recruitment.api.dto.request;

import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SaveRecruitForm {

    @Length(min=1, max = 30)
    @NotEmpty(message = "필수 입력값입니다.")
    private String volunteeringCategory;

    @Length(min = 1, max = 50)
    @NotEmpty(message = "필수 입력값입니다.")
    private String organizationName;

    private SaveRecruitAddressForm address;

    @NotNull(message = "널을 허용하지 않습니다.")
    private Boolean isIssued;

    @Length(min = 1, max = 10)
    @NotEmpty(message = "필수 입력값입니다.")
    private String volunteerType;

    @Min(value = 1)
    private Integer volunteerNum;

    @Length(min=1, max = 5)
    @NotEmpty(message = "필수 입력값입니다.")
    private String volunteeringType;

    @NotEmpty(message = "필수 입력값입니다.")
    private String startDay;

    @NotEmpty(message = "필수 입력값입니다.")
    private String endDay;

    @NotEmpty(message = "필수 입력값입니다.")
    private String startTime;

    @Range(min=1, max = 24)
    private Integer progressTime;

    @Length(max = 5)
    @NotNull(message = "널을 허용하지 않습니다.")
    private String period;

    @Length(max = 10)
    @NotNull(message = "널을 허용하지 않습니다.")
    private String week;

    @NotNull(message = "널을 허용하지 않습니다.")
    private List<String> days;

    private SaveRecruitPictureForm picture;

    @Length(min = 1, max = 255)
    @NotEmpty(message = "필수 입력값입니다.")
    private String title;

    @Length(min = 1)
    @NotEmpty(message = "필수 입력값입니다.")
    private String content;

    @NotNull(message = "널을 허용하지 않습니다.")
    private Boolean isPublished; //임시 저장글 유무

}
