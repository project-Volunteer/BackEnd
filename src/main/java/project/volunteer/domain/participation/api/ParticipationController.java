package project.volunteer.domain.participation.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.volunteer.domain.participation.api.dto.ParticipantAddParam;
import project.volunteer.domain.participation.api.dto.ParticipantRemoveParam;
import project.volunteer.domain.participation.application.ParticipationService;
import project.volunteer.global.Interceptor.OrganizationAuth;
import project.volunteer.global.Interceptor.OrganizationAuth.Auth;
import project.volunteer.global.util.SecurityUtil;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recruitment")
public class ParticipationController {

    private final ParticipationService participationService;

    @PutMapping("/{recruitmentNo}/join")
    public ResponseEntity participationRequest(@PathVariable("recruitmentNo")Long no){

        participationService.participate(SecurityUtil.getLoginUserNo(), no);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{recruitmentNo}/cancel")
    public ResponseEntity participationCancel(@PathVariable("recruitmentNo")Long no){

        participationService.cancelParticipation(SecurityUtil.getLoginUserNo(), no);
        return ResponseEntity.ok().build();
    }

    @OrganizationAuth(auth = Auth.ORGANIZATION_ADMIN)
    @PutMapping("/{recruitmentNo}/approval")
    public ResponseEntity participantAdd(@RequestBody @Valid ParticipantAddParam dto, @PathVariable("recruitmentNo")Long no){

        participationService.approvalParticipant(no, dto.getUserNos());
        return ResponseEntity.ok().build();
    }

    @OrganizationAuth(auth = Auth.ORGANIZATION_ADMIN)
    @PutMapping("/{recruitmentNo}/kick")
    public ResponseEntity participantRemove(@RequestBody @Valid ParticipantRemoveParam dto, @PathVariable("recruitmentNo")Long no){

        participationService.deportParticipant(no, dto.getUserNo());
        return ResponseEntity.ok().build();
    }

}
