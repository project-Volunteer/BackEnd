package project.volunteer.global.common.component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
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

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "progress_time", columnDefinition = "TINYINT", nullable = false)
    private Integer progressTime; //(1~24시간)

}
