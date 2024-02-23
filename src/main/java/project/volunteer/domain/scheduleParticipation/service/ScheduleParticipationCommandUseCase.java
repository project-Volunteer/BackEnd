package project.volunteer.domain.scheduleParticipation.service;

import project.volunteer.domain.recruitmentParticipation.domain.RecruitmentParticipation;
import project.volunteer.domain.sehedule.domain.Schedule;

import java.util.List;

public interface ScheduleParticipationCommandUseCase {

    Long participate(Schedule schedule, RecruitmentParticipation participant);

    void cancelParticipation(Schedule schedule, RecruitmentParticipation participant);

    void approvalCancellation(Schedule schedule, List<Long> scheduleParticipationNo);








    public void approvalCompletion(List<Long> spNo);

    public void deleteScheduleParticipation(Long scheduleNo);

    public void deleteAllScheduleParticipation(Long recruitmentNo);
}