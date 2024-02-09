package project.volunteer.domain.recruitment.repository;

import project.volunteer.domain.recruitment.repository.dto.RecruitmentAndUserDetail;

public interface RecruitmentQueryDSLRepository {
    RecruitmentAndUserDetail findRecruitmentAndUserDetailBy(Long recruitmentNo);

}
