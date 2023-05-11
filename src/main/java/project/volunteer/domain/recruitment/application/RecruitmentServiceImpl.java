package project.volunteer.domain.recruitment.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.application.dto.RecruitmentParam;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.repeatPeriod.dao.RepeatPeriodRepository;
import project.volunteer.domain.repeatPeriod.domain.RepeatPeriod;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;
import project.volunteer.global.util.SecurityUtil;

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
    public Long addRecruitment(RecruitmentParam saveDto){

        Recruitment recruitment = Recruitment.builder()
                .title(saveDto.getTitle())
                .content(saveDto.getContent())
                .volunteeringCategory(saveDto.getVolunteeringCategory())
                .volunteeringType(saveDto.getVolunteeringType())
                .volunteerType(saveDto.getVolunteerType())
                .volunteerNum(saveDto.getVolunteerNum())
                .isIssued(saveDto.getIsIssued())
                .organizationName(saveDto.getOrganizationName())
                .address(saveDto.getAddress())
                .coordinate(saveDto.getCoordinate())
                .timetable(saveDto.getTimetable())
                .isPublished(saveDto.getIsPublished())
                .build();

        //1. SecurityUtil 를 통해서 사용자 정보 가져오기 및 예외 처리
        //2. 연관관계 세팅
        //추후 수정 필요 (임시)
        recruitment.setWriter(userRepository.findById(SecurityUtil.getLoginUserId())
                .orElseThrow(()-> new BusinessException(ErrorCode.NOT_EXIST_USER, String.format("Search User ID = [%d]", SecurityUtil.getLoginUserId()))));

        return recruitmentRepository.save(recruitment).getRecruitmentNo();
    }

    @Override
    @Transactional
    public void deleteRecruitment(Long deleteNo) {

        Recruitment findRecruitment =
                recruitmentRepository.findValidByRecruitmentNo(deleteNo).orElseThrow(
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
