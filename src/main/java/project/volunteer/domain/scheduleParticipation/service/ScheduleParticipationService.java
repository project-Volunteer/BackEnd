package project.volunteer.domain.scheduleParticipation.service;

public interface ScheduleParticipationService {

    public void participate(Long recruitmentNo, Long scheduleNo, Long loginUserNo);

    public void cancelRequest(Long scheduleNo, Long loginUserNo);

}
