package project.volunteer.domain.recruitment.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.application.dto.RecruitmentParam;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.dao.RepeatPeriodRepository;
import project.volunteer.domain.recruitment.domain.Day;
import project.volunteer.domain.recruitment.domain.Period;
import project.volunteer.domain.recruitment.domain.Week;
import project.volunteer.domain.recruitment.application.dto.RepeatPeriodCommand;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.Coordinate;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.common.component.Timetable;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class RecruitmentServiceImplTestForSave {


    @Autowired private EntityManager em;
    @Autowired RecruitmentService recruitmentService;
    @Autowired RepeatPeriodService repeatPeriodService;
    @Autowired RecruitmentRepository recruitmentRepository;
    @Autowired RepeatPeriodRepository repeatPeriodRepository;
    @Autowired UserRepository userRepository;

    User writer;
    private void clear() {
        em.flush();
        em.clear();
    }
    @BeforeEach
    private void init() {
        writer = User.createUser("1234", "1234", "1234", "1234", Gender.M, LocalDate.now(), "1234",
                true, true, true, Role.USER, "kakao", "1234", null);
        userRepository.save(writer);
    }

    @Test
    @Transactional
    public void 모집글_작성_저장_성공_비정기(){
        //given
        final String title = "title";
        final String content = "content";
        final VolunteeringCategory category = VolunteeringCategory.EDUCATION;
        final VolunteeringType volunteeringType = VolunteeringType.IRREG;
        final VolunteerType volunteerType = VolunteerType.TEENAGER;
        final int volunteerNum = 4;
        final Boolean isIssued = true;
        final  String organizationName = "name";
        final Address address = Address.createAddress("1", "111", "details", "fullName");
        final Coordinate coordinate = Coordinate.createCoordinate(3.2F, 3.2F);
        final Timetable timetable = Timetable.createTimetable(LocalDate.now(), LocalDate.now().plusMonths(3), HourFormat.AM, LocalTime.now(), 3);
        final  Boolean isPublished = true;

        RecruitmentParam param = new RecruitmentParam(title, content, category, volunteeringType, volunteerType, volunteerNum, isIssued, organizationName, address,
                coordinate, timetable, isPublished);

        //when
        Long no = recruitmentService.addRecruitment(writer, param).getRecruitmentNo();
        clear();

        //then
        Recruitment find = recruitmentRepository.findById(no).get();
        assertThat(find.getVolunteeringType()).isEqualTo(volunteeringType);
        assertThat(find.getOrganizationName()).isEqualTo(organizationName);
        assertThat(find.getContent()).isEqualTo(content);
        assertThat(find.getTitle()).isEqualTo(title);
    }

    @Test
    @Transactional
    public void 모집글_작성_저장_성공_정기_매주() {
        //given
        final String title = "title";
        final String content = "content";
        final VolunteeringCategory category = VolunteeringCategory.EDUCATION;
        final VolunteeringType volunteeringType = VolunteeringType.REG;
        final VolunteerType volunteerType = VolunteerType.TEENAGER;
        final int volunteerNum = 4;
        final Boolean isIssued = true;
        final  String organizationName = "name";
        final Address address = Address.createAddress("1", "111", "details", "fullName");
        final Coordinate coordinate = Coordinate.createCoordinate(3.2F, 3.2F);
        final Timetable timetable = Timetable.createTimetable(LocalDate.now(), LocalDate.now().plusMonths(3), HourFormat.AM, LocalTime.now(), 3);
        final  Boolean isPublished = true;
        RecruitmentParam param = new RecruitmentParam(title, content, category, volunteeringType, volunteerType, volunteerNum, isIssued, organizationName, address,
                coordinate, timetable, isPublished);

        final Period period = Period.WEEK;
        final Week week = null;
        final List<Day> days = List.of(Day.MON, Day.TUES);
        RepeatPeriodCommand repeatPeriodParam = new RepeatPeriodCommand(period, week, days);

        //when
        Recruitment recruitment = recruitmentService.addRecruitment(writer, param);
        repeatPeriodService.addRepeatPeriod(recruitment, repeatPeriodParam);
        clear();

        //then
        Recruitment find = recruitmentRepository.findById(recruitment.getRecruitmentNo()).get();
        assertThat(find.getVolunteeringType()).isEqualTo(volunteeringType);

        List<project.volunteer.domain.recruitment.domain.RepeatPeriod> list = repeatPeriodRepository.findByRecruitment_RecruitmentNo(recruitment.getRecruitmentNo());
        assertThat(list.get(0).getDay()).isEqualTo(Day.MON);
        assertThat(list.get(1).getDay()).isEqualTo(Day.TUES);
        assertThat(list.get(0).getPeriod()).isEqualTo(Period.WEEK);
        assertThat(list.get(1).getPeriod()).isEqualTo(Period.WEEK);
        assertThat(list.get(0).getWeek()).isNull();
        assertThat(list.get(1).getWeek()).isNull();
    }

    @Test
    @Transactional
    public void 모집글_작성_저장_성공_정기_매월(){
        //given
        final String title = "title";
        final String content = "content";
        final VolunteeringCategory category = VolunteeringCategory.EDUCATION;
        final VolunteeringType volunteeringType = VolunteeringType.REG;
        final VolunteerType volunteerType = VolunteerType.TEENAGER;
        final int volunteerNum = 4;
        final Boolean isIssued = true;
        final  String organizationName = "name";
        final Address address = Address.createAddress("1", "111", "details", "fullName");
        final Coordinate coordinate = Coordinate.createCoordinate(3.2F, 3.2F);
        final Timetable timetable = Timetable.createTimetable(LocalDate.now(), LocalDate.now().plusMonths(3), HourFormat.AM, LocalTime.now(), 3);
        final  Boolean isPublished = true;
        RecruitmentParam param = new RecruitmentParam(title, content, category, volunteeringType, volunteerType, volunteerNum, isIssued, organizationName, address,
                coordinate, timetable, isPublished);

        final Period period = Period.MONTH;
        final Week week = Week.FIRST;
        final List<Day> days = List.of(Day.MON, Day.TUES);
        RepeatPeriodCommand repeatPeriodParam = new RepeatPeriodCommand(period, week, days);


        //when
        Recruitment recruitment = recruitmentService.addRecruitment(writer, param);
        repeatPeriodService.addRepeatPeriod(recruitment, repeatPeriodParam);
        clear();

        //then
        Recruitment find = recruitmentRepository.findById(recruitment.getRecruitmentNo()).get();
        assertThat(find.getVolunteeringType()).isEqualTo(volunteeringType);

        List<project.volunteer.domain.recruitment.domain.RepeatPeriod> list = repeatPeriodRepository.findByRecruitment_RecruitmentNo(recruitment.getRecruitmentNo());
        assertThat(list.get(0).getDay()).isEqualTo(Day.MON);
        assertThat(list.get(1).getDay()).isEqualTo(Day.TUES);
        assertThat(list.get(0).getPeriod()).isEqualTo(Period.MONTH);
        assertThat(list.get(1).getPeriod()).isEqualTo(Period.MONTH);
        assertThat(list.get(0).getWeek()).isEqualTo(Week.FIRST);
        assertThat(list.get(1).getWeek()).isEqualTo(Week.FIRST);
    }

}