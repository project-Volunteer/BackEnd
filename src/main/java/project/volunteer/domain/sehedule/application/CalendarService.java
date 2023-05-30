package project.volunteer.domain.sehedule.application;

import project.volunteer.domain.sehedule.domain.Schedule;

import java.time.LocalDate;
import java.util.List;

public interface CalendarService {

    //캘린더 스케줄 리스트 조회
    public List<Schedule> findCalendarSchedules(Long recruitmentNo, Long loginUserNo, LocalDate startDay, LocalDate endDay);
}
