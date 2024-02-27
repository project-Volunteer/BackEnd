package project.volunteer.domain.recruitment.application;

import project.volunteer.domain.recruitment.application.dto.command.RecruitmentCreateCommand;
import project.volunteer.domain.user.domain.User;

public interface RecruitmentCommandUseCase {

    Long addRecruitment(User writer, RecruitmentCreateCommand command);

    void deleteRecruitment(Long recruitmentNo);

}
