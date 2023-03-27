package project.volunteer.domain.recruitment.api.form;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SaveRecruitForm {
    private String volunteeringCategory;
    private String organizationName;
    private SaveRecruitAddressForm address;
    private Boolean isIssued;
    private String volunteerType;
    private Integer volunteerNum;
    private String volunteeringType;
    private String startDay;
    private String endDay;
    private String startTime;
    private Integer progressTime;
    private String period;
    private String week;
    private List<String> days;
    private SaveRecruitPictureForm picture;
    private String title;
    private String content;
    private Boolean isPublished;

}
