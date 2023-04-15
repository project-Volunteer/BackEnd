package project.volunteer.domain.sehedule.application;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitment.application.RecruitmentService;
import project.volunteer.domain.recruitment.application.dto.RecruitmentParam;
import project.volunteer.domain.repeatPeriod.application.dto.RepeatPeriodParam;
import project.volunteer.domain.repeatPeriod.domain.Day;
import project.volunteer.domain.repeatPeriod.domain.Week;
import project.volunteer.domain.sehedule.application.dto.ScheduleParamReg;
import project.volunteer.domain.sehedule.dao.ScheduleRepository;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.domain.sehedule.application.dto.ScheduleParam;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.HourFormat;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@SpringBootTest
@Transactional
class ScheduleServiceImplTest {

    @PersistenceContext EntityManager em;
    @Autowired ScheduleService scheduleService;
    @Autowired ScheduleRepository scheduleRepository;
    @Autowired UserRepository userRepository;
    @Autowired RecruitmentService recruitmentService;

    private Long saveRecruitmentNo;
    private void clear() {
        em.flush();
        em.clear();
    }
    private void setMockUpData(){
        //임시 모집글 등록
        String category = "001";
        String organizationName ="name";
        String sido = "11";
        String sigungu = "11011";
        String details = "details";
        Float latitude = 3.2F , longitude = 3.2F;
        Boolean isIssued = true;
        String volunteerType = "1"; //all
        Integer volunteerNum = 5;
        String volunteeringType = "reg";
        String startDay = "01-01-2000";
        String endDay = "01-01-2000";
        String hourFormat = HourFormat.AM.name();
        String startTime = "01:01";
        Integer progressTime = 3;
        String title = "title", content = "content";
        Boolean isPublished = true;
        RecruitmentParam saveRecruitDto = new RecruitmentParam(category, organizationName, sido,sigungu, details, latitude, longitude,
                isIssued, volunteerType, volunteerNum, volunteeringType, startDay, endDay,hourFormat, startTime, progressTime, title, content, isPublished);
        saveRecruitmentNo = recruitmentService.addRecruitment(saveRecruitDto);
        clear();
    }
    @BeforeEach
    private void initUser() {
        //임시 유저 회원가입 및 인증
        String nickname = "nickname";
        String email = "email@gmail.com";
        Gender gender = Gender.M;
        LocalDate birth = LocalDate.now();
        String picture = "picture";
        Boolean alarm = true;
        userRepository.save(User.builder().nickName(nickname)
                .email(email).gender(gender).birthDay(birth).picture(picture)
                .joinAlarmYn(alarm).beforeAlarmYn(alarm).noticeAlarmYn(alarm)
                .provider("kakao").providerId("1234").build());
        clear();
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION) //@BeforeEach 어노테이션부터 활성화하도록!!
    public void 스케줄_등록_성공() {
        //init
        setMockUpData();

        //given
        String startDay = "01-01-2000";
        String hourFormat = HourFormat.AM.name();
        String startTime = "01:01";
        Integer progressTime = 3;
        String content = "content";
        String organizationName ="name";
        String sido = "11";
        String sigungu = "11011";
        String details = "details";
        ScheduleParam dto = new ScheduleParam(startDay,startDay, hourFormat, startTime, progressTime,
                organizationName, sido, sigungu, details, content);

        //when
        scheduleService.addSchedule(saveRecruitmentNo, dto);
        clear();

        //then
        Schedule schedule = scheduleRepository.findByRecruitment_RecruitmentNo(saveRecruitmentNo).get();
        Assertions.assertThat(schedule.getAddress().getSido()).isEqualTo(sido);
        Assertions.assertThat(schedule.getAddress().getSigungu()).isEqualTo(sigungu);
        Assertions.assertThat(schedule.getContent()).isEqualTo(content);
        Assertions.assertThat(schedule.getScheduleTimeTable().getStartDay().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"))).isEqualTo(startDay);
        Assertions.assertThat(schedule.getScheduleTimeTable().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"))).isEqualTo(startTime);
        Assertions.assertThat(schedule.getScheduleTimeTable().getProgressTime()).isEqualTo(progressTime);
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION) //@BeforeEach 어노테이션부터 활성화하도록!!
    public void 스케줄_등록_실패_없는모집글() {
        //init
        setMockUpData();

        //given
        String startDay = "01-01-2000";
        String hourFormat = HourFormat.AM.name();
        String startTime = "01:01";
        Integer progressTime = 3;
        String content = "content";
        String organizationName ="name";
        String sido = "11";
        String sigungu = "11011";
        String details = "details";
        ScheduleParam dto = new ScheduleParam(startDay, startDay,hourFormat, startTime, progressTime,
                organizationName, sido, sigungu, details, content);

        //when & then
        Assertions.assertThatThrownBy(() -> scheduleService.addSchedule(Long.MAX_VALUE, dto))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION) //@BeforeEach 어노테이션부터 활성화하도록!!
    public void 정기모집글_스케줄_자동생성_매달_성공(){
        //init
        setMockUpData();

        //given
        String startDay = "12-01-2022";
        String endDay = "02-01-2023";
        String hourFormat = HourFormat.AM.name();
        String startTime = "01:01";
        Integer progressTime = 3;
        String content = "content";
        String organizationName ="name";
        String sido = "11";
        String sigungu = "11011";
        String details = "details";
        String period = "month"; //매달
        int week = Week.FOUR.getValue();
        List<Integer> days = List.of(Day.MON.getValue(), Day.TUES.getValue());
        RepeatPeriodParam periodParam = new RepeatPeriodParam(period, week, days);
        ScheduleParamReg reg = ScheduleParamReg.builder()
                .startDay(startDay)
                .endDay(endDay)
                .hourFormat(hourFormat)
                .startTime(startTime)
                .progressTime(progressTime)
                .organizationName(organizationName)
                .sido(sido)
                .sigungu(sigungu)
                .details(details)
                .content(content)
                .periodParam(periodParam)
                .build();

        //when & then
        scheduleService.addRegSchedule(saveRecruitmentNo, reg);
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION) //@BeforeEach 어노테이션부터 활성화하도록!!
    public void 정기모집글_스케줄_자동생성_매주_성공(){
        //init
        setMockUpData();

        //given
        String startDay = "12-01-2022";
        String endDay = "01-10-2023";
        String hourFormat = HourFormat.AM.name();
        String startTime = "01:01";
        Integer progressTime = 3;
        String content = "content";
        String organizationName ="name";
        String sido = "11";
        String sigungu = "11011";
        String details = "details";
        String period = "week"; //매주
        int week = 0;
        List<Integer> days = List.of(Day.MON.getValue(), Day.TUES.getValue());
        RepeatPeriodParam periodParam = new RepeatPeriodParam(period, week, days);
        ScheduleParamReg reg = ScheduleParamReg.builder()
                .startDay(startDay)
                .endDay(endDay)
                .hourFormat(hourFormat)
                .startTime(startTime)
                .progressTime(progressTime)
                .organizationName(organizationName)
                .sido(sido)
                .sigungu(sigungu)
                .details(details)
                .content(content)
                .periodParam(periodParam)
                .build();

        //when & then
        scheduleService.addRegSchedule(saveRecruitmentNo, reg);
    }

}