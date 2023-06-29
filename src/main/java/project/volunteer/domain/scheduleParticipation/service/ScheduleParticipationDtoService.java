package project.volunteer.domain.scheduleParticipation.service;

import project.volunteer.domain.scheduleParticipation.service.dto.CancelledParticipantList;
import project.volunteer.domain.scheduleParticipation.service.dto.CompletedParticipantList;
import project.volunteer.domain.scheduleParticipation.service.dto.ParticipatingParticipantList;

import java.util.List;

public interface ScheduleParticipationDtoService {

    //참여 중 참가자 리스트 조회
    public List<ParticipatingParticipantList> findParticipatingParticipants(Long scheduleNo);

    //취소 요청 참가자 리스트 조회
    public List<CancelledParticipantList> findCancelledParticipants(Long scheduleNo);

    //참여 완료된(참여 미승인, 참여 승인) 참가자 리스트 조회
    public List<CompletedParticipantList> findCompletedParticipants(Long scheduleNo);
}
