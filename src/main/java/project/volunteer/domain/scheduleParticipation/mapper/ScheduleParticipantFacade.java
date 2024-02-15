package project.volunteer.domain.scheduleParticipation.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitmentParticipation.application.RecruitmentParticipationService;
import project.volunteer.domain.recruitmentParticipation.domain.RecruitmentParticipation;
import project.volunteer.domain.scheduleParticipation.service.ScheduleParticipationDtoService;
import project.volunteer.domain.scheduleParticipation.service.ScheduleParticipationService;
import project.volunteer.domain.scheduleParticipation.service.dto.CancelledParticipantList;
import project.volunteer.domain.scheduleParticipation.service.dto.CompletedParticipantList;
import project.volunteer.domain.scheduleParticipation.service.dto.ParticipatingParticipantList;
import project.volunteer.domain.sehedule.application.ScheduleCommandUseCase;
import project.volunteer.domain.sehedule.application.ScheduleQueryUseCase;
import project.volunteer.domain.sehedule.domain.Schedule;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleParticipantFacade {
    private final ScheduleCommandUseCase scheduleCommandService;
    private final ScheduleQueryUseCase scheduleQueryService;
    private final RecruitmentParticipationService participationService;
    private final ScheduleParticipationService scheduleParticipationService;
    private final ScheduleParticipationDtoService scheduleParticipationDtoService;

    @Transactional
    public void participateVolunteerPostSchedule(Long userNo, Long recruitmentNo, Long scheduleNo){
        Schedule schedule = scheduleQueryService.findScheduleInProgressWithPERSSIMITIC_WRITE_LOCK(scheduleNo);

        RecruitmentParticipation participation = participationService.findParticipation(recruitmentNo, userNo);

        scheduleParticipationService.participate(schedule, participation);
    }

    @Transactional
    public void cancelParticipationVolunteerPostSchedule(Long userNo, Long recruitmentNo, Long scheduleNo){
        Schedule schedule = scheduleQueryService.findScheduleInProgress(scheduleNo);

        RecruitmentParticipation participation = participationService.findParticipation(recruitmentNo, userNo);

        scheduleParticipationService.cancel(schedule, participation);
    }

    @Transactional
    public void approvalCancellationVolunteerPostSchedule(Long scheduleNo, Long scheduleParticipantNo){
        Schedule schedule = scheduleQueryService.findScheduleInProgress(scheduleNo);

        scheduleParticipationService.approvalCancellation(schedule, scheduleParticipantNo);
    }

    @Transactional
    public void approvalCompletionVolunteerPostSchedule(Long scheduleNo, List<Long> scheduleParticipantNos){
        scheduleQueryService.findActivitedSchedule(scheduleNo);

        scheduleParticipationService.approvalCompletion(scheduleParticipantNos);
    }

    public List<ParticipatingParticipantList> findParticipatingParticipantsSchedule(Long scheduleNo){
        Schedule schedule = scheduleQueryService.findActivitedSchedule(scheduleNo);

        return scheduleParticipationDtoService.findParticipatingParticipants(schedule);
    }

    public List<CancelledParticipantList> findCancelledParticipantsSchedule(Long scheduleNo){
        Schedule schedule = scheduleQueryService.findActivitedSchedule(scheduleNo);

        return scheduleParticipationDtoService.findCancelledParticipants(schedule);
    }

    public List<CompletedParticipantList> findCompletedParticipantsSchedule(Long scheduleNo){
        Schedule schedule = scheduleQueryService.findActivitedSchedule(scheduleNo);

        return scheduleParticipationDtoService.findCompletedParticipants(schedule);
    }
}
