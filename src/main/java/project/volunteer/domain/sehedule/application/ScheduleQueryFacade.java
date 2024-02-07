package project.volunteer.domain.sehedule.application;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitment.application.RecruitmentCommandUseCase;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.scheduleParticipation.service.ScheduleParticipationDtoService;
import project.volunteer.domain.sehedule.application.dto.query.ScheduleCalendarSearchResult;
import project.volunteer.domain.sehedule.application.dto.query.ScheduleDetailSearchResult;
import project.volunteer.global.common.component.ParticipantState;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleQueryFacade {
    private final RecruitmentCommandUseCase recruitmentService;
    private final ScheduleQueryUseCase scheduleQueryService;
    private final ScheduleParticipationDtoService scheduleParticipationDtoService;
    private final Clock clock;

    public List<ScheduleCalendarSearchResult> findScheduleCalendar(Long recruitmentNo, LocalDate startDay,
                                                                   LocalDate endDay) {
        Recruitment recruitment = recruitmentService.findPublishedRecruitment(recruitmentNo);
        return scheduleQueryService.searchScheduleCalender(recruitment, startDay, endDay);
    }

    public ScheduleDetailSearchResult findScheduleDetail(Long userNo, Long scheduleNo) {
        LocalDate now = LocalDate.now(clock);
        ScheduleDetailSearchResult scheduleSearchResult = scheduleQueryService.searchScheduleDetail(scheduleNo);
        Optional<ParticipantState> participantState = scheduleParticipationDtoService.searchState(scheduleNo, userNo);
        scheduleSearchResult.setResponseState(participantState, now);

        return scheduleSearchResult;
    }

    public ScheduleDetailSearchResult findClosestScheduleDetail(Long userNo, Long recruitmentNo) {
        LocalDate now = LocalDate.now(clock);
        ScheduleDetailSearchResult scheduleSearchResult = scheduleQueryService.searchClosestScheduleDetail(
                recruitmentNo, now);

        if (scheduleSearchResult.hasData()) {
            Optional<ParticipantState> participantState = scheduleParticipationDtoService.searchState(
                    scheduleSearchResult.getNo(), userNo);
            scheduleSearchResult.setResponseState(participantState, now);
        }

        return scheduleSearchResult;
    }
}
