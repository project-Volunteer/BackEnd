package project.volunteer.domain.notice.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.confirmation.dao.ConfirmationRepository;
import project.volunteer.domain.notice.application.dto.NoticeDetails;
import project.volunteer.domain.notice.dao.NoticeRepository;
import project.volunteer.domain.notice.domain.Notice;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NoticeDtoServiceImpl implements NoticeDtoService{

    private final RecruitmentRepository recruitmentRepository;
    private final NoticeRepository noticeRepository;
    private final ConfirmationRepository checkRepository;

    @Override
    public List<NoticeDetails> findNoticeDtos(Long recruitmentNo, Long userNo) {

        Recruitment findRecruitment = validateAndGetRecruitment(recruitmentNo);

        return noticeRepository.findByRecruitment_RecruitmentNo(findRecruitment.getRecruitmentNo()).stream()
                .map(n -> {
                    //공지사항 읽음 확인을 위한 별도 쿼리 사용
                    Boolean isChecked = checkRepository.existsCheck(userNo, RealWorkCode.NOTICE, n.getNoticeNo());
                    return NoticeDetails.toDto(n, isChecked);
                })
                .collect(Collectors.toList());
    }

    @Override
    public NoticeDetails findNoticeDto(Long recruitmentNo, Long noticeNo, Long userNo) {

        validateAndGetRecruitment(recruitmentNo);

        Notice findNotice = validateAndGetNotice(noticeNo);
        Boolean isChecked = checkRepository.existsCheck(userNo, RealWorkCode.NOTICE, findNotice.getNoticeNo());

        //댓글 조회 및 DTO 추가 필요

        return NoticeDetails.toDto(findNotice, isChecked);
    }

    private Recruitment validateAndGetRecruitment(Long recruitmentNo){
        //모집글 검증(출판 o and 삭제 x)
        return recruitmentRepository.findPublishedByRecruitmentNo(recruitmentNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_RECRUITMENT, String.format("Recruitment No = [%d]", recruitmentNo)));
    }
    private Notice validateAndGetNotice(Long noticeNo){
        //삭제x, 존재 o
        return noticeRepository.findValidNotice(noticeNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_NOTICE, String.format("NoticeNo = [%d]", noticeNo)));
    }
}
