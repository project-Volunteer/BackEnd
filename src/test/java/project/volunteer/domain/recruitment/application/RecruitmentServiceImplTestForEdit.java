package project.volunteer.domain.recruitment.application;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitment.application.dto.RecruitmentParam;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.repeatPeriod.application.RepeatPeriodService;
import project.volunteer.domain.repeatPeriod.application.dto.RepeatPeriodParam;
import project.volunteer.domain.repeatPeriod.dao.RepeatPeriodRepository;
import project.volunteer.domain.repeatPeriod.domain.Day;
import project.volunteer.domain.repeatPeriod.domain.RepeatPeriod;
import project.volunteer.domain.repeatPeriod.domain.Week;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.global.error.exception.BusinessException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.List;

@SpringBootTest
@Transactional
class RecruitmentServiceImplTestForEdit {

    @PersistenceContext EntityManager em;
    @Autowired UserRepository userRepository;
    @Autowired RecruitmentService recruitmentService;
    @Autowired RecruitmentRepository recruitmentRepository;
    @Autowired RepeatPeriodService repeatPeriodService;
    @Autowired RepeatPeriodRepository repeatPeriodRepository;

    private Long saveRecruitmentNo;
    private void clear() {
        em.flush();
        em.clear();
    }
    @BeforeEach
    private void initUser() {
        userRepository.save(User.builder()
                .id("1234")
                .password("1234")
                .nickName("nickname")
                .email("email@gmail.com")
                .gender(Gender.M)
                .birthDay(LocalDate.now())
                .picture("picture")
                .joinAlarmYn(true).beforeAlarmYn(true).noticeAlarmYn(true)
                .role(Role.USER)
                .provider("kakao").providerId("1234")
                .build());
        clear();
    }

    private void setRegRecruitment() {
        //정기-모집글 저장
        String category = "001";
        String volunteeringType = VolunteeringType.REG.name();
        String volunteerType = "1"; //all
        Boolean isIssued = true;
        String sido = "11";
        String sigungu = "1111";
        String organizationName = "name";
        String details = "details";
        Float latitude = 3.2F, longitude = 3.2F;
        Integer volunteerNum = 5;
        String startDay = "01-01-2000";
        String endDay = "01-01-2000";
        String hourFormat = HourFormat.AM.name();
        String startTime = "01:01";
        Integer progressTime = 3;
        String title = "title", content = "content";
        Boolean isPublished = true;
        RecruitmentParam saveRecruitDto = new RecruitmentParam(category, organizationName, sido, sigungu, details, latitude, longitude,
                isIssued, volunteerType, volunteerNum, volunteeringType, startDay, endDay, hourFormat, startTime, progressTime, title, content, isPublished);
        saveRecruitmentNo = recruitmentService.addRecruitment(saveRecruitDto);

        //정기-주기 저장
        String period = "month";
        int week = Week.FIRST.getValue();
        List<Integer> days = List.of(Day.MON.getValue(), Day.TUES.getValue());
        RepeatPeriodParam savePeriodDto = new RepeatPeriodParam(period, week, days);
        repeatPeriodService.addRepeatPeriod(saveRecruitmentNo, savePeriodDto);

        clear();
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 정기모집글_삭제_성공_반복주기포함(){
        //given
        setRegRecruitment();

        //when
        recruitmentService.deleteRecruitment(saveRecruitmentNo);
        clear();

        //then
        Recruitment recruitment = recruitmentRepository.findById(saveRecruitmentNo).get();
        Assertions.assertThat(recruitment.getIsDeleted()).isEqualTo(IsDeleted.Y);

        List<RepeatPeriod> findPeriod = repeatPeriodRepository.findByRecruitment_RecruitmentNo(saveRecruitmentNo);
        findPeriod.stream()
                .forEach(p -> Assertions.assertThat(p.getIsDeleted()).isEqualTo(IsDeleted.Y));
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 정기모집글_삭제_실패_없는모집글(){
        //given
        setRegRecruitment();

        //when & then
        Assertions.assertThatThrownBy(() -> recruitmentService.deleteRecruitment(Long.MAX_VALUE)) //없는 모집글 PK
                .isInstanceOf(BusinessException.class);
    }

}