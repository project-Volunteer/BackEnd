package project.volunteer.domain.recruitment.application;

import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.participation.dao.ParticipantRepository;
import project.volunteer.domain.participation.dao.dto.RecruitmentParticipantDetail;
import project.volunteer.domain.recruitment.api.dto.response.RecruitmentListResponse;
import project.volunteer.domain.recruitment.application.dto.RecruitmentList;
import project.volunteer.domain.recruitment.application.dto.query.detail.RepeatPeriodDetail;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.repository.RepeatPeriodRepository;
import project.volunteer.domain.recruitment.repository.dto.RecruitmentAndUserDetail;
import project.volunteer.domain.recruitment.repository.queryDto.RecruitmentQueryDtoRepository;
import project.volunteer.domain.recruitment.repository.queryDto.dto.RecruitmentCond;
import project.volunteer.domain.recruitment.repository.queryDto.dto.RecruitmentListQuery;
import project.volunteer.domain.recruitment.domain.repeatPeriod.RepeatPeriod;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.domain.recruitment.application.dto.query.detail.PictureDetail;
import project.volunteer.domain.recruitment.application.dto.query.detail.RecruitmentDetailSearchResult;
import project.volunteer.domain.recruitment.repository.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecruitmentQueryService implements RecruitmentQueryUseCase {
    private final RecruitmentRepository recruitmentRepository;
    private final RepeatPeriodRepository repeatPeriodRepository;
    private final ParticipantRepository participantRepository;
    private final Clock clock;


    private final RecruitmentQueryDtoRepository recruitmentQueryDtoRepository;


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
        final RecruitmentAndUserDetail recruitmentDetail = recruitmentRepository.findRecruitmentAndUserDetailBy(recruitmentNo);
        final RepeatPeriodDetail repeatPeriodDetail = findRepeatPeriodDetail(recruitmentDetail.getVolunteeringType(), recruitmentNo);
        final List<RecruitmentParticipantDetail> participantsDetail = participantRepository.findParticipantsDetailBy(recruitmentNo,
                List.of(ParticipantState.JOIN_REQUEST, ParticipantState.JOIN_APPROVAL));

        return RecruitmentDetailSearchResult.of(recruitmentDetail, repeatPeriodDetail, participantsDetail);
    }

    private RepeatPeriodDetail findRepeatPeriodDetail(final VolunteeringType volunteeringType, final Long recruitmentNo) {
        if(volunteeringType.equals(VolunteeringType.REG)) {
            List<RepeatPeriod> repeatPeriods = repeatPeriodRepository.findByRecruitment_RecruitmentNo(recruitmentNo);
            return RepeatPeriodDetail.from(repeatPeriods);
        } else {
            return RepeatPeriodDetail.init();
        }
    }












    @Override
    public RecruitmentListResponse findSliceRecruitmentDtosByRecruitmentCond(Pageable pageable, RecruitmentCond cond) {
        Slice<RecruitmentListQuery> result = recruitmentQueryDtoRepository.findRecruitmentJoinImageBySearchType(
                pageable, cond);

        List<RecruitmentList> list = makeRecruitmentList(result.getContent());

        return new RecruitmentListResponse(list, result.isLast(),
                (list.isEmpty()) ? null : (list.get(list.size() - 1).getNo()));
    }

    @Override
    public RecruitmentListResponse findSliceRecruitmentDtosByKeyWord(Pageable pageable, String keyWord) {
        Slice<RecruitmentListQuery> result = recruitmentQueryDtoRepository.findRecruitmentJoinImageByTitle(pageable,
                keyWord);

        List<RecruitmentList> list = makeRecruitmentList(result.getContent());

        return new RecruitmentListResponse(list, result.isLast(),
                (list.isEmpty()) ? null : (list.get(list.size() - 1).getNo()));
    }

    private List<RecruitmentList> makeRecruitmentList(List<RecruitmentListQuery> content) {
        return content.stream()
                .map(query -> {
                    //TODO: 현재 root 쿼리 1번 결과만큼(모집글 개수) 쿼리 N번(참여자 수 count 쿼리) 발생
                    //TODO: 추후 최적화가 필요한 부분
                    Long currentParticipantNum = recruitmentQueryDtoRepository.countParticipants(query.getNo());

                    RecruitmentList recruitmentList = RecruitmentList.createRecruitmentList(query,
                            currentParticipantNum);

                    PictureDetail pictureDetails = null;
                    if (query.getUploadImage() == null) {
                        pictureDetails = new PictureDetail(true, null);
                    } else {
                        pictureDetails = new PictureDetail(false, query.getUploadImage());
                    }
                    recruitmentList.setPicture(pictureDetails);
                    return recruitmentList;
                }).collect(Collectors.toList());
    }

    @Override
    public void validRecruitmentOwner(Long recruitmentNo, Long loginUserNo) {
        Recruitment findRecruitment = recruitmentRepository.findById(recruitmentNo).orElseThrow(
                () -> new BusinessException(ErrorCode.NOT_EXIST_RECRUITMENT,
                        String.format("RecruitmentNo = [%d]", recruitmentNo)));

        if (!findRecruitment.isRecruitmentOwner(loginUserNo)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_RECRUITMENT,
                    String.format("RecruitmentNo = [%d], UserNo = [%d]", findRecruitment.getRecruitmentNo(),
                            loginUserNo));
        }
    }

}
