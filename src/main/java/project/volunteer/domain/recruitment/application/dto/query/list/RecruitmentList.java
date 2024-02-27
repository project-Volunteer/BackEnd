package project.volunteer.domain.recruitment.application.dto.query.list;

import java.time.format.DateTimeFormatter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.recruitment.application.dto.query.detail.PictureDetail;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.Timetable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecruitmentList {
    private Long no;
    private String volunteeringCategory;
    private String volunteeringType;
    private String volunteerType;
    private String title;
    private String sido;
    private String sigungu;
    private String fullName;
    private String startDate;
    private String endDate;
    private Boolean isIssued;
    private Integer maxParticipationNum;
    private Integer currentParticipationNum;
    private PictureDetail picture;

    public RecruitmentList(Long no, VolunteeringCategory volunteeringCategory, VolunteeringType volunteeringType,
                           VolunteerType volunteerType, String title, Boolean isIssued, Integer maxParticipationNum,
                           Integer currentParticipationNum, Address address, Timetable timetable,
                           String recruitmentImagePath) {
        this.no = no;
        this.volunteeringCategory = volunteeringCategory.getId();
        this.volunteeringType = volunteeringType.getId();
        this.volunteerType = volunteerType.getId();
        this.title = title;
        this.sido = address.getSido();
        this.sigungu = address.getSigungu();
        this.fullName = address.getFullName();
        this.startDate = timetable.getStartDay().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        this.endDate = timetable.getEndDay().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        this.isIssued = isIssued;
        this.maxParticipationNum = maxParticipationNum;
        this.currentParticipationNum = currentParticipationNum;
        this.picture = PictureDetail.of(recruitmentImagePath);
    }
}
