package project.volunteer.domain.recruitmentParticipation.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitmentParticipation.application.dto.AllParticipantDetails;
import project.volunteer.domain.recruitment.application.dto.query.detail.ParticipantDetail;
import project.volunteer.domain.recruitmentParticipation.domain.RecruitmentParticipation;
import project.volunteer.domain.recruitmentParticipation.domain.RecruitmentParticipations;
import project.volunteer.domain.recruitmentParticipation.repository.RecruitmentParticipationRepository;
import project.volunteer.domain.recruitmentParticipation.repository.dto.RecruitmentParticipantDetail;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitmentParticipationServiceImpl implements RecruitmentParticipationService {
    private final RecruitmentParticipationRepository recruitmentParticipationRepository;

    @Transactional
    @Override
    public Long join(final User user, final Recruitment recruitment) {
        checkIsFull(recruitment);

        if (!recruitmentParticipationRepository.existsByRecruitmentAndUser(recruitment, user)) {
            RecruitmentParticipation newRecruitmentParticipation = new RecruitmentParticipation(recruitment, user,
                    ParticipantState.JOIN_REQUEST);
            return recruitmentParticipationRepository.save(newRecruitmentParticipation).getId();
        }

        RecruitmentParticipation recruitmentParticipation = findRecruitmentParticipation(recruitment, user);
        checkDuplicationJoin(recruitmentParticipation);
        recruitmentParticipation.changeState(ParticipantState.JOIN_REQUEST);
        return recruitmentParticipation.getId();
    }

    private void checkIsFull(final Recruitment recruitment) {
        if (recruitment.isFull()) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_CAPACITY,
                    String.format("RecruitmentNo=[%d], maxParticipant=[%d], currentParticipant=[%d]",
                            recruitment.getRecruitmentNo(), recruitment.getMaxParticipationNum(),
                            recruitment.getCurrentVolunteerNum()));
        }
    }

    private void checkDuplicationJoin(final RecruitmentParticipation recruitmentParticipation) {
        if (!recruitmentParticipation.canRejoin()) {
            throw new BusinessException(ErrorCode.DUPLICATE_RECRUITMENT_PARTICIPATION,
                    String.format("id = [%d], state = [%s]", recruitmentParticipation.getId(),
                            recruitmentParticipation.getState().getId()));
        }
    }

    @Transactional
    @Override
    public void cancelJoin(final User user, final Recruitment recruitment) {
        RecruitmentParticipation recruitmentParticipation = findRecruitmentParticipation(recruitment, user);
        checkCancelPossible(recruitmentParticipation);
        recruitmentParticipation.changeState(ParticipantState.JOIN_CANCEL);
    }

    private void checkCancelPossible(final RecruitmentParticipation recruitmentParticipation) {
        if (!recruitmentParticipation.canCancel()) {
            throw new BusinessException(ErrorCode.INVALID_STATE,
                    String.format("state=[%s]", recruitmentParticipation.getState().getId()));
        }
    }

    private RecruitmentParticipation findRecruitmentParticipation(final Recruitment recruitment, final User user) {
        return recruitmentParticipationRepository.findByRecruitmentAndUser(recruitment, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_RECRUITMENT_PARTICIPANT));
    }

    @Transactional
    @Override
    public void approveJoin(Recruitment recruitment, List<Long> recruitmentParticipationNos) {
        RecruitmentParticipations recruitmentParticipations = findRecruitmentParticipations(recruitmentParticipationNos);
        recruitmentParticipations.approve();

        recruitment.increaseParticipationNum(recruitmentParticipations.getSize());
    }

    @Transactional
    @Override
    public void deport(Recruitment recruitment, List<Long> recruitmentParticipationNos) {
        RecruitmentParticipations recruitmentParticipations = findRecruitmentParticipations(recruitmentParticipationNos);
        recruitmentParticipations.deport();

        recruitment.decreaseParticipationNum(recruitmentParticipations.getSize());
    }

    private RecruitmentParticipations findRecruitmentParticipations(List<Long> ids) {
        List<RecruitmentParticipation> participations = recruitmentParticipationRepository.findByIdIn(ids);
        return new RecruitmentParticipations(participations);
    }












    @Override
    public AllParticipantDetails findAllParticipantDto(Long recruitmentNo) {
        List<ParticipantDetail> approvedList = new ArrayList<>();
        List<ParticipantDetail> requiredList = new ArrayList<>();

        //최적화한 쿼리(쿼리 1번)
        List<RecruitmentParticipantDetail> participants = recruitmentParticipationRepository.findParticipantsDetailBy(
                recruitmentNo,
                List.of(ParticipantState.JOIN_REQUEST, ParticipantState.JOIN_APPROVAL));

        participants.stream()
                .forEach(p -> {
                    if (p.getState().equals(ParticipantState.JOIN_REQUEST)) {
                        requiredList.add(new ParticipantDetail(p.getRecruitmentParticipationNo(), p.getNickName(),
                                p.getImageUrl()));
                    } else {
                        approvedList.add(new ParticipantDetail(p.getRecruitmentParticipationNo(), p.getNickName(),
                                p.getImageUrl()));
                    }
                });

        return new AllParticipantDetails(approvedList, requiredList);
    }

    /**
     * L1 : 봉사 모집 기간 마감 L2 : 팀 신청, 팀 신청 승인 L3 : 팀 신청 인원 마감 L4 : 팀 신청 가능(팀 신청 취소, 팀 탈퇴, 팀 강제 탈퇴)
     */
//    @Override
//    public String findParticipationState(Recruitment recruitment, User user) {
//        Optional<Participant> findParticipant = participantRepository.findByRecruitmentAndParticipant(recruitment, user);
//
//        //봉사 모집 기간 만료
//        if(recruitment.isDone()){
//            return StateResponse.DONE.getId();
//        }
//
//        //팀 신청
//        if(findParticipant.isPresent() && findParticipant.get().isEqualState(ParticipantState.JOIN_REQUEST)){
//            return StateResponse.PENDING.getId();
//        }
//
//        //팀 신청 승인
//        if(findParticipant.isPresent() && findParticipant.get().isEqualState(ParticipantState.JOIN_APPROVAL)){
//            return StateResponse.APPROVED.getId();
//        }
//
//        //팀 신청 인원 마감
//        if(recruitment.isFull()){
//            return StateResponse.FULL.getId();
//        }
//
//        //팀 신청 가능(팀 신청 취소, 팀 탈퇴, 팀 강제 탈퇴, 신규 팀 신청)
//        return StateResponse.AVAILABLE.getId();
//    }
    @Override
    public RecruitmentParticipation findParticipation(Long recruitmentNo, Long userNo) {
        return recruitmentParticipationRepository.findByRecruitment_RecruitmentNoAndUser_UserNo(recruitmentNo, userNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_RECRUITMENT_PARTICIPANT,
                        String.format("RecruitmentNo = [%d], UserNo = [%d]", recruitmentNo, userNo)));
    }

    @Override
    @Transactional
    public void deleteParticipations(Long recruitmentNo) {
        //연관관계 끊기
        recruitmentParticipationRepository.findByRecruitment_RecruitmentNo(recruitmentNo)
                .forEach(p -> {
                    p.delete();
                    p.removeUserAndRecruitment();
                });
    }

}
