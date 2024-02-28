package project.volunteer.domain.scheduleParticipation.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.domain.scheduleParticipation.repository.dto.ScheduleParticipationDetail;
import project.volunteer.global.common.component.ParticipantState;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CompletedParticipantDetail {
    private Long scheduleParticipationNo;
    private String nickname;
    private String email;
    private String profile;
    private Boolean isApproved; // 참여 완료 승인 여부

    public static CompletedParticipantDetail from(ScheduleParticipationDetail detail) {
        Boolean isApproved = detail.getState().equals(ParticipantState.PARTICIPATION_COMPLETE_APPROVAL);
        return new CompletedParticipantDetail(detail.getScheduleParticipationNo(), detail.getNickname(),
                detail.getEmail(), detail.getProfile(), isApproved);
    }

}
