package project.volunteer.domain.sehedule.application.dto.query;

import java.time.format.DateTimeFormatter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.sehedule.repository.dao.ScheduleDetail;
import project.volunteer.global.common.dto.StateResult;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDetailSearchResult {
    private Long no;
    private AddressDetail address;
    private String startDate;
    private String startTime;
    private String hourFormat;
    private int progressTime;
    private int volunteerNum;
    private String content;
    private int activeVolunteerNum;
    private String state;
    private Boolean hasData;

    public static ScheduleDetailSearchResult of(ScheduleDetail scheduleDetail, StateResult state) {
        return new ScheduleDetailSearchResult(
                scheduleDetail.getNo(),
                AddressDetail.from(scheduleDetail.getAddress()),
                scheduleDetail.getTimetable().getStartDay().format(DateTimeFormatter.ofPattern("MM-dd-yyyy")),
                scheduleDetail.getTimetable().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                scheduleDetail.getTimetable().getHourFormat().getId(),
                scheduleDetail.getTimetable().getProgressTime(),
                scheduleDetail.getMaxParticipationNum(),
                scheduleDetail.getContent(),
                scheduleDetail.getCurrentParticipationNum(),
                state.getId(),
                true
        );
    }

    public static ScheduleDetailSearchResult createEmpty() {
        ScheduleDetailSearchResult result = new ScheduleDetailSearchResult();
        result.hasData = false;
        return result;
    }

}
