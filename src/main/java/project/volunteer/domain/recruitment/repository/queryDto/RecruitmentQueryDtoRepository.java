package project.volunteer.domain.recruitment.repository.queryDto;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import project.volunteer.domain.recruitment.repository.queryDto.dto.RecruitmentListQuery;
import project.volunteer.domain.recruitment.repository.queryDto.dto.RecruitmentCond;

//서비스 화면에 맞춰진 Dto 반환 레포지토리
public interface RecruitmentQueryDtoRepository {

    //전체 모집글,이미지,저장소 join 리스트 조회
    Slice<RecruitmentListQuery> findRecruitmentJoinImageBySearchType(Pageable pageable, RecruitmentCond searchType);

    Slice<RecruitmentListQuery> findRecruitmentJoinImageByTitle(Pageable pageable, String title);

    //필터링에 따른 모집글 개수 카운트
    Long findRecruitmentCountBySearchType(RecruitmentCond searchType);

    //봉사 모집글 팀원 수 카운트
    Long countParticipants(Long recruitmentNo);
}

