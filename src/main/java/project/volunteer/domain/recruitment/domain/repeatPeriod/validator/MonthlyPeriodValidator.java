package project.volunteer.domain.recruitment.domain.repeatPeriod.validator;

import org.springframework.stereotype.Component;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Day;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Period;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Week;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

@Component
public class MonthlyPeriodValidator implements PeriodValidation {
    @Override
    public boolean isSupport(Period period) {
        return period.equals(Period.MONTH);
    }

    @Override
    public void validate(Period period, Week week, Day dayOfWeek) {
        if (period != Period.MONTH || week == Week.NONE || dayOfWeek == Day.NONE) {
            throw new BusinessException(ErrorCode.INVALID_PERIOD_PARAMETER,
                    String.format("Period=[%s], Week=[%s], DayOfWeek=[%s]", period.getId(), week.getId(),
                            dayOfWeek.getId()));
        }
    }
}
