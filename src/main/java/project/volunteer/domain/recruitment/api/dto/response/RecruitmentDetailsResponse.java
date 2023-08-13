package project.volunteer.domain.recruitment.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.domain.participation.application.dto.ParticipantDetails;
import project.volunteer.domain.recruitment.application.dto.RecruitmentDetails;
import project.volunteer.domain.recruitment.dto.RepeatPeriodDetails;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecruitmentDetailsResponse {
    private RecruitmentDetails recruitment;
    private List<ParticipantDetails> approvalVolunteer;
    private List<ParticipantDetails> requiredVolunteer;
}
