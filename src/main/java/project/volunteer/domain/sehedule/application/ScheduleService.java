package project.volunteer.domain.sehedule.application;

import project.volunteer.domain.sehedule.application.dto.ScheduleParam;
import project.volunteer.domain.sehedule.application.dto.ScheduleParamReg;
import project.volunteer.domain.sehedule.domain.Schedule;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleService {

    //스케줄 수동 등록
    public Long addSchedule(Long recruitmentNo, ScheduleParam dto);

    //스케줄 자동 등록(정기)
    public List<Long> addRegSchedule(Long recruitmentNo, ScheduleParamReg dto);

    //스케줄 정보 수정
    public Long editSchedule(Long scheduleNo, ScheduleParam dto);

    //스케줄 삭제
    public void deleteSchedule(Long scheduleNo);

    //캘린더 스케줄 리스트 조회
    public List<Schedule> findCalendarSchedules(Long recruitmentNo, LocalDate startDay, LocalDate endDay);

    //스케즐 완료
    public void scheduleParticipantStateUpdateProcess();

}
