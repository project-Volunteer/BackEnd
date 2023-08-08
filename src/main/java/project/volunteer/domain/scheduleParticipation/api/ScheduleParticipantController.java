package project.volunteer.domain.scheduleParticipation.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.volunteer.domain.scheduleParticipation.api.dto.*;
import project.volunteer.domain.scheduleParticipation.service.ScheduleParticipationDtoService;
import project.volunteer.domain.scheduleParticipation.service.ScheduleParticipationService;
import project.volunteer.domain.scheduleParticipation.service.dto.CancelledParticipantList;
import project.volunteer.domain.scheduleParticipation.service.dto.CompletedParticipantList;
import project.volunteer.domain.scheduleParticipation.service.dto.ParticipatingParticipantList;
import project.volunteer.global.Interceptor.OrganizationAuth;
import project.volunteer.global.util.SecurityUtil;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recruitment")
public class ScheduleParticipantController {

    private final ScheduleParticipationService scheduleParticipationService;
    private final ScheduleParticipationDtoService scheduleParticipationDtoService;

    @OrganizationAuth(auth = OrganizationAuth.Auth.ORGANIZATION_TEAM)
    @PutMapping("/{recruitmentNo}/schedule/{scheduleNo}/join")
    public ResponseEntity scheduleParticipation(@PathVariable Long recruitmentNo, @PathVariable Long scheduleNo){

        scheduleParticipationService.participate(recruitmentNo, scheduleNo, SecurityUtil.getLoginUserNo());
        return ResponseEntity.ok().build();
    }

    @OrganizationAuth(auth = OrganizationAuth.Auth.ORGANIZATION_TEAM)
    @PutMapping("/{recruitmentNo}/schedule/{scheduleNo}/cancel")
    public ResponseEntity scheduleCancelRequest(@PathVariable Long recruitmentNo, @PathVariable Long scheduleNo){

        scheduleParticipationService.cancel(scheduleNo, SecurityUtil.getLoginUserNo());
        return ResponseEntity.ok().build();
    }

    @OrganizationAuth(auth = OrganizationAuth.Auth.ORGANIZATION_ADMIN)
    @PutMapping("/{recruitmentNo}/schedule/{scheduleNo}/cancelling")
    public ResponseEntity scheduleCancelApproval(@PathVariable Long recruitmentNo, @PathVariable Long scheduleNo,
                                                 @RequestBody @Valid CancelApproval dto){

        scheduleParticipationService.approvalCancellation(scheduleNo, dto.getNo());
        return ResponseEntity.ok().build();
    }

    @OrganizationAuth(auth = OrganizationAuth.Auth.ORGANIZATION_ADMIN)
    @PutMapping("/{recruitmentNo}/schedule/{scheduleNo}/complete")
    public ResponseEntity scheduleCompleteApproval(@PathVariable Long recruitmentNo, @PathVariable Long scheduleNo,
                                                   @RequestBody @Valid CompleteApproval dto){

        scheduleParticipationService.approvalCompletion(scheduleNo, dto.getCompletedList());
        return ResponseEntity.ok().build();
    }

    @OrganizationAuth(auth = OrganizationAuth.Auth.ORGANIZATION_ADMIN)
    @GetMapping("/{recruitmentNo}/schedule/{scheduleNo}/participating")
    public ResponseEntity<ParticipatingParticipantListResponse> scheduleParticipantList(@PathVariable Long recruitmentNo, @PathVariable Long scheduleNo){

        List<ParticipatingParticipantList> participating = scheduleParticipationDtoService.findParticipatingParticipants(scheduleNo);
        return ResponseEntity.ok(new ParticipatingParticipantListResponse(participating));
    }

    @OrganizationAuth(auth = OrganizationAuth.Auth.ORGANIZATION_ADMIN)
    @GetMapping("/{recruitmentNo}/schedule/{scheduleNo}/cancelling")
    public ResponseEntity<CancelledParticipantListResponse> scheduleCancelledParticipantList(@PathVariable Long recruitmentNo, @PathVariable Long scheduleNo){

        List<CancelledParticipantList> cancellingParticipants = scheduleParticipationDtoService.findCancelledParticipants(scheduleNo);
        return ResponseEntity.ok(new CancelledParticipantListResponse(cancellingParticipants));
    }

    @OrganizationAuth(auth = OrganizationAuth.Auth.ORGANIZATION_ADMIN)
    @GetMapping("/{recruitmentNo}/schedule/{scheduleNo}/completion")
    public ResponseEntity<CompletedParticipantListResponse> scheduleCompletedParticipantList(@PathVariable Long recruitmentNo, @PathVariable Long scheduleNo){

        List<CompletedParticipantList> completedParticipants = scheduleParticipationDtoService.findCompletedParticipants(scheduleNo);
        return ResponseEntity.ok(new CompletedParticipantListResponse(completedParticipants));
    }
}
