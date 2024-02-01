package project.volunteer.domain.sehedule.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.participation.dao.ParticipantRepository;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.scheduleParticipation.dao.ScheduleParticipationRepository;
import project.volunteer.domain.sehedule.repository.ScheduleRepository;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.*;
import project.volunteer.global.error.exception.BusinessException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@Transactional
class ScheduleDtoServiceImplTest {

    @PersistenceContext EntityManager em;
    @Autowired UserRepository userRepository;
    @Autowired RecruitmentRepository recruitmentRepository;
    @Autowired ScheduleRepository scheduleRepository;
    @Autowired
    ScheduleCommandUseCase scheduleCommandService;
    @Autowired
    ScheduleQueryUseCase scheduleQueryService;
    @Autowired ParticipantRepository participantRepository;
    @Autowired ScheduleParticipationRepository scheduleParticipationRepository;

    User writer;
    Recruitment saveRecruitment;

    private void clear() {
        em.flush();
        em.clear();
    }
    @BeforeEach
    private void init() {
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

    private Schedule 스케줄_등록(LocalDate startDay, int volunteerNum){
        Timetable timetable = Timetable.createTimetable(startDay, startDay, HourFormat.AM, LocalTime.now(), 10);
        Address address = Address.createAddress("1", "111", "test", "fullName");

        Schedule schedule = Schedule.create(saveRecruitment, timetable, "test" ,"test", address, volunteerNum);
        return scheduleRepository.save(schedule);
    }

    @Test
    @DisplayName("3개의 스케줄 중 모집 중이며 가장 가까운 schedule3 일정 조회에 성공한다.")
    public void findClosestSchedule(){
        //given
        Schedule schedule1 = 스케줄_등록(LocalDate.now(), 3);
        Schedule schedule2 = 스케줄_등록(LocalDate.now().plusMonths(3) , 3);
        Schedule schedule3 = 스케줄_등록(LocalDate.now().plusMonths(2), 3);

        //when
        Schedule closestSchedule = scheduleQueryService.findClosestSchedule(saveRecruitment.getRecruitmentNo());

        //then
        assertAll(
                () -> assertThat(closestSchedule.getContent()).isEqualTo(schedule3.getContent()),
                () -> assertThat(closestSchedule.getScheduleTimeTable().getStartDay()).isEqualTo(schedule3.getScheduleTimeTable().getStartDay()),
                () -> assertThat(closestSchedule.getScheduleTimeTable().getHourFormat()).isEqualTo(schedule3.getScheduleTimeTable().getHourFormat()),
                () -> assertThat(closestSchedule.getScheduleTimeTable().getStartTime()).isEqualTo(schedule3.getScheduleTimeTable().getStartTime()),
                () -> assertThat(closestSchedule.getVolunteerNum()).isEqualTo(schedule3.getVolunteerNum()),
                () ->  assertThat(closestSchedule.getCurrentVolunteerNum()).isEqualTo(0));
    }

    @Disabled
    @Test
    @DisplayName("팀원이 아니므로 일정 조회를 하지 못한다.")
    public void notVolunteerTeamMember(){
        //given
        User createUser = User.createUser("temp", "temp", "temp", "temp", Gender.M, LocalDate.now(), "temp",
                true, true, true, Role.USER, "kakao", "temp", null);
        User save = userRepository.save(createUser);
        clear();

        //when & then
        assertThatThrownBy(() -> scheduleQueryService.findClosestSchedule(saveRecruitment.getRecruitmentNo()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("FORBIDDEN_RECRUITMENT_TEAM");
    }

    @Test
    @DisplayName("참여 가능한 가장 가까운 일정 조회가 없어 결과가 널이 된다")
    public void notExistNearestSchedule(){
        //given
        Schedule schedule1 = 스케줄_등록(LocalDate.now(), 3);
        Schedule schedule2 = 스케줄_등록(LocalDate.now() , 3);
        Schedule schedule3 = 스케줄_등록(LocalDate.now(),3);

        //when
        Schedule closestSchedule = scheduleQueryService.findClosestSchedule(saveRecruitment.getRecruitmentNo());

        //then
        assertThat(closestSchedule).isNull();
    }

    @Test
    @DisplayName("3개의 스케줄 중 모집 중이고 삭제되지 않은 가장 가까운 schedule3 일정 조회에 성공한다.")
    public void findClosestScheduleAndNotDeleted(){
        //given
        Schedule schedule1 = 스케줄_등록(LocalDate.now().plusMonths(2), 3);
        schedule1.delete(); //스케줄 삭제
        Schedule schedule2 = 스케줄_등록(LocalDate.now().plusMonths(6) , 3);
        Schedule schedule3 = 스케줄_등록(LocalDate.now().plusMonths(4), 3);

        //when
        Schedule closestSchedule = scheduleQueryService.findClosestSchedule(saveRecruitment.getRecruitmentNo());
        clear();

        //then
        assertAll(
                () -> assertThat(closestSchedule.getContent()).isEqualTo(schedule3.getContent()),
                () -> assertThat(closestSchedule.getScheduleTimeTable().getStartDay()).isEqualTo(schedule3.getScheduleTimeTable().getStartDay()),
                () -> assertThat(closestSchedule.getScheduleTimeTable().getHourFormat()).isEqualTo(schedule3.getScheduleTimeTable().getHourFormat()),
                () -> assertThat(closestSchedule.getScheduleTimeTable().getStartTime()).isEqualTo(schedule3.getScheduleTimeTable().getStartTime()),
                () -> assertThat(closestSchedule.getVolunteerNum()).isEqualTo(schedule3.getVolunteerNum()),
                () ->  assertThat(closestSchedule.getCurrentVolunteerNum()).isEqualTo(0));
    }

}