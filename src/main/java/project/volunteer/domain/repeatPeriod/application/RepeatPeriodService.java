package project.volunteer.domain.repeatPeriod.application;

import project.volunteer.domain.repeatPeriod.dto.SaveRepeatPeriodDto;

public interface RepeatPeriodService {

    public void addRepeatPeriod(Long recruitmentNo, SaveRepeatPeriodDto saveDto);
}
