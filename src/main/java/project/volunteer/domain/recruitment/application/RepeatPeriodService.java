package project.volunteer.domain.recruitment.application;

import project.volunteer.domain.recruitment.application.dto.RepeatPeriodParam;

public interface RepeatPeriodService {

    public void addRepeatPeriod(Long recruitmentNo, RepeatPeriodParam saveDto);

}
