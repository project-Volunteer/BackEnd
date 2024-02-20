package project.volunteer.domain.recruitment.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.confirmation.application.ConfirmationService;
import project.volunteer.domain.image.application.ImageService;
import project.volunteer.domain.notice.application.NoticeService;
import project.volunteer.domain.recruitmentParticipation.application.RecruitmentParticipationUseCase;
import project.volunteer.domain.recruitment.api.dto.request.RecruitmentRequest;
import project.volunteer.domain.scheduleParticipation.service.ScheduleParticipationCommandUseCase;
import project.volunteer.domain.sehedule.application.ScheduleCommandUseCase;
import project.volunteer.domain.user.application.UserService;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.RealWorkCode;

import java.util.List;
import project.volunteer.global.common.dto.StateResult;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitmentFacade {
    private final UserService userService;
    private final RecruitmentCommandUseCase recruitmentCommandUseCase;
    private final RecruitmentQueryUseCase recruitmentQueryUseCase;
    private final ScheduleCommandUseCase scheduleCommandUseCase;
    private final RecruitmentParticipationUseCase recruitmentParticipationUseCase;



    private final ImageService imageService;
    private final ScheduleParticipationCommandUseCase scheduleParticipationService;
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

        scheduleCommandUseCase.deleteAllSchedule(recruitmentNo);

        recruitmentParticipationUseCase.deleteRecruitmentParticipations(recruitmentNo);





        //이미지 삭제
        imageService.deleteImage(RealWorkCode.RECRUITMENT, recruitmentNo);

        //공지사항 삭제
        List<Long> noticeNoList = noticeService.deleteAllNotice(recruitmentNo);

        //공지사항 확인 리스트 삭제
        confirmationService.deleteAllConfirmation(RealWorkCode.NOTICE, noticeNoList);

        //일정 참여자 삭제
        scheduleParticipationService.deleteAllScheduleParticipation(recruitmentNo);
    }

    public StateResult findState(Long recruitmentNo, Long userNo){
        User user = userService.findUser(userNo);
        return recruitmentQueryUseCase.searchState(user.getUserNo(), recruitmentNo);
    }

}
