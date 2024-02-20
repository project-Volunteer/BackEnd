package project.volunteer.domain.scheduleParticipation.repository;

import java.time.LocalDate;

public interface ScheduleParticipationQueryDSLRepository {
    void unApprovedCompleteOfAllFinishedScheduleParticipant(final LocalDate currentDate);
}
