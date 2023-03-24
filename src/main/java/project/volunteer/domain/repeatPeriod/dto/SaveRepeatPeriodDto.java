package project.volunteer.domain.repeatPeriod.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.domain.recruitment.api.form.SaveRecruitForm;
import project.volunteer.domain.repeatPeriod.domain.Day;
import project.volunteer.domain.repeatPeriod.domain.Period;
import project.volunteer.domain.repeatPeriod.domain.Week;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class SaveRepeatPeriodDto {

    private Period period;
    private Week week;
    private List<Day> days = new ArrayList<>();

    public SaveRepeatPeriodDto(String period, String week, List<String>days) {

        //주기 Enum 변환
        this.period = Period.of(period);

        //주기가 매월일 경우, 주 Enum 변환
        this.week = (this.period.name().equals(Period.MONTH.name()))
                ?(Week.of(week)):null;

        //요일 Enum 변환
        this.days = days.stream().
                map(day -> Day.of(day)).
                collect(Collectors.toList());
    }
}
