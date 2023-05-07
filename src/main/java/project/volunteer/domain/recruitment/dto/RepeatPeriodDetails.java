package project.volunteer.domain.recruitment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class RepeatPeriodDetails {

    private String period;
    private String week;
    private List<String> days;

    public RepeatPeriodDetails(String period, String week, List<String> days){
        this.period = period;
        this.week = week;
        this.days = days;
    }

}
