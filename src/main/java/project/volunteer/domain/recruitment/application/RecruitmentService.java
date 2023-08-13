package project.volunteer.domain.recruitment.application;

import project.volunteer.domain.recruitment.application.dto.RecruitmentParam;
import project.volunteer.domain.recruitment.domain.Recruitment;

public interface RecruitmentService {

    public Recruitment addRecruitment(Long loginUserNo, RecruitmentParam saveDto);

    public void deleteRecruitment(Long deleteNo);
}
