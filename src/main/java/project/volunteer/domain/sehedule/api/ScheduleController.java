package project.volunteer.domain.sehedule.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.volunteer.domain.sehedule.api.dto.request.ScheduleSave;
import project.volunteer.domain.sehedule.application.ScheduleDtoService;
import project.volunteer.domain.sehedule.application.ScheduleService;
import project.volunteer.domain.sehedule.application.dto.ScheduleDetails;
import project.volunteer.domain.sehedule.application.dto.ScheduleParam;
import project.volunteer.global.util.SecurityUtil;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/schedule")
public class ScheduleController {

    private final ScheduleDtoService scheduleDtoService;
    private final ScheduleService scheduleService;

    @GetMapping("/{recruitmentNo}")
    public ResponseEntity<ScheduleDetails> scheduleDetails(@PathVariable Long recruitmentNo){

        ScheduleDetails details = scheduleDtoService.findClosestSchedule(recruitmentNo, SecurityUtil.getLoginUserNo());
        return ResponseEntity.ok(details);
    }

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity scheduleAdd(@RequestBody @Valid ScheduleSave saveDto){

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
}
