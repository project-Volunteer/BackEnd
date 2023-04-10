package project.volunteer.domain.sehedule.application;

import project.volunteer.domain.sehedule.application.dto.ScheduleParam;

public interface ScheduleService {

    public Long addSchedule(Long recruitmentNo, ScheduleParam dto);

}
