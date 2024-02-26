package project.volunteer.domain.scheduleParticipation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitmentParticipation.application.RecruitmentParticipationUseCase;
import project.volunteer.domain.recruitmentParticipation.domain.RecruitmentParticipation;
import project.volunteer.domain.sehedule.application.ScheduleQueryUseCase;
import project.volunteer.domain.sehedule.domain.Schedule;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleParticipationCommandFacade {
    private final ScheduleQueryUseCase scheduleQueryUsecase;
    private final RecruitmentParticipationUseCase recruitmentParticipationUseCase;
    private final ScheduleParticipationCommandUseCase scheduleParticipationService;

    public void participateSchedule(Long userNo, Long recruitmentNo, Long scheduleNo){
        Schedule schedule = scheduleQueryUsecase.findScheduleInProgressWithPERSSIMITIC_WRITE_LOCK(scheduleNo);
        RecruitmentParticipation recruitmentParticipation = recruitmentParticipationUseCase.findParticipation(recruitmentNo, userNo);
        scheduleParticipationService.participate(schedule, recruitmentParticipation);
    }

    public void cancelParticipationSchedule(Long userNo, Long recruitmentNo, Long scheduleNo){
        Schedule schedule = scheduleQueryUsecase.findScheduleInProgress(scheduleNo);
        RecruitmentParticipation recruitmentParticipation = recruitmentParticipationUseCase.findParticipation(recruitmentNo, userNo);
        scheduleParticipationService.cancelParticipation(schedule, recruitmentParticipation);
    }

    public void approvalCancellationSchedule(Long scheduleNo, List<Long> scheduleParticipantNo){
        Schedule schedule = scheduleQueryUsecase.findScheduleInProgress(scheduleNo);
        scheduleParticipationService.approvalCancellation(schedule, scheduleParticipantNo);
    }

    public void approvalParticipationCompletionSchedule(Long scheduleNo, List<Long> scheduleParticipantNos){
        scheduleQueryUsecase.findActivitedSchedule(scheduleNo);
        scheduleParticipationService.approvalParticipationCompletion(scheduleParticipantNos);
    }

}
