package project.volunteer.domain.scheduleParticipation.domain;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

// 일급 컬렉션
public class ScheduleParticipations {
    private final List<ScheduleParticipation> scheduleParticipants;

    public ScheduleParticipations(List<ScheduleParticipation> scheduleParticipants) {
        Objects.requireNonNull(scheduleParticipants);
        scheduleParticipants.forEach(Objects::requireNonNull);
        this.scheduleParticipants = Collections.unmodifiableList(scheduleParticipants);
    }

    public int getSize() {
        return scheduleParticipants.size();
    }

    public void approvalCancellations() {
        validateApprovalCancellationPossible();
        scheduleParticipants.forEach(
                scheduleParticipant -> scheduleParticipant.changeState(ParticipantState.PARTICIPATION_CANCEL_APPROVAL));
    }

    private void validateApprovalCancellationPossible() {
        for (ScheduleParticipation scheduleParticipation : scheduleParticipants) {
            if (!scheduleParticipation.canApproveCancellation()) {
                throw new BusinessException(ErrorCode.INVALID_STATE, scheduleParticipation.toString());
            }
        }
    }

}
