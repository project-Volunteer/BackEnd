package project.volunteer.domain.sehedule.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.volunteer.domain.sehedule.api.dto.request.ScheduleUpsertRequest;
import project.volunteer.domain.sehedule.api.dto.response.CalendarScheduleList;
import project.volunteer.domain.sehedule.api.dto.response.CalendarScheduleListResponse;
import project.volunteer.domain.sehedule.application.dto.ScheduleDetails;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.domain.sehedule.mapper.ScheduleFacade;
import project.volunteer.global.Interceptor.OrganizationAuth;
import project.volunteer.global.Interceptor.OrganizationAuth.Auth;
import project.volunteer.global.util.SecurityUtil;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recruitment")
public class ScheduleController {
    private final ScheduleFacade scheduleFacade;

    @OrganizationAuth(auth = Auth.ORGANIZATION_TEAM)
    @GetMapping(value = "/{recruitmentNo}/schedule", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ScheduleDetails> scheduleDetails(@PathVariable Long recruitmentNo){
        ScheduleDetails details = scheduleFacade.findClosestVolunteerPostSchedule(recruitmentNo, SecurityUtil.getLoginUserNo());

        return ResponseEntity.ok(details);
    }

    @OrganizationAuth(auth = Auth.ORGANIZATION_ADMIN)
    @PostMapping("/{recruitmentNo}/schedule")
    public ResponseEntity scheduleAdd(@RequestBody @Valid ScheduleUpsertRequest request,
                                      @PathVariable("recruitmentNo")Long no){

        scheduleFacade.registerVolunteerPostSchedule(no, request.toDto());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @OrganizationAuth(auth = Auth.ORGANIZATION_ADMIN)
    @PutMapping("/{recruitmentNo}/schedule/{scheduleNo}")
    public ResponseEntity scheduleEdit(@RequestBody @Valid ScheduleUpsertRequest request,
                                       @PathVariable("scheduleNo")Long scheduleNo,
                                       @PathVariable("recruitmentNo")Long recruitmentNo){

        scheduleFacade.editVolunteerPostSchedule(recruitmentNo, scheduleNo, request.toDto());

        return ResponseEntity.ok().build();
    }

    @OrganizationAuth(auth = Auth.ORGANIZATION_ADMIN)
    @DeleteMapping("/{recruitmentNo}/schedule/{scheduleNo}")
    public ResponseEntity scheduleDelete(@PathVariable("scheduleNo") Long scheduleNo,
                                         @PathVariable("recruitmentNo") Long recruitmentNo){

        scheduleFacade.deleteVolunteerPostSchedule(recruitmentNo, scheduleNo);
        return ResponseEntity.ok().build();
    }

    @OrganizationAuth(auth = Auth.ORGANIZATION_TEAM)
    @GetMapping("/{recruitmentNo}/calendar")
    public ResponseEntity<CalendarScheduleListResponse> scheduleList(@PathVariable("recruitmentNo")Long recruitmentNo,
                                       @RequestParam("year")Integer year,
                                       @RequestParam("mon")Integer mon){

        LocalDate startDay = LocalDate.of(year, mon, 1);
        LocalDate endDay = startDay.with(TemporalAdjusters.lastDayOfMonth());

        List<Schedule> calendarSchedules = scheduleFacade.findVolunteerPostCalendarSchedules(recruitmentNo, startDay, endDay);

        //response
        List<CalendarScheduleList> list = calendarSchedules.stream()
                .map(c -> CalendarScheduleList.createCalendarSchedule(c.getScheduleNo(), c.getScheduleTimeTable().getStartDay()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(new CalendarScheduleListResponse(list));
    }

    @OrganizationAuth(auth = Auth.ORGANIZATION_TEAM)
    @GetMapping("/{recruitmentNo}/calendar/{scheduleNo}")
    public ResponseEntity<ScheduleDetails> calendarScheduleDetails(@PathVariable("recruitmentNo")Long recruitmentNo,
                                                  @PathVariable("scheduleNo")Long scheduleNo){

        ScheduleDetails details = scheduleFacade.findVolunteerPostCalendarSchedule(SecurityUtil.getLoginUserNo(), recruitmentNo, scheduleNo);
        return ResponseEntity.ok(details);
    }
}
