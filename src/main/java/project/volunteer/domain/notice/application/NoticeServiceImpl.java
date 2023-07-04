package project.volunteer.domain.notice.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.notice.api.dto.NoticeAdd;
import project.volunteer.domain.notice.api.dto.NoticeEdit;
import project.volunteer.domain.notice.dao.NoticeRepository;
import project.volunteer.domain.notice.domain.Notice;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService{

    private final RecruitmentRepository recruitmentRepository;
    private final NoticeRepository noticeRepository;

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
}
