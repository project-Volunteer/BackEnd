package project.volunteer.domain.recruitment.application;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.image.domain.RealWorkCode;
import project.volunteer.domain.participation.dao.ParticipantRepository;
import project.volunteer.domain.participation.domain.Participant;
import project.volunteer.domain.recruitment.application.dto.ParticipantDetails;
import project.volunteer.domain.recruitment.application.dto.WriterDetails;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.dto.PictureDetails;
import project.volunteer.domain.recruitment.application.dto.RecruitmentDetails;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.dto.RepeatPeriodDetails;
import project.volunteer.domain.repeatPeriod.dao.RepeatPeriodRepository;
import project.volunteer.domain.repeatPeriod.domain.Period;
import project.volunteer.domain.repeatPeriod.domain.RepeatPeriod;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.State;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecruitmentDtoServiceImpl implements RecruitmentDtoService{

    private final RecruitmentRepository recruitmentRepository;
    private final ImageRepository imageRepository;
    private final RepeatPeriodRepository repeatPeriodRepository;
    private final ParticipantRepository participantRepository;

    @Override
    public RecruitmentDetails findRecruitment(Long no) {

        //모집글 정보 + 모집글 작성자 정보 -> 쿼리1번
        Recruitment findRecruitment = recruitmentRepository.findEGWriterAndRecruitment(no).orElseThrow(()
                -> new BusinessException(ErrorCode.NOT_EXIST_RECRUITMENT, String.format("Search Recruitment NO = [%d]", no)));
        User writer = findRecruitment.getWriter();

        //1. 모집글 정보 dto 생성
        RecruitmentDetails dto = new RecruitmentDetails(findRecruitment);

        //모집글 이미지(image + storage) -> 쿼리 1번
        //모집글 이미지는 반드시 존재!
        Optional<Image> recruitmentImage = imageRepository.findByEGStorageByCodeAndNo(RealWorkCode.RECRUITMENT, no);
        //2. 모집글 이미지 dto 세팅
        if(recruitmentImage.isPresent())
            dto.setPicture(new PictureDetails(recruitmentImage.get().getStaticImageName(), recruitmentImage.get().getStorage().getImagePath()));

        //작성자 프로필 이미지(image + storage) -> 쿼리 1번
        //upload 이미지이므로 존재할 수도 있고, 없을수 있음.!
        Optional<Image> userUploadImage = imageRepository.findByEGStorageByCodeAndNo(RealWorkCode.USER, writer.getUserNo());
        //3. 모집글 작성자 dto 세팅
        dto.setAuthor(createWriterDto(writer, userUploadImage));

        //"정기"일 경우 반복주기 검색 -> 쿼리 1번
        if(findRecruitment.getVolunteeringType().equals(VolunteeringType.REG)){
            List<RepeatPeriod> repeatPeriodDto = repeatPeriodRepository.findByRecruitment_RecruitmentNo(no);
            //4. 반복주기 dto 세팅
            dto.setRepeatPeriod(createRepeatPeriodDto(repeatPeriodDto));
        }

        //참여자 매핑 정보 + 참여자 정보 -> 쿼리 1번
        List<Participant> participants = participantRepository.findEGParticipantByRecruitment_RecruitmentNo(no);
        //5. 참여자 dto 세팅
        //upload 이미지가 아닐 경우에도 참여자 수만큼 N번 쿼리 발생(움..)
        //최적화가 필요할거 같은 부분!
        dto.setCurrentVolunteer(createParticipantsDto(participants));

        return dto;
    }

    private WriterDetails createWriterDto(User writer, Optional<Image> userUploadImage) {
        WriterDetails author  = null;
        if(userUploadImage.isPresent()){ //유저 프로필이 업로드 이미지인 경우
            author = new WriterDetails(writer.getNickName(), userUploadImage.get().getStorage().getImagePath());
        }else{                           //oauth 기본 프로필인 경우
            author = new WriterDetails(writer.getNickName(), writer.getPicture());
        }
       return author;
    }

    private RepeatPeriodDetails createRepeatPeriodDto(List<RepeatPeriod> repeatPeriods){
        String period = repeatPeriods.get(0).getPeriod().getViewName();
        String week = (period.equals(Period.MONTH))?(repeatPeriods.get(0).getWeek().getViewName()):"";
        List<String> days = repeatPeriods.stream().map(r -> r.getDay().getViewName()).collect(Collectors.toList());
        return new RepeatPeriodDetails(period, week, days);
    }

    private List<ParticipantDetails> createParticipantsDto(List<Participant> participants) {

        List<ParticipantDetails> dtos = new ArrayList<>();
        participants.stream()
                .forEach(p -> {
                    User participant = p.getParticipant();
                    Boolean isApproved = (p.getState().equals(State.JOIN_APPROVAL))?true:false;
                    Optional<Image> userUploadImage = imageRepository.findByEGStorageByCodeAndNo(RealWorkCode.USER, participant.getUserNo());
                    if(userUploadImage.isPresent()){  //유저 프로필이 업로드 이미지인 경우
                        dtos.add(new ParticipantDetails(participant.getNickName(), userUploadImage.get().getStorage().getImagePath(), isApproved));
                    }else {                           //유저 프로필이 기본(oauth image)인 경우
                        dtos.add(new ParticipantDetails(participant.getNickName(), participant.getPicture(), isApproved));
                    }
                });
        return dtos;
    }
}
