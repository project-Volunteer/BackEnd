package project.volunteer.domain.sehedule.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
public class SaveScheduleDto {

    private LocalDate startDay;
    private LocalTime startTime;
    private int progressTime; //(1~24시간)
    private String sido;
    private String sigungu;
    private String content;

    public SaveScheduleDto(String startDay, String startTime, int progressTime, String sido, String sigungu, String content){

        this.startDay = LocalDate.parse(startDay, DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        this.startTime = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm:ss"));
        this.progressTime = progressTime;
        this.sido = sido;
        this.sigungu = sigungu;
        this.content = content;
    }

}
