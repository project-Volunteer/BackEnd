package project.volunteer.domain.sehedule.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CalendarScheduleListResponse {
    private List<CalendarScheduleList> scheduleList;
}
