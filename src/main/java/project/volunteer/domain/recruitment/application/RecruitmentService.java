package project.volunteer.domain.recruitment.application;

import project.volunteer.domain.recruitment.dto.SaveRecruitDto;
import project.volunteer.domain.recruitment.dto.SearchType;

public interface RecruitmentService {

    public Long addRecruitment(SaveRecruitDto saveDto);

    //public void findListRecruitment(SearchType searchType);

}
