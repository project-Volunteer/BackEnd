package project.volunteer.domain.scheduleParticipation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.scheduleParticipation.service.dto.ActiveParticipantsSearchResult;
import project.volunteer.domain.scheduleParticipation.service.dto.CancelledParticipantsSearchResult;
import project.volunteer.domain.scheduleParticipation.service.dto.CompletedParticipantsSearchResult;
import project.volunteer.domain.sehedule.application.ScheduleQueryUseCase;
import project.volunteer.domain.sehedule.domain.Schedule;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleParticipationQueryFacade {
    private final ScheduleQueryUseCase scheduleQueryUsecase;
    private final ScheduleParticipationQueryUseCase scheduleParticipationDtoService;

    public ActiveParticipantsSearchResult findActiveParticipants(final Long scheduleNo){
        Schedule schedule = scheduleQueryUsecase.findActivitedSchedule(scheduleNo);
        return scheduleParticipationDtoService.searchActiveParticipationList(schedule.getScheduleNo());
    }

    public CancelledParticipantsSearchResult findCancelledParticipants(final Long scheduleNo){
        Schedule schedule = scheduleQueryUsecase.findActivitedSchedule(scheduleNo);
        return scheduleParticipationDtoService.searchCancelledParticipationList(schedule.getScheduleNo());
    }

    public CompletedParticipantsSearchResult findCompletedParticipants(final Long scheduleNo){
        Schedule schedule = scheduleQueryUsecase.findActivitedSchedule(scheduleNo);
        return scheduleParticipationDtoService.searchCompletedParticipationList(schedule.getScheduleNo());
    }

}
