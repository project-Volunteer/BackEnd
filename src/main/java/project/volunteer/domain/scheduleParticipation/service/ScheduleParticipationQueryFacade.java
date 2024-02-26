package project.volunteer.domain.scheduleParticipation.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.scheduleParticipation.service.dto.CancelledParticipantList;
import project.volunteer.domain.scheduleParticipation.service.dto.CompletedParticipantList;
import project.volunteer.domain.scheduleParticipation.service.dto.ParticipatingParticipantList;
import project.volunteer.domain.sehedule.application.ScheduleQueryUseCase;
import project.volunteer.domain.sehedule.domain.Schedule;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleParticipationQueryFacade {
    private final ScheduleQueryUseCase scheduleQueryUsecase;
    private final ScheduleParticipationQueryUseCase scheduleParticipationDtoService;

    public List<ParticipatingParticipantList> findScheduleParticipatingList(final Long scheduleNo){
        Schedule schedule = scheduleQueryUsecase.findActivitedSchedule(scheduleNo);
        return scheduleParticipationDtoService.searchParticipatingList(schedule.getScheduleNo());
    }







    public List<CancelledParticipantList> findCancelledParticipantsSchedule(Long scheduleNo){
        Schedule schedule = scheduleQueryUsecase.findActivitedSchedule(scheduleNo);

        return scheduleParticipationDtoService.findCancelledParticipants(schedule);
    }

    public List<CompletedParticipantList> findCompletedParticipantsSchedule(Long scheduleNo){
        Schedule schedule = scheduleQueryUsecase.findActivitedSchedule(scheduleNo);

        return scheduleParticipationDtoService.findCompletedParticipants(schedule);
    }

}
