package project.volunteer.domain.recruitment.application.dto.query.detail;

import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Period;
import project.volunteer.domain.recruitment.domain.repeatPeriod.RepeatPeriod;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Week;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RepeatPeriodDetail {
    private String period;
    private String week;
    private List<String> dayOfWeeks;

    public static RepeatPeriodDetail from(List<RepeatPeriod> repeatPeriods) {
        String period = repeatPeriods.get(0).getPeriod().getId();
        String week = repeatPeriods.get(0).getWeek().getId();
        List<String> days = repeatPeriods.stream()
                .map(repeatPeriod -> repeatPeriod.getDay().getId())
                .collect(Collectors.toList());
        return new RepeatPeriodDetail(period, week, days);
    }

    public static RepeatPeriodDetail init() {
        return new RepeatPeriodDetail(Period.NONE.getId(), Week.NONE.getId(), List.of());
    }

}
