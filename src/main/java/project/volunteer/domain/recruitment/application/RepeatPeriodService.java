package project.volunteer.domain.recruitment.application;

import project.volunteer.domain.recruitment.application.dto.RepeatPeriod;
import project.volunteer.domain.recruitment.domain.Recruitment;

public interface RepeatPeriodService {

    public void addRepeatPeriod(Recruitment recruitment, RepeatPeriod saveDto);

    public void deleteRepeatPeriod(Long recruitmentNo);

}
