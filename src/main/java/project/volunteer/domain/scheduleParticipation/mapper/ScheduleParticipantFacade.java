package project.volunteer.domain.scheduleParticipation.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.participation.application.ParticipationService;
import project.volunteer.domain.participation.domain.Participant;
import project.volunteer.domain.scheduleParticipation.service.ScheduleParticipationDtoService;
import project.volunteer.domain.scheduleParticipation.service.ScheduleParticipationService;
import project.volunteer.domain.scheduleParticipation.service.dto.CancelledParticipantList;
import project.volunteer.domain.scheduleParticipation.service.dto.CompletedParticipantList;
import project.volunteer.domain.scheduleParticipation.service.dto.ParticipatingParticipantList;
import project.volunteer.domain.sehedule.application.ScheduleService;
import project.volunteer.domain.sehedule.domain.Schedule;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleParticipantFacade {
    private final ScheduleService scheduleService;
    private final ParticipationService participationService;
    private final ScheduleParticipationService scheduleParticipationService;
    private final ScheduleParticipationDtoService scheduleParticipationDtoService;

    @Transactional
    public void participateVolunteerPostSchedule(Long userNo, Long recruitmentNo, Long scheduleNo){
        Schedule schedule = scheduleService.findActivatedScheduleWithPERSSIMITIC_WRITE_Lock(scheduleNo);

        Participant participation = participationService.findParticipation(recruitmentNo, userNo);

        scheduleParticipationService.participate(schedule, participation);
    }

    @Transactional
    public void cancelParticipationVolunteerPostSchedule(Long userNo, Long recruitmentNo, Long scheduleNo){
        Schedule schedule = scheduleService.findActivatedSchedule(scheduleNo);

        Participant participation = participationService.findParticipation(recruitmentNo, userNo);

        scheduleParticipationService.cancel(schedule, participation);
    }

    @Transactional
    public void approvalCancellationVolunteerPostSchedule(Long scheduleNo, Long scheduleParticipantNo){
        Schedule schedule = scheduleService.findActivatedSchedule(scheduleNo);

        scheduleParticipationService.approvalCancellation(schedule, scheduleParticipantNo);
    }

    @Transactional
    public void approvalCompletionVolunteerPostSchedule(Long scheduleNo, List<Long> scheduleParticipantNos){
        scheduleService.findPublishedSchedule(scheduleNo);

        scheduleParticipationService.approvalCompletion(scheduleParticipantNos);
    }

    public List<ParticipatingParticipantList> findParticipatingParticipantsSchedule(Long scheduleNo){
        Schedule schedule = scheduleService.findPublishedSchedule(scheduleNo);

        return scheduleParticipationDtoService.findParticipatingParticipants(schedule);
    }

    public List<CancelledParticipantList> findCancelledParticipantsSchedule(Long scheduleNo){
        Schedule schedule = scheduleService.findPublishedSchedule(scheduleNo);

        return scheduleParticipationDtoService.findCancelledParticipants(schedule);
    }

    public List<CompletedParticipantList> findCompletedParticipantsSchedule(Long scheduleNo){
        Schedule schedule = scheduleService.findPublishedSchedule(scheduleNo);

        return scheduleParticipationDtoService.findCompletedParticipants(schedule);
    }
}
