package project.volunteer.domain.participation.application;

import java.util.List;

public interface ParticipationService {

    //팀 참가 신청
    public void participate(Long recruitmentNo);

    //팀 참가 취소
    public void cancelParticipation(Long recruitmentNo);

    //팀 탈퇴(미정)

    //참가 승인(Only 방장)
    public void approvalParticipant(Long recruitmentNo, List<Long> userNo);

    //참여자 강제 탈퇴(Only 방장)
    public void deportParticipant(Long recruitmentNo, Long userNo);

}
