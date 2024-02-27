package project.volunteer.domain.recruitment.application.dto.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Day;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Period;
import project.volunteer.domain.recruitment.domain.repeatPeriod.RepeatPeriod;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Week;

import java.util.List;
import java.util.stream.Collectors;
import project.volunteer.domain.recruitment.domain.repeatPeriod.validator.PeriodValidation;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RepeatPeriodCreateCommand {

    private Period period;
    private Week week;
    private List<Day> dayOfWeeks;

    public static RepeatPeriodCreateCommand of(String period, String week, List<String> dayOfWeeks) {
        return new RepeatPeriodCreateCommand(
                Period.of(period),
                Week.of(week),
                dayOfWeeks.stream()
                        .map(Day::of)
                        .collect(Collectors.toList()));
    }

    public List<RepeatPeriod> toDomains(PeriodValidation periodValidation) {
        return dayOfWeeks.stream()
                .map(dayOfWeek -> RepeatPeriod.create(period, week, dayOfWeek, periodValidation))
                .collect(Collectors.toList());
    }

}
