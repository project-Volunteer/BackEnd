package project.volunteer.domain.recruitment.application;

import project.volunteer.domain.recruitment.application.dto.RecruitmentDetails;

//화면에 맞춘 서비스 로직(읽기 전용)
public interface RecruitmentDtoService {

    public RecruitmentDetails findRecruitment(Long no);
}
