package project.volunteer.domain.recruitment.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.confirmation.application.ConfirmationService;
import project.volunteer.domain.image.application.ImageService;
import project.volunteer.domain.notice.application.NoticeService;
import project.volunteer.domain.participation.application.ParticipationService;
import project.volunteer.domain.recruitment.api.dto.request.RecruitmentRequest;
import project.volunteer.domain.recruitment.application.RecruitmentQueryUseCase;
import project.volunteer.domain.recruitment.application.RecruitmentCommandUseCase;
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
    private final RecruitmentCommandUseCase recruitmentCommandUseCase;
    private final RecruitmentQueryUseCase recruitmentQueryService;
    private final ScheduleCommandUseCase scheduleService;
    private final ImageService imageService;
    private final ParticipationService participationService;
    private final ScheduleParticipationService scheduleParticipationService;
    private final NoticeService noticeService;
    private final ConfirmationService confirmationService;

    @Transactional
    public Long registerRecruitment(Long userId, RecruitmentRequest request){
        User findUser = userService.findUser(userId);
        return recruitmentCommandUseCase.addRecruitment(findUser, request.toCommand());
    }

    @Transactional
    public void deleteRecruitment(Long recruitmentNo){
        recruitmentCommandUseCase.deleteRecruitment(recruitmentNo);

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
    }









    public String findVolunteerPostParticipationState(Long recruitmentNo, Long userNo){
        User findUser = userService.findUser(userNo);

        Recruitment recruitment = recruitmentQueryService.findActivatedRecruitment(recruitmentNo);

        return participationService.findParticipationState(recruitment, findUser);
    }
}
