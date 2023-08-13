package project.volunteer.domain.sehedule.application;

import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.sehedule.application.dto.ScheduleParam;
import project.volunteer.domain.sehedule.application.dto.ScheduleParamReg;
import project.volunteer.domain.sehedule.domain.Schedule;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleService {

    //스케줄 수동 등록
    public Schedule addSchedule(Recruitment recruitment, ScheduleParam dto);

    //스케줄 자동 등록(정기)
    public List<Long> addRegSchedule(Recruitment recruitment, ScheduleParamReg dto);

    //스케줄 정보 수정
    public Schedule editSchedule(Long scheduleNo, Recruitment recruitment, ScheduleParam dto);

    //스케줄 삭제
    public void deleteSchedule(Long scheduleNo);

    //캘린더 스케줄 리스트 조회
    public List<Schedule> findCalendarSchedules(Recruitment recruitment, LocalDate startDay, LocalDate endDay);
    public Schedule findCalendarSchedule(Long scheduleNo);

    //가장 가까운 스케줄 찾기
    public Schedule findClosestSchedule(Long recruitmentNo);


    //스케즐 완료 스케줄러
    public void scheduleParticipantStateUpdateProcess();
}
