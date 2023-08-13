package project.volunteer.domain.recruitment.application;

import project.volunteer.domain.recruitment.application.dto.RecruitmentParam;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.user.domain.User;

public interface RecruitmentService {

    public Recruitment addRecruitment(User writer, RecruitmentParam saveDto);

    public void deleteRecruitment(Long deleteNo);

    //출팔된 봉사 모집글 찾는 메서드
    public Recruitment findPublishedRecruitment(Long recruitmentNo);

    //활동 중인 봉사 모집글 찾는 메서드
    public Recruitment findActivatedRecruitment(Long recruitmentNo);
}
