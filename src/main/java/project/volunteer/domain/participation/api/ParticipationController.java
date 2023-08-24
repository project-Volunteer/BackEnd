package project.volunteer.domain.participation.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.volunteer.domain.participation.api.dto.ParticipantAddParam;
import project.volunteer.domain.participation.api.dto.ParticipantRemoveParam;
import project.volunteer.domain.participation.mapper.ParticipationFacade;
import project.volunteer.global.Interceptor.OrganizationAuth;
import project.volunteer.global.Interceptor.OrganizationAuth.Auth;
import project.volunteer.global.util.SecurityUtil;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recruitment")
public class ParticipationController {
    private final ParticipationFacade participationFacade;

    @PutMapping("/{recruitmentNo}/join")
    public ResponseEntity participationRequest(@PathVariable("recruitmentNo")Long no){
        participationFacade.participateVolunteerTeam(SecurityUtil.getLoginUserNo(), no);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{recruitmentNo}/cancel")
    public ResponseEntity participationCancel(@PathVariable("recruitmentNo")Long no){
        participationFacade.cancelParticipationVolunteerTeam(SecurityUtil.getLoginUserNo(), no);
        return ResponseEntity.ok().build();
    }

    @OrganizationAuth(auth = Auth.ORGANIZATION_ADMIN)
    @PutMapping("/{recruitmentNo}/approval")
    public ResponseEntity participantAdd(@RequestBody @Valid ParticipantAddParam dto, @PathVariable("recruitmentNo")Long no){
        participationFacade.approvalParticipantVolunteerTeam(dto.getUserNos(), no);
        return ResponseEntity.ok().build();
    }

    @OrganizationAuth(auth = Auth.ORGANIZATION_ADMIN)
    @PutMapping("/{recruitmentNo}/kick")
    public ResponseEntity participantRemove(@RequestBody @Valid ParticipantRemoveParam dto, @PathVariable("recruitmentNo")Long no){
        participationFacade.deportParticipantVolunteerTeam(dto.getUserNo(), no);
        return ResponseEntity.ok().build();
    }

}
