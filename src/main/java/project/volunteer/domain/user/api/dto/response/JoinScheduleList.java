package project.volunteer.domain.user.api.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class JoinScheduleList {
    Long no;
    String startDay;
    String sido;
    String sigungu;
    String details;
    String organizationName;
    String startTime;
    String hourFormat;
    int progressTime;

    public JoinScheduleList(Long no, String startDay, String sido, String sigungu, String details,
                            String organizationName, String startTime, String hourFormat, int progressTime) {
        this.no = no;
        this.startDay = startDay;
        this.sido = sido;
        this.sigungu = sigungu;
        this.details = details;
        this.organizationName = organizationName;
        this.startTime = startTime;
        this.hourFormat = hourFormat;
        this.progressTime = progressTime;
    }
}
