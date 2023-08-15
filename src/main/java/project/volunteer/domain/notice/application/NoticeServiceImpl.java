package project.volunteer.domain.notice.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.notice.api.dto.request.NoticeAdd;
import project.volunteer.domain.notice.api.dto.request.NoticeEdit;
import project.volunteer.domain.notice.dao.NoticeRepository;
import project.volunteer.domain.notice.domain.Notice;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService{
    private final NoticeRepository noticeRepository;

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
    public void increaseCheckNumWithOPTIMSTIC_LOCK(Long noticeNo) {
        //봉사 공지사항 검증
//        Notice findNotice = validateAndGetNotice(noticeNo);
        Notice findNotice = validateAndGetNoticeWithOPTIMSTIC_LOCK(noticeNo); //낙관적 락 사용

        findNotice.increaseCheckNum();
    }

    @Override
    @Transactional
    public void decreaseCheckNumWithOPTIMSTIC_LOCK(Long noticeNo) {
        //봉사 공지사항 검증
        Notice findNotice = validateAndGetNoticeWithOPTIMSTIC_LOCK(noticeNo); //낙관적 락 사용

        findNotice.decreaseCheckNum();
    }

    @Override
    @Transactional
    public void increaseCommnetNum(Long noticeNo) {
        //봉사 공지사항 검증
        Notice findNotice = validateAndGetNotice(noticeNo);

        findNotice.increaseCommentNum();
    }

    @Transactional
    @Override
    public void decreaseCommentNum(Long noticeNo) {
        //봉사 공지사항 검증
        Notice findNotice = validateAndGetNotice(noticeNo);

        findNotice.decreaseCommentNum();
    }

    private Notice validateAndGetNotice(Long noticeNo){
        return noticeRepository.findPublishedNotice(noticeNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_NOTICE, String.format("NoticeNo = [%d]", noticeNo)));
    }
    private Notice validateAndGetNoticeWithOPTIMSTIC_LOCK(Long noticeNo){
        return noticeRepository.findValidNoticeWithOPTIMSTICLOCK(noticeNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_NOTICE, String.format("NoticeNo = [%d]", noticeNo)));
    }
}
