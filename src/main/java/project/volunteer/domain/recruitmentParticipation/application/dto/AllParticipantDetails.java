package project.volunteer.domain.recruitmentParticipation.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import project.volunteer.domain.recruitment.application.dto.query.detail.ParticipantDetail;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AllParticipantDetails {
    private List<ParticipantDetail> approvalVolunteer = new ArrayList<>(); //승인된 참여자 리스트
    private List<ParticipantDetail> requiredVolunteer = new ArrayList<>(); //참여 요청한 참여자 리스트
}
