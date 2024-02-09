package project.volunteer.domain.recruitment.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.domain.recruitment.application.dto.query.detail.ParticipantDetail;
import project.volunteer.domain.recruitment.application.dto.query.detail.RecruitmentDetailSearchResult;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecruitmentDetailsResponse {
    private RecruitmentDetailSearchResult recruitment;
    private List<ParticipantDetail> approvalVolunteer;
    private List<ParticipantDetail> requiredVolunteer;
}
