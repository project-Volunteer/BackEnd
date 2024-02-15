package project.volunteer.domain.recruitmentParticipation.application;

import project.volunteer.domain.recruitmentParticipation.application.dto.AllParticipantDetails;
import project.volunteer.domain.recruitmentParticipation.domain.RecruitmentParticipation;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.user.domain.User;

import java.util.List;

public interface ParticipationService {

    //팀 참가 신청
    Long participate(User user, Recruitment recruitment);

    //팀 참가 취소
    public void cancelParticipation(User user, Recruitment recruitment);

    //팀 탈퇴(미정)

    //참가 승인(Only 방장)
    public void approvalParticipant(Recruitment recruitment, List<Long> recruitmentParticipationNos);

    //참여자 강제 탈퇴(Only 방장)
    public void deportParticipant(Recruitment recruitment, Long recruitmentParticipationNo);

    public AllParticipantDetails findAllParticipantDto(Long recruitmentNo);

//    String findParticipationState(Recruitment recruitment, User user);

    public RecruitmentParticipation findParticipation(Long recruitmentNo, Long userNo);

    public void deleteParticipations(Long recruitmentNo);
}
