package project.volunteer.global.common.component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDate;
import java.time.LocalTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Timetable {

    @Column(name = "start_day", nullable = false)
    private LocalDate startDay;

    @Column(name = "end_day", nullable = false)
    private LocalDate endDay;

    @Enumerated(EnumType.STRING)
    @Column(name = "hour_format", length = 2, nullable = false)
    private HourFormat hourFormat;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "progress_time", columnDefinition = "TINYINT", nullable = false)
    private Integer progressTime; //(1~24시간)


    public static Timetable createTimetable(LocalDate startDay, LocalDate endDay, HourFormat hourFormat, LocalTime startTime, int progressTime){
        Timetable timetable = new Timetable();
        timetable.startDay = startDay;
        timetable.endDay = endDay;
        timetable.hourFormat = hourFormat;
        timetable.startTime = startTime;
        timetable.progressTime = progressTime;
        return timetable;
    }

}
