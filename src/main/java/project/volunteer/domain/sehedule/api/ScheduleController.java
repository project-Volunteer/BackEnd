package project.volunteer.domain.sehedule.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.volunteer.domain.sehedule.api.dto.request.ScheduleUpsertRequest;
import project.volunteer.domain.sehedule.api.dto.response.ScheduleCalenderSearchResponses;
import project.volunteer.domain.sehedule.api.dto.response.ScheduleUpsertResponse;
import project.volunteer.domain.sehedule.application.ScheduleQueryFacade;
import project.volunteer.domain.sehedule.application.dto.query.ScheduleDetailSearchResult;
import project.volunteer.domain.sehedule.application.dto.query.ScheduleCalendarSearchResult;
import project.volunteer.domain.sehedule.application.ScheduleCommandFacade;
import project.volunteer.global.Interceptor.OrganizationAuth;
import project.volunteer.global.Interceptor.OrganizationAuth.Auth;
import project.volunteer.global.util.SecurityUtil;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recruitment")
public class ScheduleController {
    private final ScheduleCommandFacade scheduleCommandFacade;
    private final ScheduleQueryFacade scheduleQueryFacade;

    @OrganizationAuth(auth = Auth.ORGANIZATION_TEAM)
    @GetMapping(value = "/{recruitmentNo}/schedule", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ScheduleDetailSearchResult> closestScheduleDetails(@PathVariable Long recruitmentNo) {
        ScheduleDetailSearchResult details = scheduleQueryFacade.findClosestScheduleDetail(
                SecurityUtil.getLoginUserNo(),
                recruitmentNo);

        return ResponseEntity.ok(details);
    }

    @OrganizationAuth(auth = Auth.ORGANIZATION_ADMIN)
    @PostMapping("/{recruitmentNo}/schedule")
    public ResponseEntity<ScheduleUpsertResponse> scheduleAdd(@RequestBody @Valid ScheduleUpsertRequest request,
                                      @PathVariable("recruitmentNo") Long no) {

        Long scheduleNo = scheduleCommandFacade.registerSchedule(no, request.toCommand());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ScheduleUpsertResponse(scheduleNo));
    }

    @OrganizationAuth(auth = Auth.ORGANIZATION_ADMIN)
    @PutMapping("/{recruitmentNo}/schedule/{scheduleNo}")
    public ResponseEntity<Void> scheduleEdit(@RequestBody @Valid ScheduleUpsertRequest request,
                                       @PathVariable("scheduleNo") Long scheduleNo,
                                       @PathVariable("recruitmentNo") Long recruitmentNo) {

        scheduleCommandFacade.updateSchedule(recruitmentNo, scheduleNo, request.toCommand());
        return ResponseEntity.ok().build();
    }

    @OrganizationAuth(auth = Auth.ORGANIZATION_ADMIN)
    @DeleteMapping("/{recruitmentNo}/schedule/{scheduleNo}")
    public ResponseEntity scheduleDelete(@PathVariable("scheduleNo") Long scheduleNo,
                                         @PathVariable("recruitmentNo") Long recruitmentNo) {

        scheduleCommandFacade.deleteSchedule(scheduleNo);
        return ResponseEntity.ok().build();
    }

    @OrganizationAuth(auth = Auth.ORGANIZATION_TEAM)
    @GetMapping("/{recruitmentNo}/calendar")
    public ResponseEntity<ScheduleCalenderSearchResponses> calendarScheduleList(
            @PathVariable("recruitmentNo") Long recruitmentNo,
            @RequestParam("year") Integer year,
            @RequestParam("mon") Integer mon) {
        final LocalDate startDay = LocalDate.of(year, mon, 1);
        final LocalDate endDay = startDay.with(TemporalAdjusters.lastDayOfMonth());

        List<ScheduleCalendarSearchResult> result = scheduleQueryFacade.findScheduleCalendar(
                recruitmentNo, startDay, endDay);
        return ResponseEntity.ok(ScheduleCalenderSearchResponses.from(result));
    }

    @OrganizationAuth(auth = Auth.ORGANIZATION_TEAM)
    @GetMapping("/{recruitmentNo}/calendar/{scheduleNo}")
    public ResponseEntity<ScheduleDetailSearchResult> scheduleDetails(
            @PathVariable("recruitmentNo") Long recruitmentNo,
            @PathVariable("scheduleNo") Long scheduleNo) {

        ScheduleDetailSearchResult result = scheduleQueryFacade.findScheduleDetail(SecurityUtil.getLoginUserNo(),
                recruitmentNo, scheduleNo);
        return ResponseEntity.ok(result);
    }
}
