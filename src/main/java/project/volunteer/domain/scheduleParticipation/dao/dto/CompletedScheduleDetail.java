package project.volunteer.domain.scheduleParticipation.dao.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CompletedScheduleDetail {
    Long scheduleNo;
    String recruitmentTitle;
    LocalDate endDay;
}
