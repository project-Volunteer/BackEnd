package project.volunteer.domain.scheduleParticipation.service.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CompletedParticipantsSearchResult {
    private List<CompletedParticipantDetail> done;
}
