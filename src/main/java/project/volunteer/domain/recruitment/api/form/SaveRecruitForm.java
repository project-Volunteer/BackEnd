package project.volunteer.domain.recruitment.api.form;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveRecruitForm {

    private String volunteeringCategory;
    private String organizationName;
    private Address address;
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
    private Picture picture;

    private String title;
    private String content;
    private Boolean isPublished;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class Address {
        private String country;
        private String details;
        private Float latitude;
        private Float longitude;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class Picture{
        private Integer type; //0,1
        private String staticImage;
        private MultipartFile uploadImage;
    }
}
