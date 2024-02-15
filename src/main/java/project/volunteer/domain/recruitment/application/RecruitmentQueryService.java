package project.volunteer.domain.recruitment.application;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitmentParticipation.repository.ParticipantRepository;
import project.volunteer.domain.recruitmentParticipation.repository.dto.RecruitmentParticipantDetail;
import project.volunteer.domain.recruitment.application.dto.query.RecruitmentCountResult;
import project.volunteer.domain.recruitment.application.dto.query.detail.RepeatPeriodDetail;
import project.volunteer.domain.recruitment.application.dto.query.list.RecruitmentListSearchResult;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.repository.RepeatPeriodRepository;
import project.volunteer.domain.recruitment.repository.dto.RecruitmentAndUserDetail;
import project.volunteer.domain.recruitment.application.dto.query.list.RecruitmentSearchCond;
import project.volunteer.domain.recruitment.application.dto.query.list.RecruitmentList;
import project.volunteer.domain.recruitment.domain.repeatPeriod.RepeatPeriod;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.domain.recruitment.application.dto.query.detail.RecruitmentDetailSearchResult;
import project.volunteer.domain.recruitment.repository.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.common.dto.StateResult;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecruitmentQueryService implements RecruitmentQueryUseCase {
    private final RecruitmentRepository recruitmentRepository;
    private final RepeatPeriodRepository repeatPeriodRepository;
    private final ParticipantRepository participantRepository;
    private final Clock clock;

    @Override
    public Recruitment findActivatedRecruitment(final Long recruitmentNo) {
        return recruitmentRepository.findRecruitmentBy(recruitmentNo, IsDeleted.N, true)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_RECRUITMENT,
                        String.format("RecruitmentNo = [%d]", recruitmentNo)));
    }

    @Override
    public Recruitment findRecruitmentInProgress(final Long recruitmentNo) {
        final Recruitment findRecruitment = recruitmentRepository.findRecruitmentBy(recruitmentNo, IsDeleted.N, true)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_RECRUITMENT,
                        String.format("RecruitmentNo = [%d]", recruitmentNo)));
        findRecruitment.checkDoneDate(LocalDate.now(clock));
        return findRecruitment;
    }

    @Override
    public RecruitmentDetailSearchResult searchRecruitmentDetail(final Long recruitmentNo) {
        final RecruitmentAndUserDetail recruitmentDetail = recruitmentRepository.findRecruitmentAndUserDetailBy(
                recruitmentNo);
        final RepeatPeriodDetail repeatPeriodDetail = findRepeatPeriodDetail(recruitmentDetail.getVolunteeringType(),
                recruitmentNo);
        final List<RecruitmentParticipantDetail> participantsDetail = participantRepository.findParticipantsDetailBy(
                recruitmentNo,
                List.of(ParticipantState.JOIN_REQUEST, ParticipantState.JOIN_APPROVAL));

        return RecruitmentDetailSearchResult.of(recruitmentDetail, repeatPeriodDetail, participantsDetail);
    }

    private RepeatPeriodDetail findRepeatPeriodDetail(final VolunteeringType volunteeringType,
                                                      final Long recruitmentNo) {
        if (volunteeringType.equals(VolunteeringType.REG)) {
            List<RepeatPeriod> repeatPeriods = repeatPeriodRepository.findByRecruitment_RecruitmentNo(recruitmentNo);
            return RepeatPeriodDetail.from(repeatPeriods);
        } else {
            return RepeatPeriodDetail.init();
        }
    }

    @Override
    public RecruitmentListSearchResult searchRecruitmentList(final Pageable pageable,
                                                             final RecruitmentSearchCond searchCond) {
        Slice<RecruitmentList> result = recruitmentRepository.findRecruitmentListBy(pageable, searchCond);
        return new RecruitmentListSearchResult(result.getContent(), result.isLast(),
                result.getContent().get(result.getContent().size() - 1).getNo());
    }

    @Override
    public RecruitmentListSearchResult searchRecruitmentList(final Pageable pageable, final String keyWord) {
        Slice<RecruitmentList> result = recruitmentRepository.findRecruitmentListByTitle(pageable, keyWord);
        return new RecruitmentListSearchResult(result.getContent(), result.isLast(),
                result.getContent().get(result.getContent().size() - 1).getNo());
    }

    @Override
    public RecruitmentCountResult searchRecruitmentCount(final RecruitmentSearchCond searchCond) {
        Long recruitmentCount = recruitmentRepository.findRecruitmentCountBy(searchCond);
        return new RecruitmentCountResult(recruitmentCount);
    }

    @Override
    public StateResult searchState(final Long userNo, final Long recruitmentNo) {
        Recruitment recruitment = recruitmentRepository.findRecruitmentBy(recruitmentNo, IsDeleted.N, true)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_RECRUITMENT,
                        String.format("RecruitmentNo = [%d]", recruitmentNo)));

        Optional<ParticipantState> state = participantRepository.findStateBy(recruitment.getRecruitmentNo(), userNo);
        return StateResult.getRecruitmentState(state, recruitment.isDone(LocalDate.now(clock)), recruitment.isFull());
    }

}
