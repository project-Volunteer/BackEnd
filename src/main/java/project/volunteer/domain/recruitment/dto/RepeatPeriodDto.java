package project.volunteer.domain.recruitment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import project.volunteer.domain.recruitment.dao.queryDto.dto.RepeatPeriodQueryDto;
import project.volunteer.domain.repeatPeriod.domain.Day;
import project.volunteer.domain.repeatPeriod.domain.Period;
import project.volunteer.domain.repeatPeriod.domain.RepeatPeriod;
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

    public RepeatPeriodDto(List<RepeatPeriodQueryDto> queryDtos) {

        this.period = (!queryDtos.isEmpty())?queryDtos.get(0).getPeriod().getViewName() : null;

        this.week = (!queryDtos.isEmpty() && this.period.equals(Period.MONTH)) //반복주기가 매달일때만 반복주 존재
                ?queryDtos.get(0).getWeek().getViewName() : null;

        this.days = queryDtos.stream()
                .map(dto -> new String(dto.getDay().getViewName()))
                .collect(Collectors.toList());
    }

    public RepeatPeriodDto(Period period, Week week, List<Day> days){
        this.period = period.getViewName();
        this.week = week.getViewName();
        this.days = days.stream()
                .map(r -> new String(r.getViewName()))
                .collect(Collectors.toList());
    }

}
