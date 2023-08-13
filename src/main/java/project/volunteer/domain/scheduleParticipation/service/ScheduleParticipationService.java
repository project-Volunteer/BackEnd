package project.volunteer.domain.scheduleParticipation.service;

import java.util.List;

public interface ScheduleParticipationService {

    public void participate(Long recruitmentNo, Long scheduleNo, Long loginUserNo);

    public void cancel(Long scheduleNo, Long loginUserNo);

    public void approvalCancellation(Long scheduleNo, Long spNo);

    public void approvalCompletion(Long scheduleNo, List<Long> spNo);

    public void deleteScheduleParticipation(Long scheduleNo);
}