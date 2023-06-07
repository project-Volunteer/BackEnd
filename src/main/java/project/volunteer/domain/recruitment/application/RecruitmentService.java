package project.volunteer.domain.recruitment.application;

import project.volunteer.domain.recruitment.application.dto.RecruitmentParam;

public interface RecruitmentService {

    public Long addRecruitment(Long loginUserNo, RecruitmentParam saveDto);

    public void deleteRecruitment(Long loginUserNo, Long deleteNo);
}
