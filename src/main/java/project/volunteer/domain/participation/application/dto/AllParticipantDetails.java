package project.volunteer.domain.participation.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AllParticipantDetails {
    private List<ParticipantDetails> approvalVolunteer = new ArrayList<>(); //승인된 참여자 리스트
    private List<ParticipantDetails> requiredVolunteer = new ArrayList<>(); //참여 요청한 참여자 리스트
}
