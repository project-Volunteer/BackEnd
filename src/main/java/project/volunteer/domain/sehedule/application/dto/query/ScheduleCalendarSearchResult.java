package project.volunteer.domain.sehedule.application.dto.query;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleCalendarSearchResult {
    private Long scheduleNo;
    private LocalDate date;

}
