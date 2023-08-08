package project.volunteer.domain.scheduleParticipation.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class ParsingCompleteSchedule {
    Long no;
    String title;
    String date;
}
