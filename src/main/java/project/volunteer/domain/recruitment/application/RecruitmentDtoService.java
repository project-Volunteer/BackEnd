package project.volunteer.domain.recruitment.application;

import project.volunteer.domain.recruitment.application.dto.RecruitmentDetails;

//화면에 맞춘 서비스 로직(읽기 전용)
public interface RecruitmentDtoService {

    public RecruitmentDetails findRecruitment(Long no);

    //봉사 모집글 팀원 신청 상태 확인 메서드
    public String findRecruitmentTeamStatus(Long recruitmentNo, Long loginUserNo);
}
