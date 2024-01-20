package project.volunteer.domain.sehedule.application;

import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.sehedule.application.dto.ScheduleCreateCommand;
import project.volunteer.domain.sehedule.application.dto.ScheduleParamReg;
import project.volunteer.domain.sehedule.domain.Schedule;

import java.util.List;

public interface ScheduleCommandUseCase {

    //스케줄 수동 등록
    Long addSchedule(Recruitment recruitment, ScheduleCreateCommand dto);

    //스케줄 자동 등록(정기)
    List<Long> addRegSchedule(Recruitment recruitment, ScheduleParamReg dto);

    //스케줄 정보 수정
    Schedule editSchedule(Long scheduleNo, Recruitment recruitment, ScheduleCreateCommand dto);

    //스케줄 삭제
    void deleteSchedule(Long scheduleNo);
    void deleteAllSchedule(Long recruitmentNo);

    //스케즐 완료 스케줄러
    void scheduleParticipantStateUpdateProcess();
}
