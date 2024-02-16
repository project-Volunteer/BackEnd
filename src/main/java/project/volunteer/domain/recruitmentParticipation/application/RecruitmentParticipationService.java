package project.volunteer.domain.recruitmentParticipation.application;

import project.volunteer.domain.recruitmentParticipation.domain.RecruitmentParticipation;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.user.domain.User;

import java.util.List;

public interface RecruitmentParticipationService {
    Long join(User user, Recruitment recruitment);

    void cancelJoin(User user, Recruitment recruitment);

    void approveJoin(Recruitment recruitment, List<Long> recruitmentParticipationNos);

    // 강제 탈퇴
    void deport(Recruitment recruitment, List<Long> recruitmentParticipationNos);







    //팀 탈퇴(미정)

    RecruitmentParticipation findParticipation(Long recruitmentNo, Long userNo);

    void deleteParticipations(Long recruitmentNo);

}
