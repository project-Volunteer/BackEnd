package project.volunteer.domain.recruitment.api.dto.request;

import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;
import project.volunteer.domain.recruitment.application.dto.command.RecruitmentCreateCommand;
import project.volunteer.domain.recruitment.application.dto.command.RepeatPeriodCreateCommand;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.Coordinate;
import project.volunteer.global.common.component.Timetable;
import project.volunteer.global.util.LegacyCodeEnumValueConverterUtils;

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

    @NotNull
    private Boolean isIssued;

    @NotEmpty
    private String volunteerType;

    @NotNull
    @Range(min = 1, max = 9999)
    private Integer maxParticipationNum;

    @NotEmpty
    private String volunteeringType;

    @NotEmpty
    @Pattern(regexp = "^(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])-\\d{4}$")
    private String startDate;

    @NotEmpty
    @Pattern(regexp = "^(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])-\\d{4}$")
    private String endDate;

    @NotEmpty
    private String hourFormat;

    @NotEmpty
    @Pattern(regexp = "^(0[1-9]|1[012]):(0[0-9]|[12345][0-9])$")
    private String startTime;

    @Range(min = 1, max = 24)
    private Integer progressTime;

    private String period;

    private String week;

    private List<String> dayOfWeeks;

    @NotNull
    @Length(min = 1, max = 255)
    private String title;

    @NotNull
    @Length(min = 1, max = 255)
    private String content;

    @NotNull
    private Boolean isPublished; //임시 저장글 유무

    @Valid
    @NotNull
    private AddressRequest addressRequest;

    @Valid
    @NotNull
    private PictureRequest pictureRequest;

    public RecruitmentCreateCommand toCommand() {
        return new RecruitmentCreateCommand(
                title, content,
                LegacyCodeEnumValueConverterUtils.ofLegacyCode(VolunteeringCategory.class, volunteeringCategory),
                VolunteeringType.of(volunteeringType),
                LegacyCodeEnumValueConverterUtils.ofLegacyCode(VolunteerType.class, volunteerType),
                maxParticipationNum, isIssued, organizationName, isPublished,
                new Address(addressRequest.getSido(), addressRequest.getSigungu(), addressRequest.getDetails(),
                        addressRequest.getFullName()),
                new Coordinate(addressRequest.getLatitude(), addressRequest.getLongitude()),
                Timetable.of(startDate, endDate, hourFormat, startTime, progressTime),
                RepeatPeriodCreateCommand.of(period, week, dayOfWeeks),
                pictureRequest.getIsStaticImage(), pictureRequest.getUploadImage());
    }

}
