package project.volunteer.domain.scheduleParticipation.service;

import java.util.Optional;
import project.volunteer.domain.scheduleParticipation.service.dto.CancelledParticipantList;
import project.volunteer.domain.scheduleParticipation.service.dto.ParsingCompleteSchedule;
import project.volunteer.domain.scheduleParticipation.service.dto.CompletedParticipantList;
import project.volunteer.domain.scheduleParticipation.service.dto.ParticipatingParticipantList;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.global.common.component.ParticipantState;

import java.util.List;

public interface ScheduleParticipationDtoService {

    //봉사 일정 참여 상태 조회
    Optional<ParticipantState> searchState(Long scheduleNo, Long userNo);

    //참여 중 참가자 리스트 조회
    public List<ParticipatingParticipantList> findParticipatingParticipants(Schedule schedule);

    //취소 요청 참가자 리스트 조회
    public List<CancelledParticipantList> findCancelledParticipants(Schedule schedule);

    //참여 완료된(참여 미승인, 참여 승인) 참가자 리스트 조회
    public List<CompletedParticipantList> findCompletedParticipants(Schedule schedule);


    // 특정 유저의 참여 승인된 스케줄 리스트 조회
    public List<ParsingCompleteSchedule> findCompleteScheduleList(Long loginUserNo, ParticipantState state);
}
