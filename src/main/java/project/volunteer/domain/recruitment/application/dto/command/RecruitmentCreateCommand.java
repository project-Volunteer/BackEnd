package project.volunteer.domain.recruitment.application.dto.command;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import project.volunteer.domain.image.application.dto.ImageParam;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.repeatPeriod.RepeatPeriod;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.domain.repeatPeriod.validator.PeriodValidation;
import project.volunteer.domain.sehedule.application.dto.command.RegularScheduleCreateCommand;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.Coordinate;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.global.common.component.Timetable;

@Getter
@NoArgsConstructor
public class RecruitmentCreateCommand {

    private String title;
    private String content;
    private VolunteeringCategory volunteeringCategory;
    private VolunteeringType volunteeringType;
    private VolunteerType volunteerType;
    private Integer maxParticipationNum;
    private Boolean isIssued;
    private String organizationName;
    private Boolean isPublished;
    private Address address;
    private Coordinate coordinate;
    private Timetable timetable;

    private RepeatPeriodCreateCommand repeatPeriodCommand; // 반복 날짜 정보
    private Boolean isStaticImage;
    private MultipartFile uploadImageFile;

    public RecruitmentCreateCommand(String title, String content, VolunteeringCategory volunteeringCategory,
                                    VolunteeringType volunteeringType, VolunteerType volunteerType,
                                    Integer maxParticipationNum, Boolean isIssued, String organizationName,
                                    Boolean isPublished, Address address, Coordinate coordinate, Timetable timetable,
                                    RepeatPeriodCreateCommand repeatPeriodCommand, Boolean isStaticImage,
                                    MultipartFile uploadImageFile) {
        this.title = title;
        this.content = content;
        this.volunteeringCategory = volunteeringCategory;
        this.volunteeringType = volunteeringType;
        this.volunteerType = volunteerType;
        this.maxParticipationNum = maxParticipationNum;
        this.isIssued = isIssued;
        this.organizationName = organizationName;
        this.isPublished = isPublished;
        this.address = address;
        this.coordinate = coordinate;
        this.timetable = timetable;
        this.repeatPeriodCommand = repeatPeriodCommand;
        this.isStaticImage = isStaticImage;
        this.uploadImageFile = uploadImageFile;
    }

    public Recruitment toRecruitmentDomain(User writer) {
        return Recruitment.create(title, content, volunteeringCategory, volunteeringType, volunteerType,
                maxParticipationNum,
                isIssued, organizationName, address, coordinate, timetable, isPublished, writer);
    }

    public List<RepeatPeriod> toRepeatPeriodDomains(PeriodValidation periodValidation) {
        return repeatPeriodCommand.toDomains(periodValidation);
    }

    public RegularScheduleCreateCommand toRegularScheduleCreateCommand() {
        return new RegularScheduleCreateCommand(timetable, repeatPeriodCommand, organizationName, address, "",
                maxParticipationNum);
    }

    public ImageParam toUploadImageCreateCommand(Long recruitmentNo) {
        return new ImageParam(RealWorkCode.RECRUITMENT, recruitmentNo, uploadImageFile);
    }

    public Boolean isUploadImage() {
        return !isStaticImage;
    }

}
