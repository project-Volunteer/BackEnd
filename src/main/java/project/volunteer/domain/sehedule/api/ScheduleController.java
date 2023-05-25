package project.volunteer.domain.sehedule.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.volunteer.domain.sehedule.application.ScheduleDtoService;
import project.volunteer.domain.sehedule.application.dto.ScheduleDetails;
import project.volunteer.global.util.SecurityUtil;

@RestController
@RequiredArgsConstructor
@RequestMapping("/schedule")
public class ScheduleController {

    private final ScheduleDtoService scheduleDtoService;

    @GetMapping("/{recruitmentNo}")
    public ResponseEntity<ScheduleDetails> scheduleDetails(@PathVariable Long recruitmentNo){

        ScheduleDetails details = scheduleDtoService.findClosestSchedule(recruitmentNo, SecurityUtil.getLoginUserNo());
        return ResponseEntity.ok(details);
    }
}
