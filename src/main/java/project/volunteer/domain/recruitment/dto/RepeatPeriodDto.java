package project.volunteer.domain.recruitment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import project.volunteer.domain.repeatPeriod.domain.Day;
import project.volunteer.domain.repeatPeriod.domain.Period;
import project.volunteer.domain.repeatPeriod.domain.Week;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class RepeatPeriodDto {

    private String period;
    private String week;
    private List<String> days;

    public RepeatPeriodDto(Period period, Week week, List<Day> days){
        this.period = period.getViewName();
        this.week = week.getViewName();
        this.days = days.stream()
                .map(r -> new String(r.getViewName()))
                .collect(Collectors.toList());
    }

}
