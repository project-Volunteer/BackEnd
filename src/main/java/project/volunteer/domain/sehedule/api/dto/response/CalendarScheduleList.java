package project.volunteer.domain.sehedule.api.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.joda.time.format.DateTimeFormat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
public class CalendarScheduleList {

    private Long no;
    private String day;

    public static CalendarScheduleList createCalendarSchedule(Long schedule, LocalDate date){
        CalendarScheduleList calendarSchedule = new CalendarScheduleList();
        calendarSchedule.no = schedule;
        calendarSchedule.day = date.format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        return calendarSchedule;
    }
}
