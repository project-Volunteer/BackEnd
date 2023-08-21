package project.volunteer.domain.notice.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.confirmation.dao.ConfirmationRepository;
import project.volunteer.domain.notice.application.dto.NoticeDetails;
import project.volunteer.domain.notice.dao.NoticeRepository;
import project.volunteer.domain.notice.domain.Notice;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NoticeDtoServiceImpl implements NoticeDtoService{
    private final NoticeRepository noticeRepository;
    private final ConfirmationRepository checkRepository;

    @Override
    public List<NoticeDetails> findNoticeDtos(Long recruitmentNo, Long userNo) {

        return noticeRepository.findByRecruitment_RecruitmentNo(recruitmentNo).stream()
                .map(n -> {
                    //공지사항 읽음 확인을 위한 별도 쿼리 사용
                    Boolean isChecked = checkRepository.existsCheck(userNo, RealWorkCode.NOTICE, n.getNoticeNo());
                    return NoticeDetails.toDto(n, isChecked);
                })
                .collect(Collectors.toList());
    }

    @Override
    public NoticeDetails findNoticeDto(Long noticeNo, Long userNo) {
        Notice findNotice = validateAndGetNotice(noticeNo);
        Boolean isChecked = checkRepository.existsCheck(userNo, RealWorkCode.NOTICE, findNotice.getNoticeNo());

        return NoticeDetails.toDto(findNotice, isChecked);
    }

    private Notice validateAndGetNotice(Long noticeNo){
        return noticeRepository.findPublishedNotice(noticeNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_NOTICE, String.format("NoticeNo = [%d]", noticeNo)));
    }
}
