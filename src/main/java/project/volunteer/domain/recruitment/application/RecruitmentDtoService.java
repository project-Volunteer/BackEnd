package project.volunteer.domain.recruitment.application;

import project.volunteer.domain.recruitment.application.dto.RecruitmentDto;

//화면에 맞춘 서비스 로직(읽기 전용)
public interface RecruitmentDtoService {

    public RecruitmentDto findRecruitment(Long no);
}
