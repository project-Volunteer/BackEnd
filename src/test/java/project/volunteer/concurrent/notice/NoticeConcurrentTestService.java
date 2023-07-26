package project.volunteer.concurrent.notice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.confirmation.dao.ConfirmationRepository;
import project.volunteer.domain.confirmation.domain.Confirmation;
import project.volunteer.domain.notice.dao.NoticeRepository;
import project.volunteer.domain.notice.domain.Notice;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

//@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class NoticeConcurrentTestService {

    private final UserRepository userRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final NoticeRepository noticeRepository;
    private final ConfirmationRepository confirmationRepository;

    @Transactional
    public void readNoticeBasic(Long recruitmentNo, Long noticeNo, Long userNo) {
        //봉사 모집글 검증
        validateAndGetRecruitment(recruitmentNo);

        //봉사 공지사항 검증
        Notice findNotice = validateAndGetNotice(noticeNo);

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

    @Transactional
    public void readNoticeOPTIMISTIC_LOCK(Long recruitmentNo, Long noticeNo, Long userNo){
        //봉사 모집글 검증
        validateAndGetRecruitment(recruitmentNo);

        //봉사 공지사항 검증
        Notice findNotice = validateAndGetNotice_OPTIMSTIC_LOCK(noticeNo);

        //봉사 공지사항 유무 검증
        if (confirmationRepository.existsCheck(userNo, RealWorkCode.NOTICE, findNotice.getNoticeNo())) {
            throw new BusinessException(ErrorCode.INVALID_CONFIRMATION,
                    String.format("code = [%s], NoticeNo = [%d], userNo = [%d]", RealWorkCode.NOTICE.name(), noticeNo, userNo));
        }

        Confirmation createConfirmation = Confirmation.createConfirmation(RealWorkCode.NOTICE, findNotice.getNoticeNo());
        User loginUser = validateAndGetUser(userNo);
        createConfirmation.setUser(loginUser);

        confirmationRepository.save(createConfirmation);
        findNotice.increaseCheckNum();
    }

    @Transactional
    public void readNotice_UNIQUE_KEY(Long recruitmentNo, Long noticeNo, Long userNo){
        //봉사 모집글 검증
        validateAndGetRecruitment(recruitmentNo);

        //봉사 공지사항 검증
        Notice findNotice = validateAndGetNotice(noticeNo);

        //봉사 공지사항 유무 검증
        if (confirmationRepository.existsCheck(userNo, RealWorkCode.NOTICE, findNotice.getNoticeNo())) {
            throw new BusinessException(ErrorCode.INVALID_CONFIRMATION,
                    String.format("code = [%s], NoticeNo = [%d], userNo = [%d]", RealWorkCode.NOTICE.name(), noticeNo, userNo));
        }

        Confirmation createConfirmation = Confirmation.createConfirmation(RealWorkCode.NOTICE, findNotice.getNoticeNo());
        User loginUser = validateAndGetUser(userNo);
        createConfirmation.setUser(loginUser);

        confirmationRepository.save(createConfirmation);
        findNotice.increaseCheckNum();
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
    private User validateAndGetUser(Long userNo){
        return userRepository.findByUserNo(userNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_USER, String.format("UserNo = [%d]", userNo)));
    }

    private Notice validateAndGetNotice(Long noticeNo){
        return noticeRepository.findValidNotice(noticeNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_NOTICE, String.format("NoticeNo = [%d]", noticeNo)));
    }
    private Notice validateAndGetNotice_OPTIMSTIC_LOCK(Long noticeNo){
        return noticeRepository.findValidNoticeWithOPTIMSTICLOCK(noticeNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_NOTICE, String.format("NoticeNo = [%d]", noticeNo)));
    }
}
