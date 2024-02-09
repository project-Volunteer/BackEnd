package project.volunteer.domain.recruitment.application;

import project.volunteer.domain.recruitment.application.dto.command.RepeatPeriodCreateCommand;
import project.volunteer.domain.recruitment.domain.Recruitment;

public interface RepeatPeriodService {

    public void addRepeatPeriod(Recruitment recruitment, RepeatPeriodCreateCommand saveDto);

    public void deleteRepeatPeriod(Long recruitmentNo);

}
