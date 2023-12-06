package project.volunteer.domain.recruitment.application;

import org.springframework.data.domain.Pageable;
import project.volunteer.domain.recruitment.api.dto.response.RecruitmentListResponse;
import project.volunteer.domain.recruitment.application.dto.RecruitmentDetails;
import project.volunteer.domain.recruitment.dao.queryDto.dto.RecruitmentCond;

//화면에 맞춘 서비스 로직(읽기 전용)
public interface RecruitmentDtoService {

    public RecruitmentDetails findRecruitmentAndWriterDto(Long no);

    public RecruitmentListResponse findSliceRecruitmentDtosByRecruitmentCond(Pageable pageable, RecruitmentCond cond);

    public RecruitmentListResponse findSliceRecruitmentDtosByKeyWord(Pageable pageable, String keyWord);
}
