package project.volunteer.domain.recruitment.api.dto.request;

import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecruitmentRequest {

    @NotEmpty
    private String volunteeringCategory;

    @NotNull
    @Length(min = 1, max = 50)
    private String organizationName;

    private AddressRequest address;

    @NotNull
    private Boolean isIssued;

    @NotEmpty
    private String volunteerType;

    @NotNull
    @Range(min = 1, max = 9999)
    private Integer volunteerNum;

    @NotEmpty
    private String volunteeringType;

    @NotEmpty
    @Pattern(regexp = "^(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])-\\d{4}$")
    private String startDay;

    @NotEmpty
    @Pattern(regexp = "^(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])-\\d{4}$")
    private String endDay;

    @NotEmpty
    private String hourFormat;

    @NotEmpty
    @Pattern(regexp = "^(0[1-9]|1[012]):(0[0-9]|[12345][0-9])$")
    private String startTime;

    @Range(min=1, max = 24)
    private Integer progressTime;

    @NotNull
    private String period;

    @NotNull
    private Integer week;

    @NotNull
    private List<Integer> days;

    private PictureRequest picture;

    @NotNull
    @Length(min = 1, max = 255)
    private String title;

    @NotNull
    @Length(min = 1, max = 255)
    private String content;

    @NotNull
    private Boolean isPublished; //임시 저장글 유무

}
