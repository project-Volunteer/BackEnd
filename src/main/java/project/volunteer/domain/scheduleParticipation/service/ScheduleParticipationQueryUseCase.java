package project.volunteer.domain.scheduleParticipation.service;

import project.volunteer.domain.scheduleParticipation.service.dto.CancelledParticipantList;
import project.volunteer.domain.scheduleParticipation.service.dto.ParsingCompleteSchedule;
import project.volunteer.domain.scheduleParticipation.service.dto.CompletedParticipantList;
import project.volunteer.domain.scheduleParticipation.service.dto.ParticipatingParticipantList;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.global.common.component.ParticipantState;

import java.util.List;

public interface ScheduleParticipationQueryUseCase {
    List<ParticipatingParticipantList> searchParticipatingList(Long scheduleNo);





    //취소 요청 참가자 리스트 조회
    public List<CancelledParticipantList> findCancelledParticipants(Schedule schedule);

    //참여 완료된(참여 미승인, 참여 승인) 참가자 리스트 조회
    public List<CompletedParticipantList> findCompletedParticipants(Schedule schedule);


    // 특정 유저의 참여 승인된 스케줄 리스트 조회
    public List<ParsingCompleteSchedule> findCompleteScheduleList(Long loginUserNo, ParticipantState state);
}
