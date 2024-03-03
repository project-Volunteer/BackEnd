package project.volunteer.domain.scheduleParticipation.service;

import project.volunteer.domain.recruitmentParticipation.domain.RecruitmentParticipation;
import project.volunteer.domain.sehedule.domain.Schedule;

import java.util.List;

public interface ScheduleParticipationCommandUseCase {

    Long participate(Schedule schedule, RecruitmentParticipation participant);

    void cancelParticipation(Schedule schedule, RecruitmentParticipation participant);

    void approvalCancellation(Schedule schedule, List<Long> scheduleParticipationNos);

    void approvalParticipationCompletion(List<Long> scheduleParticipationNos);

    void deleteAllScheduleParticipationBySchedule(Long scheduleNo);

    void deleteAllScheduleParticipationByRecruitment(Long recruitmentNo);

}