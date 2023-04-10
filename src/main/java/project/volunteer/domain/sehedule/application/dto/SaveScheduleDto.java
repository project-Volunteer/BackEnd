package project.volunteer.domain.sehedule.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.global.common.component.Timetable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
public class SaveScheduleDto {

    private Timetable timetable;
    private String organizationName;
    private String sido;
    private String sigungu;
    private String details;
    private String content;

    public SaveScheduleDto(String startDay, String endDay, String startTime, int progressTime,
                           String organizationName, String sido, String sigungu, String details, String content){

        this.timetable = new Timetable(
                LocalDate.parse(startDay, DateTimeFormatter.ofPattern("MM-dd-yyyy")),
                LocalDate.parse(endDay, DateTimeFormatter.ofPattern("MM-dd-yyyy")),
                LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm:ss")),
                progressTime
        );
        this.sido = sido;
        this.sigungu = sigungu;
        this.organizationName = organizationName;
        this.details = details;
        this.content = content;
    }

}
