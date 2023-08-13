package project.volunteer.domain.recruitment.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.image.application.ImageService;
import project.volunteer.domain.image.application.dto.ImageParam;
import project.volunteer.domain.participation.application.ParticipationService;
import project.volunteer.domain.participation.application.dto.AllParticipantDetails;
import project.volunteer.domain.recruitment.api.dto.request.RecruitmentRequest;
import project.volunteer.domain.recruitment.api.dto.response.RecruitmentDetailsResponse;
import project.volunteer.domain.recruitment.application.RecruitmentDtoService;
import project.volunteer.domain.recruitment.application.RecruitmentService;
import project.volunteer.domain.recruitment.application.RepeatPeriodService;
import project.volunteer.domain.recruitment.application.dto.RecruitmentDetails;
import project.volunteer.domain.recruitment.application.dto.RecruitmentParam;
import project.volunteer.domain.recruitment.application.dto.RepeatPeriodParam;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.dto.RepeatPeriodDetails;
import project.volunteer.domain.sehedule.application.ScheduleService;
import project.volunteer.domain.sehedule.application.dto.ScheduleParamReg;
import project.volunteer.domain.user.application.UserService;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.RealWorkCode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitmentFacade {
    private final UserService userService;
    private final RecruitmentService recruitmentService;
    private final RecruitmentDtoService recruitmentDtoService;
    private final RepeatPeriodService repeatPeriodService;
    private final ScheduleService scheduleService;
    private final ImageService imageService;
    private final ParticipationService participationService;

    @Transactional
    public Long registerVolunteerPost(Long userId, RecruitmentRequest form){
        User findUser = userService.findUser(userId);

        Recruitment recruitment = recruitmentService.addRecruitment(findUser, RecruitmentParam.ToRecruitmentParam(form));
        //정기일 경우
        if(form.getVolunteeringType().toUpperCase().equals(VolunteeringType.REG.name())){
            RepeatPeriodParam periodParam = new RepeatPeriodParam(form.getPeriod(), form.getWeek(), form.getDays());
            //반복 주기 저장
            repeatPeriodService.addRepeatPeriod(recruitment, periodParam);

            //스케줄 자동 할당
            scheduleService.addRegSchedule(recruitment,
                    new ScheduleParamReg(form.getStartDay(), form.getEndDay(), form.getHourFormat(), form.getStartTime(), form.getProgressTime(),
                            form.getOrganizationName(), form.getAddress().getSido(), form.getAddress().getSigungu(), form.getAddress().getDetails(),
                            form.getContent(), form.getVolunteerNum(), periodParam));
        }

        //업로드 이미지 저장
        if(!form.getPicture().getIsStaticImage()) {
            imageService.addImage(ImageParam.builder()
                    .code(RealWorkCode.RECRUITMENT)
                    .no(recruitment.getRecruitmentNo())
                    .uploadImage(form.getPicture().getUploadImage())
                    .build());
        }

        return recruitment.getRecruitmentNo();
    }

    @Transactional
    public void deleteVolunteerPost(Long recruitmentNo){
        //일정 삭제
        repeatPeriodService.deleteRepeatPeriod(recruitmentNo);

        //이미지 삭제
        imageService.deleteImage(RealWorkCode.RECRUITMENT, recruitmentNo);

        //공지사항 확인 리스트 삭제

        //공지사항 삭제

        //봉사 참여자 삭제

        //일정 삭제

        //일정 참여자 삭제

        //봉사 모집글 삭제
        recruitmentService.deleteRecruitment(recruitmentNo);
    }

    public RecruitmentDetailsResponse findVolunteerPostDetails(Long recruitmentNo){
        //봉사 모집글 & 작성자 DTO
        RecruitmentDetails recruitmentAndWriterDto = recruitmentDtoService.findRecruitmentAndWriterDto(recruitmentNo);

        //정기 일 경우 반복주기 DTO
        RepeatPeriodDetails repeatPeriodDto = null;
        if(recruitmentAndWriterDto.getVolunteeringType().equals(VolunteeringType.REG.getId())){
            repeatPeriodDto = repeatPeriodService.findRepeatPeriodDto(recruitmentNo);
            recruitmentAndWriterDto.setRepeatPeriod(repeatPeriodDto);
        }

        //참여자(승인,신청) 리스트 DTO
        AllParticipantDetails allParticipantDto = participationService.findAllParticipantDto(recruitmentNo);

        return new RecruitmentDetailsResponse(
                recruitmentAndWriterDto, allParticipantDto.getApprovalVolunteer(), allParticipantDto.getRequiredVolunteer());
    }

    public String findVolunteerPostParticipationState(Long recruitmentNo, Long userNo){
        Recruitment recruitment = recruitmentService.findPublishedRecruitment(recruitmentNo);

        return participationService.findParticipationState(recruitment, userNo);
    }
}
