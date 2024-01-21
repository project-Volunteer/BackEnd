package project.volunteer.domain.sehedule.application.dto;

import lombok.*;
import project.volunteer.domain.recruitment.application.dto.RepeatPeriod;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.common.component.Timetable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegularScheduleCreateCommand {

    private Timetable timetable;
    private RepeatPeriod repeatPeriod;

    private String organizationName;
    private Address address;
    private String content;
    private int maxParticipationNum;

    @Builder
    public RegularScheduleCreateCommand(String startDay, String endDay, String hourFormat, String startTime, int progressTime,
                                        String organizationName, String sido, String sigungu, String details, String fullName, String content, int maxParticipationNum,
                                        RepeatPeriod repeatPeriod){
        this.timetable = Timetable.builder()
                .startDay(LocalDate.parse(startDay, DateTimeFormatter.ofPattern("MM-dd-yyyy")))
                .endDay(LocalDate.parse(endDay, DateTimeFormatter.ofPattern("MM-dd-yyyy")))
                .hourFormat(HourFormat.ofName(hourFormat))
                .startTime(LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm")))
                .progressTime(progressTime)
                .build();
        this.address = Address.builder()
                .sido(sido)
                .sigungu(sigungu)
                .details(details)
            .fullName(fullName)
                .build();
        this.repeatPeriod = repeatPeriod;
        this.organizationName = organizationName;
        this.content = content;
        this.maxParticipationNum = maxParticipationNum;
    }
}
