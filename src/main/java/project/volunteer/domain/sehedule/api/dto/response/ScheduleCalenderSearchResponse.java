package project.volunteer.domain.sehedule.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.format.DateTimeFormatter;
import project.volunteer.domain.sehedule.application.dto.query.ScheduleCalendarSearchResult;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleCalenderSearchResponse {
    private Long no;
    private String day;

    public static ScheduleCalenderSearchResponse from(ScheduleCalendarSearchResult result) {
        return new ScheduleCalenderSearchResponse(result.getScheduleNo(),
                result.getDate().format(DateTimeFormatter.ofPattern("MM-dd-yyyy")));
    }

}
