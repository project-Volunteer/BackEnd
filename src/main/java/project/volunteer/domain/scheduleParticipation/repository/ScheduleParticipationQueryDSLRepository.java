package project.volunteer.domain.scheduleParticipation.repository;

import java.time.LocalDate;
import java.util.List;
import project.volunteer.domain.scheduleParticipation.repository.dto.ScheduleParticipationDetail;
import project.volunteer.global.common.component.ParticipantState;

public interface ScheduleParticipationQueryDSLRepository {
    void unApprovedCompleteOfAllFinishedScheduleParticipant(final LocalDate currentDate);

    List<ScheduleParticipationDetail> findScheduleParticipationDetailBy(Long scheduleNo, List<ParticipantState> states);

}
