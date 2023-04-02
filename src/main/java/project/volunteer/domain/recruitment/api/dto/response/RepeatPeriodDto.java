package project.volunteer.domain.recruitment.api.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.domain.recruitment.dao.queryDto.dto.RepeatPeriodQueryDto;
import project.volunteer.domain.repeatPeriod.domain.Period;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class RepeatPeriodDto {

    private String period;
    private String week;
    private List<String> days;

    public RepeatPeriodDto(List<RepeatPeriodQueryDto> queryDto) {

        this.period = (!queryDto.isEmpty())?queryDto.get(0).getPeriod().getViewName() : null;

        this.week = (!queryDto.isEmpty() && this.period.equals(Period.MONTH)) //반복주기가 매달일때만 반복주 존재
                ?queryDto.get(0).getWeek().getViewName() : null;

        this.days = queryDto.stream()
                .map(dto -> new String(dto.getDay().getViewName()))
                .collect(Collectors.toList());
    }
}
