package project.volunteer.domain.user.api.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.recruitment.application.dto.query.detail.PictureDetail;
import project.volunteer.domain.user.dao.queryDto.dto.UserHistoryQuery;

import java.time.format.DateTimeFormatter;

@Getter
@NoArgsConstructor
public class HistoriesList {
    private Long no;
    private PictureDetail picture;
    private String title;
    private String date;
    private String sido;
    private String sigungu;
    private String volunteeringCategory;
    private String volunteeringType;
    private Boolean isIssued;
    private String volunteerType;
    private Integer progressTime;

    public static HistoriesList makeHistoriesList(UserHistoryQuery userHistorydto) {
        HistoriesList historiesList = new HistoriesList();
        historiesList.no = userHistorydto.getNo();
        if(userHistorydto.getUploadImage()==null){
            historiesList.picture = new PictureDetail(true, null);
        }else{
            historiesList.picture = new PictureDetail(false, userHistorydto.getUploadImage());
        }
        historiesList.title = userHistorydto.getTitle();
        historiesList.date = userHistorydto.getEndDay().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        historiesList.sido = userHistorydto.getSido();
        historiesList.sigungu = userHistorydto.getSigungu();
        historiesList.volunteeringCategory = userHistorydto.getVolunteeringCategory().getDesc();
        historiesList.volunteeringType = userHistorydto.getVolunteeringType().getViewName();
        historiesList.isIssued = userHistorydto.getIsIssued();
        historiesList.volunteerType = userHistorydto.getVolunteerType().getDesc();
        historiesList.progressTime = userHistorydto.getProgressTime();
        return  historiesList;
    }
}
