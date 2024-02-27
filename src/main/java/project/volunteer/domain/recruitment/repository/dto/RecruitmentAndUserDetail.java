package project.volunteer.domain.recruitment.repository.dto;

import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.Coordinate;
import project.volunteer.global.common.component.Timetable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecruitmentAndUserDetail {
    private Long no;
    private VolunteeringCategory volunteeringCategory;
    private String organizationName;
    private Boolean isIssued;
    private VolunteeringType volunteeringType;
    private VolunteerType volunteerType;
    private Integer maxParticipationNum;
    private String title;
    private String content;
    private Timetable timetable;
    private Address address;
    private Coordinate coordinate;

    private String recruitmentImagePath;
    private String userNickName;
    private String userImagePath;

    public RecruitmentAndUserDetail(Long no, VolunteeringCategory volunteeringCategory, String organizationName,
                                    Boolean isIssued, VolunteeringType volunteeringType, VolunteerType volunteerType,
                                    Integer maxParticipationNum, Timetable timetable, String title, String content,
                                    Address address, Coordinate coordinate, String recruitmentImagePath,
                                    String userNickName, String basicUserImagePath, String uploadUserImagePath) {
        this.no = no;
        this.volunteeringCategory = volunteeringCategory;
        this.organizationName = organizationName;
        this.isIssued = isIssued;
        this.volunteeringType = volunteeringType;
        this.volunteerType = volunteerType;
        this.maxParticipationNum = maxParticipationNum;
        this.title = title;
        this.content = content;
        this.timetable = timetable;
        this.address = address;
        this.coordinate = coordinate;
        this.recruitmentImagePath = recruitmentImagePath;
        this.userNickName = userNickName;
        if (Objects.isNull(uploadUserImagePath)) {
            userImagePath = basicUserImagePath;
        } else {
            userImagePath = uploadUserImagePath;
        }
    }

}
