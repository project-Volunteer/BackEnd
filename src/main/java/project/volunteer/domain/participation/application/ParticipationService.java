package project.volunteer.domain.participation.application;

import project.volunteer.domain.participation.application.dto.AllParticipantDetails;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.user.domain.User;

import java.util.List;

public interface ParticipationService {

    //팀 참가 신청
    public void participate(User user, Recruitment recruitment);

    //팀 참가 취소
    public void cancelParticipation(User user, Recruitment recruitment);

    //팀 탈퇴(미정)

    //참가 승인(Only 방장)
    public void approvalParticipant(Recruitment recruitment, List<Long> userNo);

    //참여자 강제 탈퇴(Only 방장)
    public void deportParticipant(Recruitment recruitment, User user);

    public AllParticipantDetails findAllParticipantDto(Long recruitmentNo);

    public String findParticipationState(Recruitment recruitment, User user);

    public void deleteParticipations(Long recruitmentNo);
}
