package project.volunteer.domain.scheduleParticipation.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.volunteer.domain.scheduleParticipation.api.dto.*;
import project.volunteer.domain.scheduleParticipation.service.ScheduleParticipationCommandFacade;
import project.volunteer.domain.scheduleParticipation.service.ScheduleParticipationQueryFacade;
import project.volunteer.domain.scheduleParticipation.service.dto.ActiveParticipantsSearchResult;
import project.volunteer.domain.scheduleParticipation.service.dto.CancelledParticipantsSearchResult;
import project.volunteer.domain.scheduleParticipation.service.dto.CompletedParticipantDetail;
import project.volunteer.domain.scheduleParticipation.service.dto.CompletedParticipantsSearchResult;
import project.volunteer.global.Interceptor.OrganizationAuth;
import project.volunteer.global.util.SecurityUtil;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recruitment")
public class ScheduleParticipantController {
    private final ScheduleParticipationCommandFacade scheduleParticipationCommandFacade;
    private final ScheduleParticipationQueryFacade scheduleParticipationQueryFacade;

    @OrganizationAuth(auth = OrganizationAuth.Auth.ORGANIZATION_TEAM)
    @PutMapping("/{recruitmentNo}/schedule/{scheduleNo}/join")
    public ResponseEntity scheduleParticipation(@PathVariable Long recruitmentNo, @PathVariable Long scheduleNo){
        scheduleParticipationCommandFacade.participateSchedule(SecurityUtil.getLoginUserNo(), recruitmentNo, scheduleNo);
        return ResponseEntity.ok().build();
    }

    @OrganizationAuth(auth = OrganizationAuth.Auth.ORGANIZATION_TEAM)
    @PutMapping("/{recruitmentNo}/schedule/{scheduleNo}/cancel")
    public ResponseEntity scheduleCancelRequest(@PathVariable Long recruitmentNo, @PathVariable Long scheduleNo){
        scheduleParticipationCommandFacade.cancelParticipationSchedule(SecurityUtil.getLoginUserNo(), recruitmentNo, scheduleNo);
        return ResponseEntity.ok().build();
    }

    @OrganizationAuth(auth = OrganizationAuth.Auth.ORGANIZATION_ADMIN)
    @PutMapping("/{recruitmentNo}/schedule/{scheduleNo}/cancelling")
    public ResponseEntity scheduleCancelApproval(@PathVariable Long recruitmentNo, @PathVariable Long scheduleNo,
                                                 @RequestBody @Valid CancelApproval dto){
        scheduleParticipationCommandFacade.approvalCancellationSchedule(scheduleNo, dto.getNo());
        return ResponseEntity.ok().build();
    }

    @OrganizationAuth(auth = OrganizationAuth.Auth.ORGANIZATION_ADMIN)
    @PutMapping("/{recruitmentNo}/schedule/{scheduleNo}/complete")
    public ResponseEntity scheduleCompleteApproval(@PathVariable Long recruitmentNo, @PathVariable Long scheduleNo,
                                                   @RequestBody @Valid CompleteApproval dto){

        scheduleParticipationCommandFacade.approvalParticipationCompletionSchedule(scheduleNo, dto.getCompletedList());
        return ResponseEntity.ok().build();
    }

    @OrganizationAuth(auth = OrganizationAuth.Auth.ORGANIZATION_ADMIN)
    @GetMapping("/{recruitmentNo}/schedule/{scheduleNo}/participating")
    public ResponseEntity<ActiveParticipantsSearchResult> scheduleParticipantList(@PathVariable Long recruitmentNo, @PathVariable Long scheduleNo){
        ActiveParticipantsSearchResult result = scheduleParticipationQueryFacade.findActiveParticipants(scheduleNo);
        return ResponseEntity.ok(result);
    }

    @OrganizationAuth(auth = OrganizationAuth.Auth.ORGANIZATION_ADMIN)
    @GetMapping("/{recruitmentNo}/schedule/{scheduleNo}/cancelling")
    public ResponseEntity<CancelledParticipantsSearchResult> scheduleCancelledParticipantList(@PathVariable Long recruitmentNo, @PathVariable Long scheduleNo){
        CancelledParticipantsSearchResult result = scheduleParticipationQueryFacade.findCancelledParticipants(scheduleNo);
        return ResponseEntity.ok(result);
    }

    @OrganizationAuth(auth = OrganizationAuth.Auth.ORGANIZATION_ADMIN)
    @GetMapping("/{recruitmentNo}/schedule/{scheduleNo}/completion")
    public ResponseEntity<CompletedParticipantsSearchResult> scheduleCompletedParticipantList(@PathVariable Long recruitmentNo, @PathVariable Long scheduleNo){

        CompletedParticipantsSearchResult result = scheduleParticipationQueryFacade.findCompletedParticipants(scheduleNo);
        return ResponseEntity.ok(result);
    }
}
