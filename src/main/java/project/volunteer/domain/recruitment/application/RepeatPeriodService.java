package project.volunteer.domain.recruitment.application;

import project.volunteer.domain.recruitment.application.dto.RepeatPeriodParam;
import project.volunteer.domain.recruitment.domain.Recruitment;

public interface RepeatPeriodService {

    public void addRepeatPeriod(Recruitment recruitment, RepeatPeriodParam saveDto);

}
