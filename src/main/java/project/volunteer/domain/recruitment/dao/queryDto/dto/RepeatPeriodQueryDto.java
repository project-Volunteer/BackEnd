package project.volunteer.domain.recruitment.dao.queryDto.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.domain.repeatPeriod.domain.Day;
import project.volunteer.domain.repeatPeriod.domain.Period;
import project.volunteer.domain.repeatPeriod.domain.Week;

@Setter
@Getter
@NoArgsConstructor
public class RepeatPeriodQueryDto {
    private Period period;
    private Week week;
    private Day day;

    @QueryProjection
    public RepeatPeriodQueryDto(Period period, Week week, Day day){
        this.period = period;
        this.week = week;
        this.day = day;
    }

}
