package project.volunteer.domain.scheduleParticipation.service;

import project.volunteer.domain.scheduleParticipation.service.dto.ActiveParticipantsSearchResult;
import project.volunteer.domain.scheduleParticipation.service.dto.CancelledParticipantsSearchResult;
import project.volunteer.domain.scheduleParticipation.service.dto.CompletedParticipantsSearchResult;
import project.volunteer.domain.scheduleParticipation.service.dto.ParsingCompleteSchedule;
import project.volunteer.global.common.component.ParticipantState;

import java.util.List;

public interface ScheduleParticipationQueryUseCase {
    ActiveParticipantsSearchResult searchActiveParticipationList(Long scheduleNo);

    CancelledParticipantsSearchResult searchCancelledParticipationList(Long scheduleNo);

    // 참여 완료 미승인 and 참여 완료 승인
    CompletedParticipantsSearchResult searchCompletedParticipationList(Long scheduleNo);



    // 특정 유저의 참여 승인된 스케줄 리스트 조회
    public List<ParsingCompleteSchedule> findCompleteScheduleList(Long loginUserNo, ParticipantState state);
}
