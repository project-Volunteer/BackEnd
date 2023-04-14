package project.volunteer.domain.sehedule.application.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.domain.repeatPeriod.application.dto.RepeatPeriodParam;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.Timetable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Setter
@Getter
@NoArgsConstructor
public class ScheduleParamReg {

    private Timetable timetable;
    private RepeatPeriodParam repeatPeriodParam;
    private String organizationName;
    private Address address;
    private String content;

    @Builder
    public ScheduleParamReg(String startDay, String endDay, String startTime, int progressTime,
                            String organizationName, String sido, String sigungu, String details, String content,
                            RepeatPeriodParam periodParam){
        this.timetable = new Timetable(
                LocalDate.parse(startDay, DateTimeFormatter.ofPattern("MM-dd-yyyy")),
                LocalDate.parse(endDay, DateTimeFormatter.ofPattern("MM-dd-yyyy")),
                LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm:ss")),
                progressTime
        );
        this.address = Address.builder()
                .sido(sido)
                .sigungu(sigungu)
                .details(details)
                .build();
        this.repeatPeriodParam = periodParam;
        this.organizationName = organizationName;
        this.content = content;
    }
}
