package project.volunteer.domain.scheduleParticipation.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.scheduleParticipation.service.dto.CancelledParticipantList;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CancelledParticipantListResponse {
    List<CancelledParticipantList> cancelling;
}
