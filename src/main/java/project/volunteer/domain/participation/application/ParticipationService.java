package project.volunteer.domain.participation.application;

import project.volunteer.domain.participation.application.dto.AllParticipantDetails;
import project.volunteer.domain.recruitment.domain.Recruitment;

import java.util.List;

public interface ParticipationService {

    //팀 참가 신청
    public void participate(Long loginUserNo, Long recruitmentNo);

    //팀 참가 취소
    public void cancelParticipation(Long loginUserNo, Long recruitmentNo);

    //팀 탈퇴(미정)

    //참가 승인(Only 방장)
    public void approvalParticipant(Long recruitmentNo, List<Long> userNo);

    //참여자 강제 탈퇴(Only 방장)
    public void deportParticipant(Long recruitmentNo, Long userNo);



    public AllParticipantDetails findAllParticipantDto(Long recruitmentNo);

    public String findParticipationState(Recruitment recruitment, Long loginUserNo);
}
