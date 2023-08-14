package project.volunteer.domain.notice.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.notice.api.dto.request.NoticeAdd;
import project.volunteer.domain.notice.api.dto.request.NoticeEdit;
import project.volunteer.domain.notice.application.NoticeService;
import project.volunteer.domain.notice.domain.Notice;
import project.volunteer.domain.recruitment.application.RecruitmentService;
import project.volunteer.domain.recruitment.domain.Recruitment;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeFacade {
    private final RecruitmentService recruitmentService;
    private final NoticeService noticeService;

    @Transactional
    public Notice registerVolunteerPostNotice(Long recruitmentNo, NoticeAdd dto){
        Recruitment recruitment = recruitmentService.findActivatedRecruitment(recruitmentNo);

        return noticeService.addNotice(recruitment, dto);
    }

    @Transactional
    public void editVolunteerPostNotice(Long recruitmentNo, Long noticeNo, NoticeEdit dto){
        recruitmentService.findActivatedRecruitment(recruitmentNo);

        noticeService.editNotice(noticeNo, dto);
    }

    @Transactional
    public void deleteVolunteerPostNotice(Long recruitmentNo, Long noticeNo){
        recruitmentService.findActivatedRecruitment(recruitmentNo);

        noticeService.deleteNotice(noticeNo);
    }
}
