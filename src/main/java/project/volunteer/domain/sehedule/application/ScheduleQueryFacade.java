package project.volunteer.domain.sehedule.application;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitment.application.RecruitmentQueryService;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.sehedule.application.dto.query.ScheduleCalendarSearchResult;
import project.volunteer.domain.sehedule.application.dto.query.ScheduleDetailSearchResult;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleQueryFacade {
    private final RecruitmentQueryService recruitmentQueryService;
    private final ScheduleQueryUseCase scheduleQueryService;

    public List<ScheduleCalendarSearchResult> findScheduleCalendar(Long recruitmentNo, LocalDate startDay,
                                                                   LocalDate endDay) {
        Recruitment recruitment = recruitmentQueryService.findActivatedRecruitment(recruitmentNo);
        return scheduleQueryService.searchScheduleCalender(recruitment.getRecruitmentNo(), startDay, endDay);
    }

    public ScheduleDetailSearchResult findScheduleDetail(Long userNo, Long recruitmentNo, Long scheduleNo) {
        recruitmentQueryService.findActivatedRecruitment(recruitmentNo);
        return scheduleQueryService.searchScheduleDetail(userNo, scheduleNo);
    }

    public ScheduleDetailSearchResult findClosestScheduleDetail(Long userNo, Long recruitmentNo) {
        Recruitment recruitment = recruitmentQueryService.findActivatedRecruitment(recruitmentNo);
        return scheduleQueryService.searchClosestScheduleDetail(userNo, recruitment.getRecruitmentNo());
    }
}
