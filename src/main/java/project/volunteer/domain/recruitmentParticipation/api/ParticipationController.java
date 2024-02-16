package project.volunteer.domain.recruitmentParticipation.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.volunteer.domain.recruitmentParticipation.api.dto.request.ParticipantAddParam;
import project.volunteer.domain.recruitmentParticipation.api.dto.request.ParticipantRemoveParam;
import project.volunteer.domain.recruitmentParticipation.api.dto.response.JoinResponse;
import project.volunteer.domain.recruitmentParticipation.application.RecruitmentParticipationFacade;
import project.volunteer.global.Interceptor.OrganizationAuth;
import project.volunteer.global.Interceptor.OrganizationAuth.Auth;
import project.volunteer.global.util.SecurityUtil;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recruitment")
public class ParticipationController {
    private final RecruitmentParticipationFacade participationFacade;

    @PutMapping("/{recruitmentNo}/join")
    public ResponseEntity<JoinResponse> participationRequest(@PathVariable("recruitmentNo") Long no) {
        Long recruitmentParticipationNo = participationFacade.joinRecruitmentTeam(SecurityUtil.getLoginUserNo(),
                no);
        return ResponseEntity.ok(new JoinResponse(recruitmentParticipationNo));
    }

    @PutMapping("/{recruitmentNo}/cancel")
    public ResponseEntity participationCancel(@PathVariable("recruitmentNo") Long no) {
        participationFacade.cancelJoinRecruitmentTeam(SecurityUtil.getLoginUserNo(), no);
        return ResponseEntity.ok().build();
    }

    @OrganizationAuth(auth = Auth.ORGANIZATION_ADMIN)
    @PutMapping("/{recruitmentNo}/approval")
    public ResponseEntity participantAdd(@RequestBody @Valid ParticipantAddParam dto,
                                         @PathVariable("recruitmentNo") Long no) {
        participationFacade.approveJoinRecruitmentTeam(dto.getRecruitmentParticipationNos(), no);
        return ResponseEntity.ok().build();
    }

    @OrganizationAuth(auth = Auth.ORGANIZATION_ADMIN)
    @PutMapping("/{recruitmentNo}/kick")
    public ResponseEntity participantRemove(@RequestBody @Valid ParticipantRemoveParam dto,
                                            @PathVariable("recruitmentNo") Long no) {
        participationFacade.deportRecruitmentTeam(dto.getRecruitmentParticipationNos(), no);
        return ResponseEntity.ok().build();
    }

}
