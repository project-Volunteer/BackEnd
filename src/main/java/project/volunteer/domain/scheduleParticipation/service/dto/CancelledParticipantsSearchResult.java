package project.volunteer.domain.scheduleParticipation.service.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CancelledParticipantsSearchResult {
    private List<CancelledParticipantDetail> cancelling;

}
