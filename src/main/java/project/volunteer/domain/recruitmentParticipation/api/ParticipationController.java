package project.volunteer.domain.recruitmentParticipation.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.volunteer.domain.recruitmentParticipation.api.dto.request.ParticipantAddRequest;
import project.volunteer.domain.recruitmentParticipation.api.dto.request.ParticipantRemoveRequest;
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
    public ResponseEntity<Void> participationCancel(@PathVariable("recruitmentNo") Long no) {
        participationFacade.cancelJoinRecruitmentTeam(SecurityUtil.getLoginUserNo(), no);
        return ResponseEntity.ok().build();
    }

    @OrganizationAuth(auth = Auth.ORGANIZATION_ADMIN)
    @PutMapping("/{recruitmentNo}/approval")
    public ResponseEntity<Void> participantAdd(@RequestBody @Valid ParticipantAddRequest request,
                                         @PathVariable("recruitmentNo") Long no) {
        participationFacade.approveJoinRecruitmentTeam(request.getRecruitmentParticipationNos(), no);
        return ResponseEntity.ok().build();
    }

    @OrganizationAuth(auth = Auth.ORGANIZATION_ADMIN)
    @PutMapping("/{recruitmentNo}/kick")
    public ResponseEntity<Void> participantRemove(@RequestBody @Valid ParticipantRemoveRequest request,
                                            @PathVariable("recruitmentNo") Long no) {
        participationFacade.deportRecruitmentTeam(request.getRecruitmentParticipationNos(), no);
        return ResponseEntity.ok().build();
    }

}
