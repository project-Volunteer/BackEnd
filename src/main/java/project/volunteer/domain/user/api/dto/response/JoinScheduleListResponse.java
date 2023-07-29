package project.volunteer.domain.user.api.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class JoinScheduleListResponse {
    private List<JoinScheduleList> scheduleList;

    public JoinScheduleListResponse(List<JoinScheduleList> scheduleList) {
        this.scheduleList = scheduleList;
    }
}
