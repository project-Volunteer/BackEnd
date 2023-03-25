package project.volunteer.domain.recruitment.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.dto.SaveRecruitDto;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.global.util.SecurityUtil;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecruitmentServiceImpl implements RecruitmentService{

    private final UserRepository userRepository;
    private final RecruitmentRepository recruitmentRepository;

    @Transactional
    public Long addRecruitment(SaveRecruitDto saveDto){

        Recruitment recruitment = Recruitment.builder()
                .title(saveDto.getTitle())
                .content(saveDto.getContent())
                .volunteeringCategory(saveDto.getVolunteeringCategory())
                .volunteeringType(saveDto.getVolunteeringType())
                .volunteerType(saveDto.getVolunteerType())
                .volunteerNum(saveDto.getVolunteerNum())
                .isIssued(saveDto.getIsIssued())
                .organizationName(saveDto.getOrganizationName())
                .country(saveDto.getCountry())
                .details(saveDto.getDetails())
                .latitude(saveDto.getLatitude())
                .longitude(saveDto.getLongitude())
                .startDay(saveDto.getStartDay())
                .endDay(saveDto.getEndDay())
                .startTime(saveDto.getStartTime())
                .progressTime(saveDto.getProgressTime())
                .isPublished(saveDto.getIsPublished())
                .build();

        //1. SecurityUtil 를 통해서 사용자 정보 가져오기 및 예외 처리
        //2. 연관관계 세팅
        //추후 수정 필요 (임시)
        recruitment.setWriter(userRepository.findByEmail(SecurityUtil.getLoginUserEmail())
                .orElseThrow(()-> new NullPointerException("존재하지 않는 회원입니다.")));

        return  recruitmentRepository.save(recruitment).getRecruitmentNo();
    }
}
