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
import project.volunteer.domain.recruitment.dao.queryDto.RecruitmentQueryDtoRepository;
import project.volunteer.domain.recruitment.dao.queryDto.dto.RecruitmentCond;
import project.volunteer.domain.recruitment.dao.queryDto.dto.RecruitmentListQuery;
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
public class RecruitmentDtoServiceImpl implements RecruitmentDtoService{

    private final RecruitmentRepository recruitmentRepository;
    private final RecruitmentQueryDtoRepository recruitmentQueryDtoRepository;
    private final ImageRepository imageRepository;

    @Override
    public RecruitmentDetails findRecruitmentAndWriterDto(Long no) {
        //TODO: 모집글, 작성자, 모집글 이미지, 작성자 이미지 한번에 가져오는 쿼리로 최적화하기
        //TODO: ImageRepository 의존성 삭제 가능
        //모집글 정보 + 모집글 작성자 정보 -> 쿼리 1번
        Recruitment findRecruitment = recruitmentRepository.findWriterEG(no).orElseThrow(()
                -> new BusinessException(ErrorCode.NOT_EXIST_RECRUITMENT, String.format("Search Recruitment NO = [%d]", no)));

        //모집글 정보 dto 세팅
        RecruitmentDetails dto = new RecruitmentDetails(findRecruitment);

        //모집글 이미지 dto 세팅 -> 쿼리 1번
        makeRecruitmentImageDto(dto, no);

        //모집글 작성자 dto 세팅 -> 쿼리 1번
        makeWriterDto(dto, findRecruitment.getWriter());

        return dto;
    }

    @Override
    public RecruitmentListResponse findSliceOptimizerRecruitmentDtos(Pageable pageable, RecruitmentCond cond) {
        Slice<RecruitmentListQuery> result = recruitmentQueryDtoRepository.findRecruitmentDtos(pageable, cond);

        List<RecruitmentList> list = result.getContent().stream().
                map(query -> {
                    RecruitmentList recruitmentList = RecruitmentList.createRecruitmentList(query);
                    PictureDetails pictureDetails = null;
                    if(query.getUploadImage() == null){
                        pictureDetails = new PictureDetails(true, null);
                    }else{
                        pictureDetails = new PictureDetails(false, query.getUploadImage());
                    }
                    recruitmentList.setPicture(pictureDetails);
                    return recruitmentList;
                }).collect(Collectors.toList());

        return new RecruitmentListResponse(list, result.isLast(), (list.isEmpty())?null:(list.get(list.size()-1).getNo()));
    }

    private void makeRecruitmentImageDto(RecruitmentDetails dto, Long recruitmentNo){
        Optional<Image> recruitmentImage = imageRepository.findEGStorageByCodeAndNo(RealWorkCode.RECRUITMENT, recruitmentNo);
        //업로드 이미지가 존재하는 경우
        if(recruitmentImage.isPresent())
            dto.setPicture(new PictureDetails(false, recruitmentImage.get().getStorage().getImagePath()));
    }

    private void makeWriterDto(RecruitmentDetails dto, User writer) {
        //작성자 프로필 이미지(image + storage) -> 쿼리 1번
        //upload 이미지이므로 존재할 수도 있고, 없을수 있음.!
        Optional<Image> userUploadImage = imageRepository.findEGStorageByCodeAndNo(RealWorkCode.USER, writer.getUserNo());

        WriterDetails author  = null;
        if(userUploadImage.isPresent()){ //유저 프로필이 업로드 이미지인 경우
            author = new WriterDetails(writer.getNickName(), userUploadImage.get().getStorage().getImagePath());
        }else{                           //oauth 기본 프로필인 경우
            author = new WriterDetails(writer.getNickName(), writer.getPicture());
        }
        dto.setAuthor(author);
    }
}
