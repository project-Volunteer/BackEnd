package project.volunteer.domain.recruitment.dao.queryDto;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import project.volunteer.domain.recruitment.dao.queryDto.dto.RecruitmentListQuery;
import project.volunteer.domain.recruitment.dao.queryDto.dto.RecruitmentCond;

//서비스 화면에 맞춰진 Dto 반환 레포지토리
public interface RecruitmentQueryDtoRepository {

    //전체 모집글 리스트 조회(모집글,이미지,저장소,반복주기,참여자 매칭테이블)
    Slice<RecruitmentListQuery> findRecruitmentDtos(Pageable pageable, RecruitmentCond searchType);

    //전체 모집글,이미지,저장소 join 리스트 조회
    Slice<RecruitmentListQuery> findRecruitmentJoinImageBySearchType(Pageable pageable, RecruitmentCond searchType);

    //필터링에 따른 모집글 개수 카운트
    Long findRecruitmentCountBySearchType(RecruitmentCond searchType);
}

