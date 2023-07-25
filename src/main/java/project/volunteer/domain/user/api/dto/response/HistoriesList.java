package project.volunteer.domain.user.api.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.domain.recruitment.dto.PictureDetails;
import project.volunteer.domain.user.dao.queryDto.dto.UserHistoryQuery;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
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

    public HistoriesList(UserHistoryQuery userHistorydto) {
        this.no = userHistorydto.getNo();
        this.picture = new PictureDetails(userHistorydto.getStaticImage(), userHistorydto.getUploadImage());
        this.title = userHistorydto.getTitle();
        this.date = userHistorydto.getEndDay().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        this.sido = userHistorydto.getSido();
        this.sigungu = userHistorydto.getSigungu();
        this.volunteeringType = userHistorydto.getVolunteeringType().getViewName();
        this.progressTime = userHistorydto.getProgressTime();
        this.volunteerType = userHistorydto.getVolunteerType().getDesc();
        this.volunteeringCategory = userHistorydto.getVolunteeringCategory().getDesc();

    }
}
