package project.volunteer.domain.recruitment.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.domain.recruitment.domain.Day;
import project.volunteer.domain.recruitment.domain.Period;
import project.volunteer.domain.recruitment.domain.Week;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RepeatPeriodCommand {

    private Period period;
    private Week week;
    private List<Day> dayOfWeeks = new ArrayList<>();

    public RepeatPeriodCommand(String period, String week, List<String> dayOfWeeks) {
        this.period = Period.of(period);
        this.week = (this.period.equals(Period.MONTH))
                ? (Week.of(week)) : null;
        this.dayOfWeeks = dayOfWeeks.stream().
                map(Day::of).
                collect(Collectors.toList());
    }
}
