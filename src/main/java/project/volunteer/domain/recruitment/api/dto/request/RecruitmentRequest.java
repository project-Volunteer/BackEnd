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
public class RecruitmentRequest {

    @Length(min=1, max = 30)
    @NotEmpty(message = "필수 입력값입니다.")
    private String volunteeringCategory;

    @Length(min = 1, max = 50)
    @NotEmpty(message = "필수 입력값입니다.")
    private String organizationName;

    private AddressRequest address;

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

    @Length(max = 2)
    @NotEmpty(message = "필수 입력값입니다.")
    private String hourFormat;

    @NotEmpty(message = "필수 입력값입니다.")
    private String startTime;

    @Range(min=1, max = 24)
    private Integer progressTime;

    @Length(max = 5)
    @NotNull(message = "널을 허용하지 않습니다.")
    private String period;

    @Range(min = 0, max = 5)
    @NotNull(message = "널을 허용하지 않습니다.")
    private Integer week;

    @NotNull(message = "널을 허용하지 않습니다.")
    private List<Integer> days;

    private PictureRequest picture;

    @Length(min = 1, max = 255)
    @NotEmpty(message = "필수 입력값입니다.")
    private String title;

    @Length(min = 1)
    @NotEmpty(message = "필수 입력값입니다.")
    private String content;

    @NotNull(message = "널을 허용하지 않습니다.")
    private Boolean isPublished; //임시 저장글 유무

}
