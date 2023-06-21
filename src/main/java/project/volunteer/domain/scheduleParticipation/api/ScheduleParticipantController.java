package project.volunteer.domain.scheduleParticipation.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.volunteer.domain.scheduleParticipation.api.dto.CancelApproval;
import project.volunteer.domain.scheduleParticipation.service.ScheduleParticipationService;
import project.volunteer.global.Interceptor.OrganizationAuth;
import project.volunteer.global.util.SecurityUtil;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recruitment")
public class ScheduleParticipantController {

    private final ScheduleParticipationService scheduleParticipationService;

    @OrganizationAuth(auth = OrganizationAuth.Auth.ORGANIZATION_TEAM)
    @PutMapping("/{recruitmentNo}/schedule/{scheduleNo}/join")
    public ResponseEntity scheduleParticipation(@PathVariable Long recruitmentNo, @PathVariable Long scheduleNo){

        scheduleParticipationService.participate(recruitmentNo, scheduleNo, SecurityUtil.getLoginUserNo());
        return ResponseEntity.ok().build();
    }

    @OrganizationAuth(auth = OrganizationAuth.Auth.ORGANIZATION_TEAM)
    @PutMapping("/{recruitmentNo}/schedule/{scheduleNo}/cancel")
    public ResponseEntity scheduleCancelRequest(@PathVariable Long recruitmentNo, @PathVariable Long scheduleNo){

        scheduleParticipationService.cancelRequest(scheduleNo, SecurityUtil.getLoginUserNo());
        return ResponseEntity.ok().build();
    }

    @OrganizationAuth(auth = OrganizationAuth.Auth.ORGANIZATION_ADMIN)
    @PutMapping("/{recruitmentNo}/schedule/{scheduleNo}/cancelling")
    public ResponseEntity scheduleCancelApproval(@PathVariable Long recruitmentNo, @PathVariable Long scheduleNo,
                                                 @RequestBody @Valid CancelApproval dto){

        scheduleParticipationService.cancelApproval(scheduleNo, dto.getNo());
        return ResponseEntity.ok().build();
    }
}
