package project.volunteer.domain.recruitment.application;

import project.volunteer.domain.recruitment.application.dto.RepeatPeriodCommand;
import project.volunteer.domain.recruitment.domain.Recruitment;

public interface RepeatPeriodService {

    public void addRepeatPeriod(Recruitment recruitment, RepeatPeriodCommand saveDto);

    public void deleteRepeatPeriod(Long recruitmentNo);

}
