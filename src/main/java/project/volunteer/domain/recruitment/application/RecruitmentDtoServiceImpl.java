package project.volunteer.domain.recruitment.application;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.image.domain.RealWorkCode;
import project.volunteer.domain.participation.dao.ParticipantRepository;
import project.volunteer.domain.participation.dao.dto.ParticipantStateDetails;
import project.volunteer.domain.participation.domain.Participant;
import project.volunteer.domain.recruitment.application.dto.ParticipantDetails;
import project.volunteer.domain.recruitment.application.dto.ParticipantState;
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
import project.volunteer.global.util.SecurityUtil;

import java.time.LocalDate;
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

        //모집글 정보 + 모집글 작성자 정보 -> 쿼리 1번
        Recruitment findRecruitment = recruitmentRepository.findWriterEG(no).orElseThrow(()
                -> new BusinessException(ErrorCode.NOT_EXIST_RECRUITMENT, String.format("Search Recruitment NO = [%d]", no)));
        User writer = findRecruitment.getWriter();

        //1. 모집글 정보 dto 세팅
        RecruitmentDetails dto = new RecruitmentDetails(findRecruitment);

        //2. 모집글 이미지 dto 세팅 -> 쿼리 1번
        makeRecruitmentImageDto(dto, no);

        //3. 모집글 작성자 dto 세팅 -> 쿼리 1번
        makeWriterDto(dto, writer);

        //4. 정기 모집글 반복주기 dto 세팅
        //"정기"일 경우 반복주기 검색 -> 쿼리 1번
        if(findRecruitment.getVolunteeringType().equals(VolunteeringType.REG)){
            makeRepeatPeriodDto(dto, no);
        }

        //5. 모집글 참여자 dto 세팅 -> 참여자 정보(쿼리1번) + 참여자 이미지 검색(참여자 수 N)
        //makeParticipantsDto(dto, no);
        makeOptimizedParticipantsDto(dto, no);

        //6. 로그인 사용자 상태 판별 -> 쿼리 2번
        dto.setStatus(decideUserState(findRecruitment));

        return dto;
    }

    private void makeRecruitmentImageDto(RecruitmentDetails dto, Long recruitmentNo){
        //모집글 이미지(image + storage) -> 쿼리 1번
        //모집글 이미지는 반드시 존재!
        Optional<Image> recruitmentImage = imageRepository.findEGStorageByCodeAndNo(RealWorkCode.RECRUITMENT, recruitmentNo);
        if(recruitmentImage.isPresent())
            dto.setPicture(new PictureDetails(recruitmentImage.get().getStaticImageName(), recruitmentImage.get().getStorage().getImagePath()));
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

    private void makeRepeatPeriodDto(RecruitmentDetails dto, Long recruitmentNo){
        List<RepeatPeriod> repeatPeriods = repeatPeriodRepository.findByRecruitment_RecruitmentNo(recruitmentNo);

        String period = repeatPeriods.get(0).getPeriod().getViewName();
        String week = (period.equals(Period.MONTH))?(repeatPeriods.get(0).getWeek().getViewName()):"";
        List<String> days = repeatPeriods.stream().map(r -> r.getDay().getViewName()).collect(Collectors.toList());

        dto.setRepeatPeriod(new RepeatPeriodDetails(period, week, days));
    }

    private void makeParticipantsDto(RecruitmentDetails dto, Long recruitmentNo) {
        //참여자 정보 + (approval, request) 상태 조회 -> 쿼리 1번
        List<Participant> participants = participantRepository.findEGParticipantByRecruitment_RecruitmentNoAndStateIn(
                recruitmentNo, List.of(State.JOIN_REQUEST, State.JOIN_APPROVAL));

        List<ParticipantDetails> approvedList = new ArrayList<>();
        List<ParticipantDetails> requiredList = new ArrayList<>();

        participants.stream()
                .forEach(p -> {
                    User participant = p.getParticipant(); //EntityGraph 인해 쿼리 발생 X
                    ParticipantDetails details = null;

                    //업로드 한 이미지 인지 판단 - 참여자(승인,요청) 인원수 만큼 쿼리 발생
                    //upload 이미지가 아닐 경우에도 참여자 N명 만큼 쿼리 발생
                    //최적화가 필요할거 같은 부분!
                    Optional<Image> userUploadImage = imageRepository.findEGStorageByCodeAndNo(RealWorkCode.USER, participant.getUserNo());
                    if(userUploadImage.isPresent()){
                        details = new ParticipantDetails(
                                participant.getUserNo(), participant.getNickName(), userUploadImage.get().getStorage().getImagePath());
                    }else {
                        details = new ParticipantDetails(participant.getUserNo(), participant.getNickName(), participant.getPicture());
                    }

                    if(p.getState().equals(State.JOIN_APPROVAL)){
                        approvedList.add(details);
                    }else{
                        requiredList.add(details);
                    }
                });

        dto.setApprovalVolunteer(approvedList);
        dto.setRequiredVolunteer(requiredList);
    }

    private void makeOptimizedParticipantsDto(RecruitmentDetails dto, Long recruitmentNo){

        List<ParticipantDetails> approvedList = new ArrayList<>();
        List<ParticipantDetails> requiredList = new ArrayList<>();

        //최적화한 쿼리(쿼리 1번)
        List<ParticipantStateDetails> participants = participantRepository.findParticipantsByOptimization(recruitmentNo,
                List.of(State.JOIN_REQUEST, State.JOIN_APPROVAL));

        participants.stream()
                .forEach(p -> {
                    if(p.getState().equals(State.JOIN_REQUEST)){
                        requiredList.add(new ParticipantDetails(p.getUserNo(), p.getNickName(), p.getImageUrl()));
                    }else{
                        approvedList.add(new ParticipantDetails(p.getUserNo(), p.getNickName(), p.getImageUrl()));
                    }
                });

        dto.setApprovalVolunteer(approvedList);
        dto.setRequiredVolunteer(requiredList);
    }

    private String decideUserState(Recruitment findRecruitment){
        //모집 마감 상태
        if(findRecruitment.getVolunteeringTimeTable().getEndDay().isBefore(LocalDate.now()) ||
                participantRepository.countAvailableParticipants(findRecruitment.getRecruitmentNo())==findRecruitment.getVolunteerNum()) {
            return ParticipantState.DONE.name();
        }

        Long loginUserNo = SecurityUtil.getLoginUserNo();
        Optional<Participant> findState = participantRepository.findByRecruitment_RecruitmentNoAndParticipant_UserNo(
                findRecruitment.getRecruitmentNo(), loginUserNo);

        //신청 가능 상태(재신청 포함)
        if(!findState.isPresent() || List.of(State.JOIN_CANCEL, State.QUIT, State.DEPORT).contains(findState.get().getState())){
            return ParticipantState.AVAILABLE.name();
        }

        //승인 대기 상태
        if(findState.get().getState().equals(State.JOIN_REQUEST)){
            return ParticipantState.PENDING.name();
        }

        //승인 완료 상태
        if(findState.get().getState().equals(State.JOIN_APPROVAL)){
            return ParticipantState.APPROVED.name();
        }

        return "";
    }

}
