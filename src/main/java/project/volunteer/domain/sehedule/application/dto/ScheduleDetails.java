package project.volunteer.domain.sehedule.application.dto;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.global.common.component.HourFormat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Setter
@Getter
@NoArgsConstructor
public class ScheduleDetails {

    private Long no;
    private Address address;
    private String startDay;
    private String startTime;
    private String hourFormat;
    private int progressTime;
    private int volunteerNum;
    private String content;
    private int activeVolunteerNum; //활동 중인 참여자 수
    private String state; //팀원의 일정 신청 상태

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    static class Address{
        String sido;
        String sigungu;
        String details;
        String fullName;
    }

    public static ScheduleDetails createScheduleDetails(Schedule schedule, String state) {

        ScheduleDetails scheduleDetails = new ScheduleDetails();
        scheduleDetails.no = schedule.getScheduleNo();
        scheduleDetails.address = new Address(
            schedule.getAddress().getSido(), schedule.getAddress().getSigungu(), schedule.getAddress().getDetails(), schedule.getAddress().getFullName());
        scheduleDetails.startDay = schedule.getScheduleTimeTable().getStartDay().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        scheduleDetails.startTime = schedule.getScheduleTimeTable().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        scheduleDetails.hourFormat = schedule.getScheduleTimeTable().getHourFormat().getDesc();
        scheduleDetails.progressTime = schedule.getScheduleTimeTable().getProgressTime();
        scheduleDetails.volunteerNum = schedule.getVolunteerNum();
        scheduleDetails.content = schedule.getContent();
        scheduleDetails.activeVolunteerNum = schedule.getCurrentVolunteerNum();
        scheduleDetails.state = state;
        return scheduleDetails;
    }

}
