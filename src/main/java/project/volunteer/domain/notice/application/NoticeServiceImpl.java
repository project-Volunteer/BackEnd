package project.volunteer.domain.notice.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.confirmation.dao.ConfirmationRepository;
import project.volunteer.domain.confirmation.domain.Confirmation;
import project.volunteer.domain.notice.api.dto.request.NoticeAdd;
import project.volunteer.domain.notice.api.dto.request.NoticeEdit;
import project.volunteer.domain.notice.dao.NoticeRepository;
import project.volunteer.domain.notice.domain.Notice;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.reply.application.ReplyService;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService{

    private final UserRepository userRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final NoticeRepository noticeRepository;
    private final ConfirmationRepository confirmationRepository;

    private final ReplyService replyService;

    @Override
    @Transactional
    public Notice addNotice(Recruitment recruitment, NoticeAdd dto) {
        Notice createNotice = dto.toEntity();
        createNotice.setRecruitment(recruitment);

        return noticeRepository.save(createNotice);
    }

    @Override
    @Transactional
    public void editNotice(Long noticeNo, NoticeEdit dto) {
        Notice findNotice = validateAndGetNotice(noticeNo);
        findNotice.updateNotice(dto.getContent());
    }

    @Override
    @Transactional
    public void deleteNotice(Long noticeNo) {
        Notice findNotice = validateAndGetNotice(noticeNo);
        findNotice.delete();
    }

    @Override
    @Transactional
    public void readNotice(Long recruitmentNo, Long noticeNo, Long userNo) {
        //봉사 모집글 검증
        validateAndGetRecruitment(recruitmentNo);

        //봉사 공지사항 검증
//        Notice findNotice = validateAndGetNotice(noticeNo);
        Notice findNotice = validateAndGetNoticeWithOPTIMSTIC_LOCK(noticeNo); //낙관적 락 사용

        //봉사 공지사항 유무 검증
        if(confirmationRepository.existsCheck(userNo, RealWorkCode.NOTICE, findNotice.getNoticeNo())){
            throw new BusinessException(ErrorCode.INVALID_CONFIRMATION,
                    String.format("code = [%s], NoticeNo = [%d], userNo = [%d]", RealWorkCode.NOTICE.name(), noticeNo, userNo));
        }

        Confirmation createConfirmation = Confirmation.createConfirmation(RealWorkCode.NOTICE, findNotice.getNoticeNo());
        User loginUser = validateAndGetUser(userNo);
        createConfirmation.setUser(loginUser);

        confirmationRepository.save(createConfirmation);
        findNotice.increaseCheckNum();
    }

    @Override
    @Transactional
    public void readCancelNotice(Long recruitmentNo, Long noticeNo, Long userNo) {
        //봉사 모집글 검증
        validateAndGetRecruitment(recruitmentNo);

        //봉사 공지사항 검증
        Notice findNotice = validateAndGetNotice(noticeNo);

        //공지사항 읽음 삭제
        Confirmation findConfirmation = validateAndGetConfirmation(RealWorkCode.NOTICE, noticeNo, userNo);
        confirmationRepository.delete(findConfirmation);

        findNotice.decreaseCheckNum();
    }


    @Override
    @Transactional
    public void addNoticeComment(Long recruitmentNo, Long noticeNo, Long loginUserNo, String content) {
        //봉사 모집글 검증(존재여부 + 작성 가능 기간)
        validateAndGetRecruitment(recruitmentNo);
        //봉사 공지사항 검증
        Notice findNotice = validateAndGetNotice(noticeNo);

        //TODO: 현재는 replyService 의존, Notice 필드 업데이트가 필요하므로 별도 service 로직이 필요.
        //TODO: 현재 service에 많은 repository 의존 관계 존재(스파게티 코드, 테스트 코드 작성 어렵, 응집도 높음)
        //TODO: Service에 최소 repository 의존관계만 형성해 응집도를 낮추고 테스트에 용의하게 하고 재사용성을 높힌다.
        //TODO: Controller에서 분리된 Service를 각 호출하면 트랜잭션 원자성을 해침.
        //TODO: 퍼사드(facade) 디자인 패턴을 사용해서 최소한으로 분리시킨 service의 상의 계층을 만들어 사용하도록 리팩토링할 예정.
        replyService.addComment(loginUserNo, RealWorkCode.NOTICE, noticeNo, content);

        findNotice.increaseCommentNum();
    }
    @Transactional
    @Override
    public void addNoticeCommentReply(Long recruitmentNo, Long noticeNo, Long loginUserNo, Long parentNo, String content) {
        //봉사 모집글 검증(존재여부 + 작성 가능 기간)
        validateAndGetRecruitment(recruitmentNo);
        //봉사 공지사항 검증
        Notice findNotice = validateAndGetNotice(noticeNo);

        //TODO: 디자인 패턴 리팩토링 필요
        replyService.addCommentReply(loginUserNo, RealWorkCode.NOTICE, noticeNo, parentNo, content);
        findNotice.increaseCommentNum();
    }
    @Transactional
    @Override
    public void editNoticeReply(Long recruitmentNo, Long noticeNo, Long loginUserNo, Long replyNo, String content) {
        //봉사 모집글 검증(존재여부 + 작성 가능 기간)
        validateAndGetRecruitment(recruitmentNo);
        //봉사 공지사항 검증
        validateAndGetNotice(noticeNo);

        //TODO: 디자인 패턴 리팩토링 필요
        replyService.editReply(loginUserNo, replyNo, content);
    }
    @Transactional
    @Override
    public void deleteNoticeReply(Long recruitmentNo, Long noticeNo, Long replyNo) {
        //봉사 모집글 검증(존재여부 + 작성 가능 기간)
        validateAndGetRecruitment(recruitmentNo);
        //봉사 공지사항 검증
        Notice findNotice = validateAndGetNotice(noticeNo);

        //TODO: 디자인 패턴 리팩토링 필요
        replyService.deleteReply(replyNo);
        findNotice.decreaseCommentNum();
    }



    private Recruitment validateAndGetRecruitment(Long recruitmentNo){
        //모집글 검증(출판 and 삭제 x)
        Recruitment findRecruitment = recruitmentRepository.findPublishedByRecruitmentNo(recruitmentNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_RECRUITMENT, String.format("Recruitment No = [%d]", recruitmentNo)));

        //모집글 공지사항 작성 가능 일자 검증
        if(findRecruitment.isDoneDate()){
            throw new BusinessException(ErrorCode.EXPIRED_PERIOD_ACTIVITY_RECRUITMENT,
                    String.format("RecruitmentNo = [%d], Recruitment EndDay = [%s]", recruitmentNo, findRecruitment.getVolunteeringTimeTable().getEndDay().toString()));
        }
        return findRecruitment;
    }
    private Notice validateAndGetNotice(Long noticeNo){
        return noticeRepository.findValidNotice(noticeNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_NOTICE, String.format("NoticeNo = [%d]", noticeNo)));
    }
    private Notice validateAndGetNoticeWithOPTIMSTIC_LOCK(Long noticeNo){
        return noticeRepository.findValidNoticeWithOPTIMSTICLOCK(noticeNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_NOTICE, String.format("NoticeNo = [%d]", noticeNo)));
    }
    private User validateAndGetUser(Long userNo){
        return userRepository.findByUserNo(userNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_USER, String.format("UserNo = [%d]", userNo)));
    }
    private Confirmation validateAndGetConfirmation(RealWorkCode code, Long no, Long userNo){
        return confirmationRepository.findConfirmation(userNo, code, no)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_CONFIRMATION,
                        String.format("code = [%s], no = [%d], userNo = [%d]", code.name(), no, userNo)));
    }
}
