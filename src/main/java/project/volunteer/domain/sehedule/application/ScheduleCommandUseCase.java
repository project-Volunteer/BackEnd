package project.volunteer.domain.sehedule.application;

import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.sehedule.application.dto.ScheduleUpsertCommand;
import project.volunteer.domain.sehedule.application.dto.RegularScheduleCreateCommand;

import java.util.List;

public interface ScheduleCommandUseCase {

    //스케줄 수동 등록
    Long addSchedule(Recruitment recruitment, ScheduleUpsertCommand dto);

    //스케줄 자동 등록(정기)
    List<Long> addRegularSchedule(Recruitment recruitment, RegularScheduleCreateCommand dto);

    //스케줄 정보 수정
    Long editSchedule(Long scheduleNo, Recruitment recruitment, ScheduleUpsertCommand dto);

    //스케줄 삭제
    void deleteSchedule(Long scheduleNo);

    void deleteAllSchedule(Long recruitmentNo);

}
