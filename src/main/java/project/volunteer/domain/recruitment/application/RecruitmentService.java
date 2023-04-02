package project.volunteer.domain.recruitment.application;

import project.volunteer.domain.recruitment.application.dto.SaveRecruitDto;

public interface RecruitmentService {

    public Long addRecruitment(SaveRecruitDto saveDto);

    //public void findListRecruitment(SearchType searchType);

}
