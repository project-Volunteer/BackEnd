package project.volunteer.domain.recruitment.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.application.dto.RecruitmentParam;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.dao.RepeatPeriodRepository;
import project.volunteer.domain.recruitment.domain.RepeatPeriod;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecruitmentServiceImpl implements RecruitmentService{

    private final UserRepository userRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final RepeatPeriodRepository repeatPeriodRepository;

    @Transactional
    public Recruitment addRecruitment(Long loginUserNo, RecruitmentParam saveDto){

        Recruitment recruitment = Recruitment.createRecruitment(saveDto.getTitle(), saveDto.getContent(), saveDto.getVolunteeringCategory(), saveDto.getVolunteeringType(),
                saveDto.getVolunteerType(), saveDto.getVolunteerNum(), saveDto.getIsIssued(), saveDto.getOrganizationName(), saveDto.getAddress(),
                saveDto.getCoordinate(), saveDto.getTimetable(), saveDto.getIsPublished());

        recruitment.setWriter(userRepository.findById(loginUserNo)
                .orElseThrow(()-> new BusinessException(ErrorCode.UNAUTHORIZED_USER,
                        String.format("Unauthorized UserNo = [%d]", loginUserNo))));

        return recruitmentRepository.save(recruitment);
    }

    @Override
    @Transactional
    public void deleteRecruitment(Long deleteNo) {

        Recruitment findRecruitment =
                recruitmentRepository.findValidRecruitment(deleteNo).orElseThrow(
                        () -> new BusinessException(ErrorCode.NOT_EXIST_RECRUITMENT, String.format("Delete Recruitment ID = [%d]", deleteNo)));

        //정기일 경우 반복주기 삭제
        if(findRecruitment.getVolunteeringType().equals(VolunteeringType.REG)){
            List<RepeatPeriod> findPeriod = repeatPeriodRepository.findByRecruitment_RecruitmentNo(findRecruitment.getRecruitmentNo());
            findPeriod.stream()
                    .forEach(p -> p.setDeleted());
        }

        //모집글 삭제
        findRecruitment.setDeleted();
    }

}
