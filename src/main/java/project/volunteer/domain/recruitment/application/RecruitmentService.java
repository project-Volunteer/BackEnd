package project.volunteer.domain.recruitment.application;

import project.volunteer.domain.recruitment.application.dto.RecruitmentParam;

public interface RecruitmentService {

    public Long addRecruitment(RecruitmentParam saveDto);

    public void deleteRecruitment(Long deleteNo);
}
