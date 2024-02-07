package project.volunteer.domain.recruitment.application;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.recruitment.api.dto.response.RecruitmentListResponse;
import project.volunteer.domain.recruitment.application.dto.RecruitmentList;
import project.volunteer.domain.recruitment.application.dto.RepeatPeriodDetails;
import project.volunteer.domain.recruitment.dao.RepeatPeriodRepository;
import project.volunteer.domain.recruitment.dao.queryDto.RecruitmentQueryDtoRepository;
import project.volunteer.domain.recruitment.dao.queryDto.dto.RecruitmentCond;
import project.volunteer.domain.recruitment.dao.queryDto.dto.RecruitmentListQuery;
import project.volunteer.domain.recruitment.domain.Period;
import project.volunteer.domain.recruitment.domain.RepeatPeriod;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.domain.recruitment.application.dto.WriterDetails;
import project.volunteer.domain.recruitment.dto.PictureDetails;
import project.volunteer.domain.recruitment.application.dto.RecruitmentDetails;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecruitmentQueryService implements RecruitmentQueryUseCase {

    private final RecruitmentRepository recruitmentRepository;
    private final RecruitmentQueryDtoRepository recruitmentQueryDtoRepository;
    private final ImageRepository imageRepository;
    private final RepeatPeriodRepository repeatPeriodRepository;

    @Override
    public RecruitmentDetails findRecruitmentAndWriterDto(Long no) {
        //TODO: 쿼리 최적화 필요.
        //모집글 정보 + 모집글 작성자 정보 -> 쿼리 1번
        Recruitment findRecruitment = recruitmentRepository.findWriterEG(no).orElseThrow(()
                -> new BusinessException(ErrorCode.NOT_EXIST_RECRUITMENT, String.format("Search Recruitment NO = [%d]", no)));

        //모집글 정보 dto 세팅
        RecruitmentDetails dto = new RecruitmentDetails(findRecruitment);

        //모집글 이미지 dto 세팅 -> 쿼리 1번
        dto.setPicture(makeRecruitmentPictureDto(no));

        //모집글 작성자 dto 세팅 -> 쿼리 1번
        dto.setAuthor(makeWriterDto(findRecruitment.getWriter()));

        //정기 모집글 일 경우 반복주기 dto 세팅 -> 쿼리 1번
        if(findRecruitment.getVolunteeringType().equals(VolunteeringType.REG)){
            dto.setRepeatPeriod(makeRepeatPeriodDto(findRecruitment.getRecruitmentNo()));
        }

        return dto;
    }

    @Override
    public RecruitmentListResponse findSliceRecruitmentDtosByRecruitmentCond(Pageable pageable, RecruitmentCond cond) {
        Slice<RecruitmentListQuery> result = recruitmentQueryDtoRepository.findRecruitmentJoinImageBySearchType(pageable, cond);

        List<RecruitmentList> list = makeRecruitmentList(result.getContent());

        return new RecruitmentListResponse(list, result.isLast(), (list.isEmpty())?null:(list.get(list.size()-1).getNo()));
    }

    @Override
    public RecruitmentListResponse findSliceRecruitmentDtosByKeyWord(Pageable pageable, String keyWord) {
        Slice<RecruitmentListQuery> result = recruitmentQueryDtoRepository.findRecruitmentJoinImageByTitle(pageable, keyWord);

        List<RecruitmentList> list = makeRecruitmentList(result.getContent());

        return new RecruitmentListResponse(list, result.isLast(), (list.isEmpty())?null:(list.get(list.size()-1).getNo()));
    }

    private List<RecruitmentList> makeRecruitmentList(List<RecruitmentListQuery> content){
        return content.stream()
                .map(query -> {
                    //TODO: 현재 root 쿼리 1번 결과만큼(모집글 개수) 쿼리 N번(참여자 수 count 쿼리) 발생
                    //TODO: 추후 최적화가 필요한 부분
                    Long currentParticipantNum = recruitmentQueryDtoRepository.countParticipants(query.getNo());

                    RecruitmentList recruitmentList = RecruitmentList.createRecruitmentList(query, currentParticipantNum);

                    PictureDetails pictureDetails = null;
                    if (query.getUploadImage() == null) {
                        pictureDetails = new PictureDetails(true, null);
                    } else {
                        pictureDetails = new PictureDetails(false, query.getUploadImage());
                    }
                    recruitmentList.setPicture(pictureDetails);
                    return recruitmentList;
                }).collect(Collectors.toList());
    }

    private PictureDetails makeRecruitmentPictureDto(Long recruitmentNo){
        PictureDetails pictureDetails = null;

        Optional<Image> recruitmentImage = imageRepository.findEGStorageByCodeAndNo(RealWorkCode.RECRUITMENT, recruitmentNo);
        //업로드 이미지가 존재하는 경우
        if(recruitmentImage.isPresent()){
            pictureDetails = new PictureDetails(false, recruitmentImage.get().getStorage().getImagePath());
        }else{
            pictureDetails = new PictureDetails(true, null);
        }
        return pictureDetails;
    }

    private WriterDetails makeWriterDto(User writer) {
        //작성자 프로필 이미지(image + storage) -> 쿼리 1번
        //upload 이미지이므로 존재할 수도 있고, 없을수 있음.!
        Optional<Image> userUploadImage = imageRepository.findEGStorageByCodeAndNo(RealWorkCode.USER, writer.getUserNo());

        WriterDetails writerDetails  = null;
        if(userUploadImage.isPresent()){ //유저 프로필이 업로드 이미지인 경우
            writerDetails = new WriterDetails(writer.getNickName(), userUploadImage.get().getStorage().getImagePath());
        }else{                           //oauth 기본 프로필인 경우
            writerDetails = new WriterDetails(writer.getNickName(), writer.getPicture());
        }
        return writerDetails;
    }

    private RepeatPeriodDetails makeRepeatPeriodDto(Long recruitmentNo){
        List<RepeatPeriod> repeatPeriods = repeatPeriodRepository.findByRecruitment_RecruitmentNo(recruitmentNo);

        String period = repeatPeriods.get(0).getPeriod().getId();
        String week = (period.equals(Period.MONTH.getId()))?(repeatPeriods.get(0).getWeek().getId()):null;
        List<String> days = repeatPeriods.stream().map(r -> r.getDay().getId()).collect(Collectors.toList());

        return new RepeatPeriodDetails(period, week, days);
    }
}
