package project.volunteer.domain.recruitment.application;

import org.springframework.data.domain.Pageable;
import project.volunteer.domain.recruitment.api.dto.response.RecruitmentListResponse;
import project.volunteer.domain.recruitment.application.dto.RecruitmentDetails;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.repository.queryDto.dto.RecruitmentCond;

//화면에 맞춘 서비스 로직(읽기 전용)
public interface RecruitmentQueryUseCase {
    // 삭제 되지 않고, 출판된 봉사 모집글
    Recruitment findActivatedRecruitment(Long recruitmentNo);

    // 삭제되지 않은 && 출판된 && 모집 중인 봉사 모집글
    Recruitment findRecruitmentInProgress(Long recruitmentNo);







    RecruitmentDetails findRecruitmentAndWriterDto(Long no);

    RecruitmentListResponse findSliceRecruitmentDtosByRecruitmentCond(Pageable pageable, RecruitmentCond cond);

    RecruitmentListResponse findSliceRecruitmentDtosByKeyWord(Pageable pageable, String keyWord);

    void validRecruitmentOwner(Long recruitmentNo, Long loginUserNo);
}
