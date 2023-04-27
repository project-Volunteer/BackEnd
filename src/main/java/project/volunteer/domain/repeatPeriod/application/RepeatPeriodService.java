package project.volunteer.domain.repeatPeriod.application;

import project.volunteer.domain.repeatPeriod.application.dto.RepeatPeriodParam;

public interface RepeatPeriodService {

    public void addRepeatPeriod(Long recruitmentNo, RepeatPeriodParam saveDto);

    public void deleteRepeatPeriod(Long recruitmentNo);

}
