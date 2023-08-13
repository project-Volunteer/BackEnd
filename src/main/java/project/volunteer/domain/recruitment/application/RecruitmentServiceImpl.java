package project.volunteer.domain.recruitment.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.application.dto.RecruitmentParam;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecruitmentServiceImpl implements RecruitmentService{
    private final RecruitmentRepository recruitmentRepository;

    @Transactional
    public Recruitment addRecruitment(User writer, RecruitmentParam saveDto){
        Recruitment recruitment = Recruitment.createRecruitment(saveDto.getTitle(), saveDto.getContent(), saveDto.getVolunteeringCategory(), saveDto.getVolunteeringType(),
                saveDto.getVolunteerType(), saveDto.getVolunteerNum(), saveDto.getIsIssued(), saveDto.getOrganizationName(), saveDto.getAddress(),
                saveDto.getCoordinate(), saveDto.getTimetable(), saveDto.getIsPublished());

        recruitment.setWriter(writer);
        return recruitmentRepository.save(recruitment);
    }

    @Override
    @Transactional
    public void deleteRecruitment(Long deleteNo) {
        Recruitment findRecruitment =
                recruitmentRepository.findValidRecruitment(deleteNo).orElseThrow(
                        () -> new BusinessException(ErrorCode.NOT_EXIST_RECRUITMENT, String.format("Delete Recruitment ID = [%d]", deleteNo)));

        findRecruitment.setDeleted();
    }

    @Override
    public Recruitment findPublishedRecruitment(Long recruitmentNo) {
        return validateAndGetPublishedRecruitment(recruitmentNo);
    }

    @Override
    public Recruitment findActivatedRecruitment(Long recruitmentNo) {
        Recruitment findRecruitment = validateAndGetPublishedRecruitment(recruitmentNo);

        //봉사 모집글 마감 일자 조회
        if(!findRecruitment.isAvailableDate()){
            throw new BusinessException(ErrorCode.EXPIRED_PERIOD_RECRUITMENT, String.format("RecruitmentNo = [%d]", recruitmentNo));
        }
        return findRecruitment;
    }

    private Recruitment validateAndGetPublishedRecruitment(Long recruitmentNo){
        return recruitmentRepository.findPublishedByRecruitmentNo(recruitmentNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_RECRUITMENT, String.format("RecruitmentNo = [%d]", recruitmentNo)));
    }

}
