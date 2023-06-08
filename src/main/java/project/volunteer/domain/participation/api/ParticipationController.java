package project.volunteer.domain.participation.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.volunteer.domain.participation.api.dto.ParticipantAddParam;
import project.volunteer.domain.participation.api.dto.ParticipantRemoveParam;
import project.volunteer.domain.participation.api.dto.ParticipationParam;
import project.volunteer.domain.participation.application.ParticipationService;
import project.volunteer.global.util.SecurityUtil;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recruitment")
public class ParticipationController {

    private final ParticipationService participationService;

    @PostMapping("/join")
    public ResponseEntity participationRequest(@RequestBody @Valid ParticipationParam dto){

        participationService.participate(SecurityUtil.getLoginUserNo(), dto.getNo());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cancel")
    public ResponseEntity participationCancel(@RequestBody @Valid ParticipationParam dto){

        participationService.cancelParticipation(SecurityUtil.getLoginUserNo(), dto.getNo());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/approval")
    public ResponseEntity participantAdd(@RequestBody @Valid ParticipantAddParam dto){

        participationService.approvalParticipant(SecurityUtil.getLoginUserNo(), dto.getRecruitmentNo(), dto.getUserNos());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/kick")
    public ResponseEntity participantRemove(@RequestBody @Valid ParticipantRemoveParam dto){

        participationService.deportParticipant(SecurityUtil.getLoginUserNo(), dto.getRecruitmentNo(), dto.getUserNo());
        return ResponseEntity.ok().build();
    }

}
