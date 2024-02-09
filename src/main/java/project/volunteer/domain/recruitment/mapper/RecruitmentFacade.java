package project.volunteer.domain.recruitment.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.confirmation.application.ConfirmationService;
import project.volunteer.domain.image.application.ImageService;
import project.volunteer.domain.notice.application.NoticeService;
import project.volunteer.domain.participation.application.ParticipationService;
import project.volunteer.domain.participation.application.dto.AllParticipantDetails;
import project.volunteer.domain.recruitment.api.dto.request.RecruitmentRequest;
import project.volunteer.domain.recruitment.api.dto.response.RecruitmentDetailsResponse;
import project.volunteer.domain.recruitment.application.RecruitmentQueryUseCase;
import project.volunteer.domain.recruitment.application.RecruitmentCommandUseCase;
import project.volunteer.domain.recruitment.application.RepeatPeriodService;
import project.volunteer.domain.recruitment.application.dto.RecruitmentDetails;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.scheduleParticipation.service.ScheduleParticipationService;
import project.volunteer.domain.sehedule.application.ScheduleCommandUseCase;
import project.volunteer.domain.user.application.UserService;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.RealWorkCode;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitmentFacade {
    private final UserService userService;
    private final RecruitmentCommandUseCase recruitmentService;
    private final RecruitmentQueryUseCase recruitmentDtoService;
    private final RepeatPeriodService repeatPeriodService;
    private final ScheduleCommandUseCase scheduleService;
    private final ImageService imageService;
    private final ParticipationService participationService;
    private final ScheduleParticipationService scheduleParticipationService;
    private final NoticeService noticeService;
    private final ConfirmationService confirmationService;

    @Transactional
    public Long registerRecruitment(Long userId, RecruitmentRequest request){
        User findUser = userService.findUser(userId);
        return recruitmentService.addRecruitment(findUser, request.toCommand());
    }



    @Transactional
    public void deleteVolunteerPost(Long recruitmentNo){
        //일정 삭제
        repeatPeriodService.deleteRepeatPeriod(recruitmentNo);

        //이미지 삭제
        imageService.deleteImage(RealWorkCode.RECRUITMENT, recruitmentNo);

        //공지사항 삭제
        List<Long> noticeNoList = noticeService.deleteAllNotice(recruitmentNo);

        //공지사항 확인 리스트 삭제
        confirmationService.deleteAllConfirmation(RealWorkCode.NOTICE, noticeNoList);

        //일정 참여자 삭제
        scheduleParticipationService.deleteAllScheduleParticipation(recruitmentNo);

        //일정 삭제
        scheduleService.deleteAllSchedule(recruitmentNo);

        //봉사 참여자 삭제
        participationService.deleteParticipations(recruitmentNo);

        //봉사 모집글 삭제
        recruitmentService.deleteRecruitment(recruitmentNo);
    }

    public RecruitmentDetailsResponse findVolunteerPostDetails(Long recruitmentNo){
        //봉사 모집글 관련 정보 DTO 세팅(봉사 모집글 정보 + 이미지, 작성자 정보 + 이미지, 정기 일 경우 반복주기)
        RecruitmentDetails recruitmentAndWriterDto = recruitmentDtoService.findRecruitmentAndWriterDto(recruitmentNo);

        //참여자(승인,신청) 리스트 DTO
        AllParticipantDetails allParticipantDto = participationService.findAllParticipantDto(recruitmentNo);

        return new RecruitmentDetailsResponse(
                recruitmentAndWriterDto, allParticipantDto.getApprovalVolunteer(), allParticipantDto.getRequiredVolunteer());
    }

    public String findVolunteerPostParticipationState(Long recruitmentNo, Long userNo){
        User findUser = userService.findUser(userNo);

        Recruitment recruitment = recruitmentService.findPublishedRecruitment(recruitmentNo);

        return participationService.findParticipationState(recruitment, findUser);
    }
}
