package project.volunteer.domain.user.api.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.domain.recruitment.dto.PictureDetails;

@Getter
@Setter
@NoArgsConstructor
public class JoinRecruitmentList {
    private Long no;
    private PictureDetails picture;
    private String startDay;
    private String endDay;
    private String title;
    private String sido;
    private String sigungu;
    private String details;
    private String volunteeringCategory;
    private String volunteeringType;
    private Boolean isIssued;
    private String volunteerType;

    public static JoinRecruitmentList makeJoinRecruitmentList(Long no, String picture, String startDay, String endDay, String title,
                               String sido, String sigungu, String details, String volunteeringCategory,
                               String volunteeringType, Boolean isIssued, String volunteerType) {
        JoinRecruitmentList joinRecruitmentList = new JoinRecruitmentList();

        joinRecruitmentList.no = no;
        if(picture == null){
            joinRecruitmentList.picture = new PictureDetails(true, null);
        }else{
            joinRecruitmentList.picture = new PictureDetails(false, picture);
        }
        joinRecruitmentList.startDay = startDay;
        joinRecruitmentList.endDay = endDay;
        joinRecruitmentList.title = title;
        joinRecruitmentList.sido = sido;
        joinRecruitmentList.sigungu = sigungu;
        joinRecruitmentList.details = details;
        joinRecruitmentList.volunteeringCategory = volunteeringCategory;
        joinRecruitmentList.volunteeringType = volunteeringType;
        joinRecruitmentList.isIssued = isIssued;
        joinRecruitmentList.volunteerType = volunteerType;

        return joinRecruitmentList;
    }

}
