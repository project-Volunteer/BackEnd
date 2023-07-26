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

    @Override
    @Transactional
    public Notice addNotice(Long recruitmentNo, NoticeAdd dto) {
        //모집글 검증
        Recruitment findRecruitment = validateAndGetRecruitment(recruitmentNo);

        Notice createNotice = dto.toEntity();
        createNotice.setRecruitment(findRecruitment);

        return noticeRepository.save(createNotice);
    }

    @Override
    @Transactional
    public void editNotice(Long recruitmentNo, Long noticeNo, NoticeEdit dto) {
        //모집글 검증
        validateAndGetRecruitment(recruitmentNo);

        Notice findNotice = noticeRepository.findValidNotice(noticeNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_NOTICE, String.format("NoticeNo = [%d]", noticeNo)));
        findNotice.updateNotice(dto.getContent());
    }

    @Override
    @Transactional
    public void deleteNotice(Long recruitmentNo, Long noticeNo) {
        //모집글 검증
        validateAndGetRecruitment(recruitmentNo);

        Notice findNotice = noticeRepository.findValidNotice(noticeNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_NOTICE, String.format("NoticeNo = [%d]", noticeNo)));
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

    private Recruitment validateAndGetRecruitment(Long recruitmentNo){
        //모집글 검증(출판 and 삭제 x)
        Recruitment findRecruitment = recruitmentRepository.findPublishedByRecruitmentNo(recruitmentNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_RECRUITMENT, String.format("Recruitment No = [%d]", recruitmentNo)));

        //모집글 공지사항 작성 가능 일자 검증
        if(findRecruitment.isDoneDate()){
            throw new BusinessException(ErrorCode.EXPIRED_PERIOD_NOTICE,
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
