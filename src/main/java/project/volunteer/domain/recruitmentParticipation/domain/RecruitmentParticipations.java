package project.volunteer.domain.recruitmentParticipation.domain;

import java.util.List;
import java.util.Objects;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

// 일급 컬렉션
public class RecruitmentParticipations {
    private final List<RecruitmentParticipation> participants;

    public RecruitmentParticipations(List<RecruitmentParticipation> participants) {
        Objects.requireNonNull(participants);
        participants.forEach(Objects::requireNonNull);

        this.participants = participants;
    }

    public void approve() {
        validateApprovalPossible();
        participants.forEach(participant -> participant.changeState(ParticipantState.JOIN_APPROVAL));
    }

    private void validateApprovalPossible() {
        for(RecruitmentParticipation participant : participants) {
            if(!participant.canApproval()) {
                throw new BusinessException(ErrorCode.INVALID_STATE,
                        String.format("state=[%s]", participant.getState().getId()));
            }
        }
    }

}
