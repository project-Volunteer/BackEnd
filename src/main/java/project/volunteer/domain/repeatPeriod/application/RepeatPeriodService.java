package project.volunteer.domain.repeatPeriod.application;

import project.volunteer.domain.repeatPeriod.application.dto.SaveRepeatPeriodDto;
import project.volunteer.domain.repeatPeriod.domain.RepeatPeriod;

import java.util.List;

public interface RepeatPeriodService {

    public void addRepeatPeriod(Long recruitmentNo, SaveRepeatPeriodDto saveDto);

}
