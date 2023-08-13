package project.volunteer.domain.scheduleParticipation.service;

import project.volunteer.domain.scheduleParticipation.service.dto.CancelledParticipantList;
import project.volunteer.domain.scheduleParticipation.service.dto.ParsingCompleteSchedule;
import project.volunteer.domain.scheduleParticipation.service.dto.CompletedParticipantList;
import project.volunteer.domain.scheduleParticipation.service.dto.ParticipatingParticipantList;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.ParticipantState;

import java.util.List;

public interface ScheduleParticipationDtoService {

    //봉사 일정 참여 상태 조회
    public String findScheduleParticipationState(Schedule schedule, User user);



    //참여 중 참가자 리스트 조회
    public List<ParticipatingParticipantList> findParticipatingParticipants(Long scheduleNo);

    //취소 요청 참가자 리스트 조회
    public List<CancelledParticipantList> findCancelledParticipants(Long scheduleNo);

    //참여 완료된(참여 미승인, 참여 승인) 참가자 리스트 조회
    public List<CompletedParticipantList> findCompletedParticipants(Long scheduleNo);
    
    // 특정 유저의 참여 승인된 스케줄 리스트 조회
    public List<ParsingCompleteSchedule> findCompleteScheduleList(Long loginUserNo, ParticipantState state);
}
