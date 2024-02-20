package project.volunteer.domain.scheduleParticipation.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitmentParticipation.application.RecruitmentParticipationUseCase;
import project.volunteer.domain.recruitmentParticipation.domain.RecruitmentParticipation;
import project.volunteer.domain.scheduleParticipation.service.ScheduleParticipationQueryUseCase;
import project.volunteer.domain.scheduleParticipation.service.ScheduleParticipationCommandUseCase;
import project.volunteer.domain.scheduleParticipation.service.dto.CancelledParticipantList;
import project.volunteer.domain.scheduleParticipation.service.dto.CompletedParticipantList;
import project.volunteer.domain.scheduleParticipation.service.dto.ParticipatingParticipantList;
import project.volunteer.domain.sehedule.application.ScheduleQueryUseCase;
import project.volunteer.domain.sehedule.domain.Schedule;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleParticipantFacade {
    private final ScheduleQueryUseCase scheduleQueryUsecase;
    private final RecruitmentParticipationUseCase recruitmentParticipationUseCase;
    private final ScheduleParticipationCommandUseCase scheduleParticipationService;
    private final ScheduleParticipationQueryUseCase scheduleParticipationDtoService;

    @Transactional
    public void participateVolunteerPostSchedule(Long userNo, Long recruitmentNo, Long scheduleNo){
        Schedule schedule = scheduleQueryUsecase.findScheduleInProgressWithPERSSIMITIC_WRITE_LOCK(scheduleNo);

        RecruitmentParticipation recruitmentParticipation = recruitmentParticipationUseCase.findParticipation(recruitmentNo, userNo);

        scheduleParticipationService.participate(schedule, recruitmentParticipation);
    }








    @Transactional
    public void cancelParticipationVolunteerPostSchedule(Long userNo, Long recruitmentNo, Long scheduleNo){
        Schedule schedule = scheduleQueryUsecase.findScheduleInProgress(scheduleNo);

        RecruitmentParticipation recruitmentParticipation = recruitmentParticipationUseCase.findParticipation(recruitmentNo, userNo);

        scheduleParticipationService.cancel(schedule, recruitmentParticipation);
    }

    @Transactional
    public void approvalCancellationVolunteerPostSchedule(Long scheduleNo, Long scheduleParticipantNo){
        Schedule schedule = scheduleQueryUsecase.findScheduleInProgress(scheduleNo);

        scheduleParticipationService.approvalCancellation(schedule, scheduleParticipantNo);
    }

    @Transactional
    public void approvalCompletionVolunteerPostSchedule(Long scheduleNo, List<Long> scheduleParticipantNos){
        scheduleQueryUsecase.findActivitedSchedule(scheduleNo);

        scheduleParticipationService.approvalCompletion(scheduleParticipantNos);
    }

    public List<ParticipatingParticipantList> findParticipatingParticipantsSchedule(Long scheduleNo){
        Schedule schedule = scheduleQueryUsecase.findActivitedSchedule(scheduleNo);

        return scheduleParticipationDtoService.findParticipatingParticipants(schedule);
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
