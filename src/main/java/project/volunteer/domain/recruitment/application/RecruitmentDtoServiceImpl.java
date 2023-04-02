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
import project.volunteer.domain.recruitment.application.dto.ParticipantDto;
import project.volunteer.domain.recruitment.application.dto.WriterDto;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.dto.PictureDto;
import project.volunteer.domain.recruitment.application.dto.RecruitmentDto;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.dto.RepeatPeriodDto;
import project.volunteer.domain.repeatPeriod.dao.RepeatPeriodRepository;
import project.volunteer.domain.repeatPeriod.domain.Day;
import project.volunteer.domain.repeatPeriod.domain.Period;
import project.volunteer.domain.repeatPeriod.domain.RepeatPeriod;
import project.volunteer.domain.repeatPeriod.domain.Week;
import project.volunteer.domain.user.domain.User;

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
    public RecruitmentDto findRecruitment(Long no) {

        //모집글 정보 + 모집글 작성자 정보 -> 쿼리1번
        Recruitment findRecruitment = recruitmentRepository.findEGWriterByRecruitmentNo(no).orElseThrow(()
                -> new NullPointerException(String.format("Not found recruitmentNo=[%d]", no)));
        User writer = findRecruitment.getWriter();
        //1. 모집글 정보 dto 생성
        RecruitmentDto dto = new RecruitmentDto(findRecruitment);

        //모집글 이미지(image + storage) -> 쿼리 1번
        //모집글 이미지는 반드시 존재!
        Optional<Image> recruitmentImage = imageRepository.findByRealWorkCodeAndNo(RealWorkCode.RECRUITMENT, no);
        //2. 모집글 이미지 dto 세팅
        if(recruitmentImage.isPresent())
            dto.setPicture(new PictureDto(recruitmentImage.get().getStaticImageName(), recruitmentImage.get().getStorage().getImagePath()));

        //작성자 프로필 이미지(image + storage) -> 쿼리 1번
        //upload 이미지이므로 존재할 수도 있고, 없을수 있음.!
        Optional<Image> userUploadImage = imageRepository.findByRealWorkCodeAndNo(RealWorkCode.USER, writer.getUserNo());
        //3. 모집글 작성자 dto 세팅
        dto.setAuthor(createWriterDto(writer, userUploadImage));

        //"장기"일 경우 반복주기 검색 -> 쿼리 1번
        if(findRecruitment.getVolunteeringType().equals(VolunteeringType.LONG)){
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

    private WriterDto createWriterDto(User writer, Optional<Image> userUploadImage) {
        WriterDto author  = null;
        if(userUploadImage.isPresent()){ //유저 프로필이 업로드 이미지인 경우
            author = new WriterDto(writer.getNickName(), userUploadImage.get().getStorage().getImagePath());
        }else{                           //oauth 기본 프로필인 경우
            author = new WriterDto(writer.getNickName(), writer.getPicture());
        }
       return author;
    }

    private RepeatPeriodDto createRepeatPeriodDto(List<RepeatPeriod> repeatPeriods){
        Period period = repeatPeriods.get(0).getPeriod();
        Week week = (period.equals(Period.MONTH))?(repeatPeriods.get(0).getWeek()):null;
        List<Day> days = repeatPeriods.stream().map(r -> r.getDay()).collect(Collectors.toList());
        return new RepeatPeriodDto(period, week, days);
    }

    private List<ParticipantDto> createParticipantsDto(List<Participant> participants) {

        List<ParticipantDto> dtos = new ArrayList<>();
        participants.stream()
                .forEach(p -> {
                    User participant = p.getParticipant();
                    Optional<Image> userUploadImage = imageRepository.findByRealWorkCodeAndNo(RealWorkCode.USER, participant.getUserNo());
                    if(userUploadImage.isPresent()){  //유저 프로필이 업로드 이미지인 경우
                        dtos.add(new ParticipantDto(participant.getNickName(), userUploadImage.get().getStorage().getImagePath(), p.getIsApproved()));
                    }else {                           //유저 프로필이 기본(oauth image)인 경우
                        dtos.add(new ParticipantDto(participant.getNickName(), participant.getPicture(), p.getIsApproved()));
                    }
                });
        return dtos;
    }
}
