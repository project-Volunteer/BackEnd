package project.volunteer.domain.sehedule.application;

import project.volunteer.domain.sehedule.dto.SaveScheduleDto;

public interface ScheduleService {

    public Long addSchedule(Long recruitmentNo, SaveScheduleDto dto);

}
