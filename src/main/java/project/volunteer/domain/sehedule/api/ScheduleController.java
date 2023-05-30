package project.volunteer.domain.sehedule.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.volunteer.domain.sehedule.api.dto.request.ScheduleRequest;
import project.volunteer.domain.sehedule.api.dto.response.CalendarScheduleList;
import project.volunteer.domain.sehedule.application.ScheduleDtoService;
import project.volunteer.domain.sehedule.application.ScheduleService;
import project.volunteer.domain.sehedule.application.dto.ScheduleDetails;
import project.volunteer.domain.sehedule.application.dto.ScheduleParam;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.global.util.SecurityUtil;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/schedule")
public class ScheduleController {

    private final ScheduleDtoService scheduleDtoService;
    private final ScheduleService scheduleService;

    @GetMapping(value = "/{recruitmentNo}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ScheduleDetails> scheduleDetails(@PathVariable Long recruitmentNo){

        ScheduleDetails details = scheduleDtoService.findClosestSchedule(recruitmentNo, SecurityUtil.getLoginUserNo());
        return ResponseEntity.ok(details);
    }

    @PostMapping
    public ResponseEntity scheduleAdd(@RequestBody @Valid ScheduleRequest saveDto){

        Long saveScheduleNo = scheduleService.addSchedule(saveDto.getNo(), SecurityUtil.getLoginUserNo(),
                ScheduleParam.builder()
                        .startDay(saveDto.getStartDay())
                        .endDay(saveDto.getStartDay())
                        .hourFormat(saveDto.getHourFormat())
                        .startTime(saveDto.getStartTime())
                        .progressTime(saveDto.getProgressTime())
                        .organizationName(saveDto.getOrganizationName())
                        .sido(saveDto.getAddress().getSido())
                        .sigungu(saveDto.getAddress().getSigungu())
                        .details(saveDto.getAddress().getDetails())
                        .content(saveDto.getContent())
                        .volunteerNum(saveDto.getVolunteerNum())
                        .build());

        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity scheduleEdit(@RequestBody @Valid ScheduleRequest editDto){

        scheduleService.editSchedule(editDto.getNo(), SecurityUtil.getLoginUserNo(),
                ScheduleParam.builder()
                        .startDay(editDto.getStartDay())
                        .endDay(editDto.getStartDay())
                        .hourFormat(editDto.getHourFormat())
                        .startTime(editDto.getStartTime())
                        .progressTime(editDto.getProgressTime())
                        .organizationName(editDto.getOrganizationName())
                        .sido(editDto.getAddress().getSido())
                        .sigungu(editDto.getAddress().getSigungu())
                        .details(editDto.getAddress().getDetails())
                        .content(editDto.getContent())
                        .volunteerNum(editDto.getVolunteerNum())
                        .build());

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{scheduleNo}")
    public ResponseEntity scheduleDelete(@PathVariable Long scheduleNo){

        scheduleService.deleteSchedule(scheduleNo, SecurityUtil.getLoginUserNo());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{recruitmentNo}/calendar")
    public ResponseEntity<List<CalendarScheduleList>> scheduleList(@PathVariable("recruitmentNo")Long recruitmentNo,
                                       @RequestParam("year")Integer year,
                                       @RequestParam("mon")Integer mon){

        LocalDate startDay = LocalDate.of(year, mon, 1);
        LocalDate endDay = startDay.with(TemporalAdjusters.lastDayOfMonth());

        List<Schedule> calendarSchedules = scheduleService.findCalendarSchedules(recruitmentNo, SecurityUtil.getLoginUserNo(), startDay, endDay);

        //response
        List<CalendarScheduleList> list = calendarSchedules.stream()
                .map(c -> CalendarScheduleList.createCalendarSchedule(c.getScheduleNo(), c.getScheduleTimeTable().getStartDay()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
}
