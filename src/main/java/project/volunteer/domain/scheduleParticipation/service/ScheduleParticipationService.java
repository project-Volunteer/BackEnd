package project.volunteer.domain.scheduleParticipation.service;

import project.volunteer.domain.recruitmentParticipation.domain.Participant;
import project.volunteer.domain.sehedule.domain.Schedule;

import java.util.List;

public interface ScheduleParticipationService {

    public void participate(Schedule schedule, Participant participant);

    public void cancel(Schedule schedule, Participant participant);

    public void approvalCancellation(Schedule schedule, Long spNo);

    public void approvalCompletion(List<Long> spNo);

    public void deleteScheduleParticipation(Long scheduleNo);

    public void deleteAllScheduleParticipation(Long recruitmentNo);
}