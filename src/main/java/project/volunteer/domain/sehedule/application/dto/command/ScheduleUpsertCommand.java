package project.volunteer.domain.sehedule.application.dto.command;

import lombok.*;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.common.component.Timetable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleUpsertCommand {
    private Timetable timetable;
    private String organizationName;
    private Address address;
    private String content;
    private Integer maxParticipationNum;

    public static ScheduleUpsertCommand of(String startDay, String endDay, String hourFormat, String startTime,
                                           int progressTime,
                                           String organizationName, String sido, String sigungu, String details,
                                           String fullName,
                                           String content, int maxParticipationNum) {
        Timetable timetable = Timetable.builder()
                .startDay(LocalDate.parse(startDay, DateTimeFormatter.ofPattern("MM-dd-yyyy")))
                .endDay(LocalDate.parse(endDay, DateTimeFormatter.ofPattern("MM-dd-yyyy")))
                .hourFormat(HourFormat.ofName(hourFormat))
                .startTime(LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm")))
                .progressTime(progressTime)
                .build();

        Address address = Address.builder()
                .sido(sido)
                .sigungu(sigungu)
                .details(details)
                .fullName(fullName)
                .build();

        return new ScheduleUpsertCommand(timetable, organizationName, address, content, maxParticipationNum);
    }

    public Schedule toDomain(Recruitment recruitment) {
        return Schedule.create(recruitment, timetable, content, organizationName, address, maxParticipationNum);
    }

}
