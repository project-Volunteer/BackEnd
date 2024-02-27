package project.volunteer.domain.recruitment.domain.repeatPeriod.validator;

import project.volunteer.domain.recruitment.domain.repeatPeriod.Day;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Period;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Week;

public interface PeriodValidation {
    boolean isSupport(Period period);
    void validate(Period period, Week week, Day dayOfWeek);

}
