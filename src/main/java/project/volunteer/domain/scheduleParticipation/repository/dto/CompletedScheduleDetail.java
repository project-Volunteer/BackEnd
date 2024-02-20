package project.volunteer.domain.scheduleParticipation.repository.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter @Setter
public class CompletedScheduleDetail {
    Long scheduleNo;
    String recruitmentTitle;
    LocalDate endDay;

    public CompletedScheduleDetail(Long scheduleNo, String recruitmentTitle, LocalDate endDay) {
        this.scheduleNo = scheduleNo;
        this.recruitmentTitle = recruitmentTitle;
        this.endDay = endDay;
    }
}
