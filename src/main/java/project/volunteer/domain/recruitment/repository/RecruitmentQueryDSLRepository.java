package project.volunteer.domain.recruitment.repository;

import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import project.volunteer.domain.recruitment.application.dto.query.list.RecruitmentSearchCond;
import project.volunteer.domain.recruitment.repository.dto.RecruitmentAndUserDetail;
import project.volunteer.domain.recruitment.application.dto.query.list.RecruitmentList;

public interface RecruitmentQueryDSLRepository {
    Optional<RecruitmentAndUserDetail> findRecruitmentAndUserDetailBy(Long recruitmentNo);

    Slice<RecruitmentList> findRecruitmentListBy(Pageable pageable, RecruitmentSearchCond searchCond);

    Slice<RecruitmentList> findRecruitmentListByTitle(Pageable pageable, String keyWard);

    Long findRecruitmentCountBy(RecruitmentSearchCond searchCond);

}
