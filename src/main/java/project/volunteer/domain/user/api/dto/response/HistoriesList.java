package project.volunteer.domain.user.api.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.recruitment.dto.PictureDetails;
import project.volunteer.domain.user.dao.queryDto.dto.UserHistoryQuery;

import java.time.format.DateTimeFormatter;

@Getter
@NoArgsConstructor
public class HistoriesList {
    private Long no;
    private PictureDetails picture;
    private String title;
    private String date;
    private String sido;
    private String sigungu;
    private String volunteeringCategory;
    private String volunteeringType;
    private String volunteerType;
    private Integer progressTime;

    public static HistoriesList makeHistoriesList(UserHistoryQuery userHistorydto) {
        HistoriesList historiesList = new HistoriesList();
        historiesList.no = userHistorydto.getNo();
        historiesList.picture = new PictureDetails(userHistorydto.getStaticImage(), userHistorydto.getUploadImage());
        historiesList.title = userHistorydto.getTitle();
        historiesList.date = userHistorydto.getEndDay().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        historiesList.sido = userHistorydto.getSido();
        historiesList.sigungu = userHistorydto.getSigungu();
        historiesList.volunteeringType = userHistorydto.getVolunteeringType().getViewName();
        historiesList.progressTime = userHistorydto.getProgressTime();
        historiesList.volunteerType = userHistorydto.getVolunteerType().getDesc();
        historiesList.volunteeringCategory = userHistorydto.getVolunteeringCategory().getDesc();
        return  historiesList;
    }
}
