package project.volunteer.domain.sehedule.application;

import project.volunteer.domain.sehedule.application.dto.ScheduleParam;
import project.volunteer.domain.sehedule.application.dto.ScheduleParamReg;
import project.volunteer.domain.sehedule.domain.Schedule;

public interface ScheduleService {

    //스케줄 수동 등록
    public Long addSchedule(Long recruitmentNo, Long loginUserNo, ScheduleParam dto);

    //스케줄 자동 등록(정기)
    public void addRegSchedule(Long recruitmentNo, ScheduleParamReg dto);

    //스케줄 정보 수정
    public Long editSchedule(Long scheduleNo, Long loginUserNo, ScheduleParam dto);
}
