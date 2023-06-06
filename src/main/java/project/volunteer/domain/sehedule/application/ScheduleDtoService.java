package project.volunteer.domain.sehedule.application;

import project.volunteer.domain.sehedule.application.dto.ScheduleDetails;

public interface ScheduleDtoService {

    //가장 가까운 스케줄 찾기
    public ScheduleDetails findClosestSchedule(Long recruitmentNo, Long loginUserNo);

    public ScheduleDetails findCalendarSchedule(Long recruitmentNo, Long scheduleNo, Long loginUserNo);

}
