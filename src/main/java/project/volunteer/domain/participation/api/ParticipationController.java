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

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recruitment")
public class ParticipationController {

    private final ParticipationService participationService;

    @PostMapping("/join")
    public ResponseEntity participationRequest(@RequestBody @Valid ParticipationParam dto){

        participationService.participate(dto.getNo());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cancel")
    public ResponseEntity participationCancel(@RequestBody @Valid ParticipationParam dto){

        participationService.cancelParticipation(dto.getNo());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/approval")
    public ResponseEntity participantAdd(@RequestBody @Valid ParticipantAddParam dto){

        participationService.approvalParticipant(dto.getRecruitmentNo(), dto.getUserNos());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/kick")
    public ResponseEntity participantRemove(@RequestBody @Valid ParticipantRemoveParam dto){

        participationService.deportParticipant(dto.getRecruitmentNo(), dto.getUserNo());
        return ResponseEntity.ok().build();
    }

}
