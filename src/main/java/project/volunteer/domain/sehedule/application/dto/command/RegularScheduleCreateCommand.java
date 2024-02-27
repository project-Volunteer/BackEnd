package project.volunteer.domain.sehedule.application.dto.command;

import java.util.List;
import java.util.stream.Collectors;
import lombok.*;
import project.volunteer.domain.recruitment.application.dto.command.RepeatPeriodCreateCommand;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.Timetable;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegularScheduleCreateCommand {
    private Timetable recruitmentTimetable;
    private RepeatPeriodCreateCommand repeatPeriod;

    private String organizationName;
    private Address address;
    private String content;
    private int maxParticipationNum;

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
