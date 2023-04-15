package project.volunteer.domain.repeatPeriod.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.domain.repeatPeriod.domain.Day;
import project.volunteer.domain.repeatPeriod.domain.Period;
import project.volunteer.domain.repeatPeriod.domain.Week;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class RepeatPeriodParam {

    private Period period;
    private Week week;
    private List<Day> days = new ArrayList<>();

    public RepeatPeriodParam(String period, Integer week, List<Integer>days) {

        //주기 Enum 변환
        this.period = Period.of(period);

        //주기가 매월일 경우, 주 Enum 변환
        this.week = (this.period.name().equals(Period.MONTH.name()))
                ?(Week.ofValue(week)):null;

        //요일 Enum 변환
        this.days = days.stream().
                map(day -> Day.ofValue(day)).
                collect(Collectors.toList());
    }
}
