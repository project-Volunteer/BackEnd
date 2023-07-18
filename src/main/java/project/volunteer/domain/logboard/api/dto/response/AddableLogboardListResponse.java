package project.volunteer.domain.logboard.api.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.domain.scheduleParticipation.service.dto.ParsingCompleteSchedule;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddableLogboardListResponse {
	private List<ParsingCompleteSchedule> completedScheduleList;
	
}
