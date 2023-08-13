package project.volunteer.domain.recruitment.application;

import project.volunteer.domain.recruitment.application.dto.RepeatPeriodParam;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.dto.RepeatPeriodDetails;

public interface RepeatPeriodService {

    public void addRepeatPeriod(Recruitment recruitment, RepeatPeriodParam saveDto);

    public void deleteRepeatPeriod(Long recruitmentNo);

    public RepeatPeriodDetails findRepeatPeriodDto(Long recruitmentNo);
}
