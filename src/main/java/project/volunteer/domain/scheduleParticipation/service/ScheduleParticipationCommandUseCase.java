package project.volunteer.domain.scheduleParticipation.service;

import project.volunteer.domain.recruitmentParticipation.domain.RecruitmentParticipation;
import project.volunteer.domain.sehedule.domain.Schedule;

import java.util.List;

public interface ScheduleParticipationCommandUseCase {

    Long participate(Schedule schedule, RecruitmentParticipation participant);





    public void cancel(Schedule schedule, RecruitmentParticipation participant);

    public void approvalCancellation(Schedule schedule, Long spNo);

    public void approvalCompletion(List<Long> spNo);

    public void deleteScheduleParticipation(Long scheduleNo);

    public void deleteAllScheduleParticipation(Long recruitmentNo);
}