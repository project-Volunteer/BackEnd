package project.volunteer.domain.sehedule.api.dto.response;

import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import lombok.NoArgsConstructor;
import project.volunteer.domain.sehedule.application.dto.query.ScheduleCalendarSearchResult;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleCalenderSearchResponses {
    private List<ScheduleCalenderSearchResponse> scheduleList;

    public static ScheduleCalenderSearchResponses from(List<ScheduleCalendarSearchResult> results) {
        List<ScheduleCalenderSearchResponse> responses = results.stream()
                .map(ScheduleCalenderSearchResponse::from)
                .collect(Collectors.toList());
        return new ScheduleCalenderSearchResponses(responses);
    }
}
