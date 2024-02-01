package project.volunteer.domain.sehedule.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.participation.dao.ParticipantRepository;
import project.volunteer.domain.participation.domain.Participant;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ScheduleServiceImplTestForQuery {

    @PersistenceContext EntityManager em;
    @Autowired UserRepository userRepository;
    @Autowired RecruitmentRepository recruitmentRepository;
    @Autowired ParticipantRepository participantRepository;
    @Autowired ScheduleRepository scheduleRepository;
    @Autowired
    ScheduleCommandUseCase scheduleCommandService;
    @Autowired
    ScheduleQueryUseCase scheduleQueryService;

    Recruitment saveRecruitment;
    @BeforeEach
    private void init() {
        //작성자 저장
        User writer = User.createUser("1234", "1234", "1234", "1234", Gender.M, LocalDate.now(), "1234",
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

        //봉사 팀원 저장
        for(int i=0;i<3;i++){
            User createUser = User.createUser("test" + i, "test" + i, "test" + i, "test" + i, Gender.M, LocalDate.now(), "test" + i,
                    true, true, true, Role.USER, "kakao", "test" + i, null);
            User saveUser = userRepository.save(createUser);

            Participant createParticipant = Participant.createParticipant(saveRecruitment, saveUser, ParticipantState.JOIN_APPROVAL);
            participantRepository.save(createParticipant);
        }

        clear();
    }
    private Schedule 스케줄_등록(LocalDate day, int volunteerNum){
        Timetable timetable = Timetable.createTimetable(day, day, HourFormat.AM, LocalTime.now(), 10);
        Address address = Address.createAddress("1", "111", "test", "fullName");

        Schedule schedule = Schedule.create(saveRecruitment, timetable, "test" ,"test", address, volunteerNum);
        return scheduleRepository.save(schedule);
    }
    private void clear() {
        em.flush();
        em.clear();
    }

    @Disabled
    @Test
    @DisplayName("팀원이 아닌 사용자가 캘린더 스케줄 조회를 시도하다.")
    @Transactional
    public void forbidden(){
        assertThatThrownBy(() -> scheduleQueryService.searchScheduleCalender(
                saveRecruitment,
                LocalDate.of(2023, 5, 1), LocalDate.of(2023, 5, 31)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("FORBIDDEN_RECRUITMENT_TEAM");
    }

}