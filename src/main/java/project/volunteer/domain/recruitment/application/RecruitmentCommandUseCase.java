package project.volunteer.domain.recruitment.application;

import project.volunteer.domain.recruitment.application.dto.command.RecruitmentCreateCommand;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.user.domain.User;

public interface RecruitmentCommandUseCase {

    Long addRecruitment(User writer, RecruitmentCreateCommand command);



    public void deleteRecruitment(Long deleteNo);

    //출팔된 봉사 모집글 찾는 메서드
    public Recruitment findPublishedRecruitment(Long recruitmentNo);

    //활동 중인 봉사 모집글 찾는 메서드
    public Recruitment findActivatedRecruitment(Long recruitmentNo);

    public void validRecruitmentOwner(Long recruitmentNo, Long loginUserNo);
}
