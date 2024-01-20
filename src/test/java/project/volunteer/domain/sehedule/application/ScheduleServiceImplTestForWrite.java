package project.volunteer.domain.sehedule.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitment.application.RecruitmentService;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.application.dto.RepeatPeriodParam;
import project.volunteer.domain.recruitment.domain.Day;
import project.volunteer.domain.recruitment.domain.Period;
import project.volunteer.domain.recruitment.domain.Week;
import project.volunteer.domain.sehedule.application.dto.ScheduleParamReg;
import project.volunteer.domain.sehedule.dao.ScheduleRepository;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.domain.sehedule.application.dto.ScheduleCreateCommand;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.Coordinate;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.common.component.Timetable;
import project.volunteer.global.error.exception.BusinessException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
class ScheduleServiceImplTestForWrite {

    @PersistenceContext EntityManager em;
    @Autowired
    ScheduleCommandUseCase scheduleService;
    @Autowired ScheduleRepository scheduleRepository;
    @Autowired UserRepository userRepository;
    @Autowired RecruitmentService recruitmentService;
    @Autowired RecruitmentRepository recruitmentRepository;

    User writer;
    Recruitment saveRecruitment;
    private void clear() {
        em.flush();
        em.clear();
    }
    @BeforeEach
    private void setUp() {
        //작성자 저장
        writer = User.createUser("1234", "1234", "1234", "1234", Gender.M, LocalDate.now(), "1234",
                true, true, true, Role.USER, "kakao", "1234", null);
        userRepository.save(writer);

        //Embedded 값 세팅
        Address address = Address.createAddress("1", "111", "test", "fullName");
        Timetable timetable = Timetable.createTimetable(LocalDate.now(), LocalDate.now(), HourFormat.AM, LocalTime.now(), 10);
        Coordinate coordinate = Coordinate.createCoordinate(3.2F, 3.2F);

        //봉사 모집글 저장
        saveRecruitment =
                Recruitment.createRecruitment("test", "test", VolunteeringCategory.EDUCATION, VolunteeringType.IRREG,
                        VolunteerType.TEENAGER, 10, true, "test", address, coordinate, timetable, true);
        saveRecruitment.setWriter(writer);
        recruitmentRepository.save(saveRecruitment);
    }

    //TODO: 봉사 모집글 service 테스트 코드에 옮겨져야할 테스트
//    @Test
//    @Transactional
//    @DisplayName("유효하지않은 봉사 모집글에 일정을 추가하여 에러가 발생한다.")
//    public void notExistRecruitment(){
//        //given
//        final Timetable timetable = Timetable.createTimetable(LocalDate.now(), LocalDate.now(), HourFormat.AM, LocalTime.now(),3);
//        final Address address = Address.createAddress("1", "111", "details");
//        final String organizationName = "test";
//        final String content = "test";
//        final int volunteerNum = 3;
//        ScheduleParam param = new ScheduleParam(timetable, organizationName, address, content, volunteerNum);
//
//        //when && then
//        assertThatThrownBy(() -> scheduleService.addSchedule(Long.MAX_VALUE, param))
//                .isInstanceOf(BusinessException.class)
//                .hasMessageContaining("NOT_EXIST_RECRUITMENT");
//    }

    @Disabled
    @Test
    @Transactional
    @DisplayName("방장이 아닌 사용자가 일정을 등록을 시도하여 권한에러가 발생한다.")
    public void notRecruitmentOwner(){
        //given
        final Timetable timetable = Timetable.createTimetable(LocalDate.now(), LocalDate.now(), HourFormat.AM, LocalTime.now(),3);
        final Address address = Address.createAddress("1", "111", "details", "fullName");
        final String organizationName = "test";
        final String content = "test";
        final int volunteerNum = 3;
        ScheduleCreateCommand param = new ScheduleCreateCommand(timetable, organizationName, address, content, volunteerNum);

        //when && then
        assertThatThrownBy(() -> scheduleService.addSchedule(saveRecruitment, param))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("FORBIDDEN_RECRUITMENT");
    }

    @Test
    @Transactional
    @DisplayName("일정 모집 인원이 봉사 팀원 최대 모집인원을 초과하다.")
    public void exceedScheduleVolunteerNum(){
        //given
        final Timetable timetable = Timetable.createTimetable(LocalDate.now(), LocalDate.now(), HourFormat.AM, LocalTime.now(),3);
        final Address address = Address.createAddress("1", "111", "details", "fullName");
        final String organizationName = "test";
        final String content = "test";
        final int volunteerNum = 100; //초과!!
        ScheduleCreateCommand param = new ScheduleCreateCommand(timetable, organizationName, address, content, volunteerNum);

        //when && then
        assertThatThrownBy(() -> scheduleService.addSchedule(saveRecruitment, param))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("EXCEED_CAPACITY_PARTICIPANT");
    }

    @Test
    @Transactional
    @DisplayName("반복주기가 매달인 봉사 일정 자동 등록에 성공한다.")
    public void createScheduleWithMonth(){
        //given
        final Timetable timetable = Timetable.createTimetable(
                LocalDate.of(2023, 1,2), LocalDate.of(2023,5,8), HourFormat.AM, LocalTime.now(), 3);
        final String organizationName ="test";
        final String content = "test";
        final int volunteerNum = 10;
        final Address address = Address.createAddress("1", "1111", "details", "fullName");
        final RepeatPeriodParam repeatPeriodParam = new RepeatPeriodParam(Period.MONTH, Week.FIRST, List.of(Day.MON, Day.TUES));
        final ScheduleParamReg dto = new ScheduleParamReg(timetable, repeatPeriodParam, organizationName, address, content, volunteerNum);

        //when
        List<Long> saveScheduleNos = scheduleService.addRegSchedule(saveRecruitment, dto);
        clear();

        //then
        assertThat(saveScheduleNos.size()).isEqualTo(10);
    }

    @Test
    @Transactional
    @DisplayName("반복주기가 매주인 봉사 일정 자동 등록에 성공한다.")
    public void createScheduleWithWeek(){
        //given
        final Timetable timetable = Timetable.createTimetable(
                LocalDate.of(2023, 1,2), LocalDate.of(2023,2,1), HourFormat.AM, LocalTime.now(), 3);
        final String organizationName ="test";
        final String content = "test";
        final int volunteerNum = 10;
        final Address address = Address.createAddress("1", "1111", "details", "fullName");
        final RepeatPeriodParam repeatPeriodParam = new RepeatPeriodParam(Period.WEEK, null, List.of(Day.SAT, Day.SUN));
        final ScheduleParamReg dto = new ScheduleParamReg(timetable, repeatPeriodParam, organizationName, address, content, volunteerNum);

        //when
        List<Long> saveScheduleNos = scheduleService.addRegSchedule(saveRecruitment, dto);
        clear();

        //then
        assertThat(saveScheduleNos.size()).isEqualTo(8);
    }

}