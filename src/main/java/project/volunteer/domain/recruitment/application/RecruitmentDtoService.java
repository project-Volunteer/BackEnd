package project.volunteer.domain.recruitment.application;

import org.springframework.data.domain.Pageable;
import project.volunteer.domain.recruitment.api.dto.response.RecruitmentListResponse;
import project.volunteer.domain.recruitment.application.dto.RecruitmentDetails;
import project.volunteer.domain.recruitment.dao.queryDto.dto.RecruitmentCond;

//화면에 맞춘 서비스 로직(읽기 전용)
public interface RecruitmentDtoService {

    public RecruitmentDetails findRecruitmentDto(Long no);

    public RecruitmentListResponse findRecruitmentDtos(Pageable pageable, RecruitmentCond cond);

    //봉사 모집글 팀원 신청 상태 확인 메서드
    public String findRecruitmentTeamStatus(Long recruitmentNo, Long loginUserNo);
}
