package project.volunteer.domain.sehedule.application.dto.command;

import java.util.List;
import java.util.stream.Collectors;
import lombok.*;
import project.volunteer.domain.recruitment.application.dto.RepeatPeriodCommand;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.sehedule.domain.Schedule;
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
    private Timetable recruitmentTimetable;
    private RepeatPeriodCommand repeatPeriod;

    private String organizationName;
    private Address address;
    private String content;
    private int maxParticipationNum;

    public static RegularScheduleCreateCommand of(String startDay, String endDay, String hourFormat, String startTime,
                                                  int progressTime,
                                                  String organizationName, String sido, String sigungu, String details,
                                                  String fullName, String content, int maxParticipationNum,
                                                  RepeatPeriodCommand repeatPeriod) {
        Timetable recruitmentTimetable = Timetable.builder()
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

        return new RegularScheduleCreateCommand(recruitmentTimetable, repeatPeriod, organizationName, address, content,
                maxParticipationNum);
    }

    public List<Schedule> toDomains(List<Timetable> scheduleTimetable, Recruitment recruitment) {
        return scheduleTimetable.stream()
                .map(timetable -> {
                    Address newAddress = Address.builder()
                            .sido(address.getSido())
                            .sigungu(address.getSigungu())
                            .details(address.getDetails())
                            .fullName(address.getFullName())
                            .build();
                    return Schedule.create(recruitment, timetable, content, organizationName, newAddress,
                            maxParticipationNum);
                })
                .collect(Collectors.toList());
    }

}
