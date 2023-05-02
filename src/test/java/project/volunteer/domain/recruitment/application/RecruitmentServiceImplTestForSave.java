package project.volunteer.domain.recruitment.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.application.dto.RecruitmentParam;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.repeatPeriod.application.RepeatPeriodService;
import project.volunteer.domain.repeatPeriod.dao.RepeatPeriodRepository;
import project.volunteer.domain.repeatPeriod.domain.Day;
import project.volunteer.domain.repeatPeriod.domain.Period;
import project.volunteer.domain.repeatPeriod.domain.RepeatPeriod;
import project.volunteer.domain.repeatPeriod.domain.Week;
import project.volunteer.domain.repeatPeriod.application.dto.RepeatPeriodParam;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.HourFormat;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

//@ExtendWith(MockitoExtension.class)
@SpringBootTest
@Transactional
class RecruitmentServiceImplTestForSave {

    //@Mock
    @Autowired
    private EntityManager em;
    //@InjectMocks
    //RecruitmentServiceImpl recruitmentService;
    @Autowired
    RecruitmentService recruitmentService;
    @Autowired
    RepeatPeriodService repeatPeriodService;
    //@Mock
    @Autowired
    RecruitmentRepository recruitmentRepository;
    @Autowired
    RepeatPeriodRepository repeatPeriodRepository;
    //@Mock
    @Autowired
    UserRepository userRepository;

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

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION) //@BeforeEach 어노테이션부터 활성화하도록!!
    public void 모집글_작성_저장_성공_비정기(){
        //given
        String category = "001";
        String organizationName ="name";
        String sido = "11";
        String sigungu = "11011";
        String details = "details";
        Float latitude = 3.2F , longitude = 3.2F;
        Boolean isIssued = true;
        String volunteerType = "1"; //all
        Integer volunteerNum = 5;
        String volunteeringType = VolunteeringType.IRREG.name();
        String startDay = "01-01-2000";
        String endDay = "01-01-2000";
        String hourFormat = HourFormat.AM.name();
        String startTime = "01:01";
        Integer progressTime = 5;
        String title = "title", content = "content";
        Boolean isPublished = true;
        RecruitmentParam saveRecruitDto = new RecruitmentParam(category, organizationName, sido,sigungu, details, latitude, longitude,
                isIssued, volunteerType, volunteerNum, volunteeringType, startDay, endDay, hourFormat, startTime, progressTime, title, content, isPublished);

        //when
        Long no = recruitmentService.addRecruitment(saveRecruitDto);
        clear();

        //then
        Recruitment find = recruitmentRepository.findById(no).get();
        assertThat(find.getVolunteeringCategory().getLegacyCode()).isEqualTo(category);
        assertThat(find.getOrganizationName()).isEqualTo(organizationName);
        //...
        assertThat(find.getContent()).isEqualTo(content);
        assertThat(find.getTitle()).isEqualTo(title);
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION) //@BeforeEach 어노테이션부터 활성화하도록!!
    public void 모집글_작성_저장_성공_정기_매주() {
        //given
        String category = "001";
        String organizationName ="name";
        String sido = "11";
        String sigungu = "11011";
        String details = "details";
        Float latitude = 3.2F , longitude = 3.2F;
        Boolean isIssued = true;
        String volunteerType = "1"; //all
        Integer volunteerNum = 5;
        String volunteeringType = VolunteeringType.REG.name();
        String startDay = "01-01-2000";
        String endDay = "01-01-2000";
        String hourFormat = HourFormat.AM.name();
        String startTime = "01:01";
        Integer progressTime = 3;
        String title = "title", content = "content";
        Boolean isPublished = true;
        RecruitmentParam saveRecruitDto = new RecruitmentParam(category, organizationName, sido, sigungu, details, latitude, longitude,
                isIssued, volunteerType, volunteerNum, volunteeringType, startDay, endDay, hourFormat, startTime, progressTime, title, content, isPublished);

        String period = "week";
        int week = 0;
        List<Integer> days = List.of(Day.MON.getValue(), Day.TUES.getValue());
        RepeatPeriodParam savePeriodDto = new RepeatPeriodParam(period, week, days);

        //when
        Long no = recruitmentService.addRecruitment(saveRecruitDto);
        repeatPeriodService.addRepeatPeriod(no, savePeriodDto);
        clear();

        //then
        List<RepeatPeriod> list = repeatPeriodRepository.findByRecruitment_RecruitmentNo(no);
        assertThat(list.get(0).getDay().name()).isEqualTo(Day.MON.name());
        assertThat(list.get(1).getDay().name()).isEqualTo(Day.TUES.name());
        assertThat(list.get(0).getPeriod().name()).isEqualTo(Period.WEEK.name());
        assertThat(list.get(1).getPeriod().name()).isEqualTo(Period.WEEK.name());
        assertThat(list.get(0).getWeek()).isNull();
        assertThat(list.get(1).getWeek()).isNull();
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION) //@BeforeEach 어노테이션부터 활성화하도록!!
    public void 모집글_작성_저장_성공_정기_매월(){
        //given
        String category = "001";
        String organizationName ="name";
        String sido = "11";
        String sigungu = "11011";
        String details = "details";
        Float latitude = 3.2F , longitude = 3.2F;
        Boolean isIssued = true;
        String volunteerType = "1"; //all
        Integer volunteerNum = 5;
        String volunteeringType = VolunteeringType.REG.name();
        String startDay = "01-01-2000";
        String endDay = "01-01-2000";
        String hourFormat = HourFormat.AM.name();
        String startTime = "01:01";
        Integer progressTime = 3;
        String title = "title", content = "content";
        Boolean isPublished = true;
        RecruitmentParam saveRecruitDto = new RecruitmentParam(category, organizationName, sido, sigungu, details, latitude, longitude,
                isIssued, volunteerType, volunteerNum, volunteeringType, startDay, endDay, hourFormat, startTime, progressTime, title, content, isPublished);

        String period = "month";
        int week = Week.FIRST.getValue();
        List<Integer> days = List.of(Day.MON.getValue(), Day.TUES.getValue());
        RepeatPeriodParam savePeriodDto = new RepeatPeriodParam(period, week, days);

        //when
        Long no = recruitmentService.addRecruitment(saveRecruitDto);
        repeatPeriodService.addRepeatPeriod(no, savePeriodDto);
        clear();

        //then
        List<RepeatPeriod> list = repeatPeriodRepository.findByRecruitment_RecruitmentNo(no);
        assertThat(list.get(0).getDay().name()).isEqualTo(Day.MON.name());
        assertThat(list.get(1).getDay().name()).isEqualTo(Day.TUES.name());
        assertThat(list.get(0).getPeriod().name()).isEqualTo(Period.MONTH.name());
        assertThat(list.get(1).getPeriod().name()).isEqualTo(Period.MONTH.name());
        assertThat(list.get(0).getWeek().name()).isEqualTo(Week.FIRST.name());
        assertThat(list.get(1).getWeek().name()).isEqualTo(Week.FIRST.name());
    }

}