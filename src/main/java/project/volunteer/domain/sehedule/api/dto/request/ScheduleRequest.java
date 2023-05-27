package project.volunteer.domain.sehedule.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleRequest {

    @NotNull
    private Long no;

    private AddressRequest address;

    @NotEmpty
    @Pattern(regexp = "^(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])-\\d{4}$")
    private String startDay;

    @NotEmpty
    private String hourFormat;

    @NotEmpty
    @Pattern(regexp = "^(0[1-9]|1[012]):(0[0-9]|[12345][0-9])$")
    private String startTime;

    @NotNull
    @Range(min = 1, max = 24)
    private Integer progressTime;

    @NotEmpty
    private String organizationName;

    @NotNull
    @Range(min = 1, max = 50)
    private Integer volunteerNum;

    @Length(max = 50)
    private String content;
}
