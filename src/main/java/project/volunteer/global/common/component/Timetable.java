package project.volunteer.global.common.component;

import java.time.format.DateTimeFormatter;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
@Embeddable
public class Timetable {
    private final static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd-yyyy");
    private final static DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

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

    @Builder
    public Timetable(LocalDate startDay, LocalDate endDay, HourFormat hourFormat, LocalTime startTime,
                     Integer progressTime) {
        this.startDay = startDay;
        this.endDay = endDay;
        this.hourFormat = hourFormat;
        this.startTime = startTime;
        this.progressTime = progressTime;
    }

    public static Timetable of(String startDate, String endDate, String hourFormat, String startTime,
                               int progressTime) {
        return Timetable.builder()
                .startDay(LocalDate.parse(startDate, DATE_FORMATTER))
                .endDay(LocalDate.parse(endDate, DATE_FORMATTER))
                .hourFormat(HourFormat.ofName(hourFormat))
                .startTime(LocalTime.parse(startTime, TIME_FORMATTER))
                .progressTime(progressTime)
                .build();
    }

    public boolean isDoneByStartDate(LocalDate now) {
        return startDay.isBefore(now);
    }

    public boolean isDoneByEndDate(LocalDate now) {
        return endDay.isBefore(now);
    }

    @Override
    public String toString() {
        return "Timetable{" +
                "startDay=" + startDay +
                ", endDay=" + endDay +
                ", hourFormat=" + hourFormat +
                ", startTime=" + startTime +
                ", progressTime=" + progressTime +
                '}';
    }

}
