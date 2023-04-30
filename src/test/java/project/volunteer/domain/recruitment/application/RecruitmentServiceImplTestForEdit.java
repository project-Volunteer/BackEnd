package project.volunteer.domain.recruitment.application;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.Rollback;
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
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.common.component.IsDeleted;

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
        final String nickname = "nickname";
        final String email = "email@gmail.com";
        final Gender gender = Gender.M;
        final LocalDate birth = LocalDate.now();
        final String picture = "picture";
        final Boolean alarm = true;

        userRepository.save(User.builder().nickName(nickname)
                .email(email).gender(gender).birthDay(birth).picture(picture)
                .joinAlarmYn(alarm).beforeAlarmYn(alarm).noticeAlarmYn(alarm)
                .provider("kakao").providerId("1234").build());
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
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION) //@BeforeEach 어노테이션부터 활성화하도록!!
    @Rollback(value = false)
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
}