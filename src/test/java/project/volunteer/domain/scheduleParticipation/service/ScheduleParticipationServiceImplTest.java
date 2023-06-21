package project.volunteer.domain.scheduleParticipation.service;

import org.junit.jupiter.api.BeforeEach;
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
import project.volunteer.domain.scheduleParticipation.dao.ScheduleParticipationRepository;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;
import project.volunteer.domain.sehedule.dao.ScheduleRepository;
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

@SpringBootTest
class ScheduleParticipationServiceImplTest {

    @PersistenceContext EntityManager em;
    @Autowired UserRepository userRepository;
    @Autowired RecruitmentRepository recruitmentRepository;
    @Autowired ScheduleRepository scheduleRepository;
    @Autowired ParticipantRepository participantRepository;
    @Autowired ScheduleParticipationRepository scheduleParticipationRepository;
    @Autowired ScheduleParticipationService spService;

    private User writer;
    private Recruitment saveRecruitment;
    private Schedule saveSchedule;
    @BeforeEach
    void init(){
        //작성자 저장
        User writerUser = User.createUser("1234", "1234", "1234", "1234", Gender.M, LocalDate.now(), "1234",
                true, true, true, Role.USER, "kakao", "1234", null);
        writer = userRepository.save(writerUser);

        //모집글 저장
        Recruitment createRecruitment = Recruitment.createRecruitment("title", "content", VolunteeringCategory.CULTURAL_EVENT, VolunteeringType.IRREG,
                VolunteerType.TEENAGER, 3, true, "organization",
                Address.createAddress("11", "1111","details"), Coordinate.createCoordinate(3.2F, 3.2F),
                Timetable.createTimetable(LocalDate.now(), LocalDate.now().plusMonths(3), HourFormat.AM, LocalTime.now(), 3), true);
        createRecruitment.setWriter(writer);
        saveRecruitment = recruitmentRepository.save(createRecruitment);

        //일정 저장
        Schedule createSchedule = Schedule.createSchedule(
                Timetable.createTimetable(
                        LocalDate.now().plusMonths(1), LocalDate.now().plusMonths(1),
                        HourFormat.AM, LocalTime.now(), 3),
                "content", "organization",
                Address.createAddress("11", "1111", "details"), 3);
        createSchedule.setRecruitment(saveRecruitment);
        saveSchedule = scheduleRepository.save(createSchedule);
    }

    @Test
    @Transactional
    @DisplayName("일정 첫 참가에 성공하다.")
    public void schedule_participating(){
        //given
        User newUser = 사용자_등록("구본식");
        봉사모집글_팀원_등록(saveRecruitment, newUser);

        //when
        spService.participate(saveRecruitment.getRecruitmentNo(), saveSchedule.getScheduleNo(), newUser.getUserNo());
        clear();

        //then
        ScheduleParticipation findSP = scheduleParticipationRepository.findByUserNoAndScheduleNo(newUser.getUserNo(), saveSchedule.getScheduleNo()).get();
        assertThat(findSP.getState()).isEqualTo(State.PARTICIPATING);
    }

    @Test
    @Transactional
    @DisplayName("일정 기간 종료로 인해 참가 신청에 실패하다.")
    public void schedule_period_end(){
        //given
        User newUser = 사용자_등록("구본식");
        봉사모집글_팀원_등록(saveRecruitment, newUser);

        Timetable changeTime = Timetable.createTimetable(
                LocalDate.now().minusDays(1), LocalDate.now().minusDays(1), HourFormat.AM,LocalTime.now(),3);
        saveSchedule.changeScheduleTime(changeTime);
        clear();

        //when & then
        assertThatThrownBy(() -> spService.participate(saveRecruitment.getRecruitmentNo(), saveSchedule.getScheduleNo(), newUser.getUserNo()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("NOT_EXIST_SCHEDULE");
    }

    @Test
    @Transactional
    @DisplayName("일정 모집 인원 초가로 인해 참가 신청에 실패하다.")
    public void schedule_volunteerNum_insufficient(){
        //given
        User newUser1 = 사용자_등록("구본식");
        Participant newParticipant1 = 봉사모집글_팀원_등록(saveRecruitment, newUser1);
        일정_참여자_추가(saveSchedule, newParticipant1, State.PARTICIPATING);

        User newUser2 = 사용자_등록("홍길동");
        Participant newParticipant2 = 봉사모집글_팀원_등록(saveRecruitment, newUser2);
        일정_참여자_추가(saveSchedule, newParticipant2, State.PARTICIPATING);

        User newUser3 = 사용자_등록("구하라");
        Participant newParticipant3 = 봉사모집글_팀원_등록(saveRecruitment, newUser3);
        일정_참여자_추가(saveSchedule, newParticipant3, State.PARTICIPATING);

        User newUser4 = 사용자_등록("박하영");
        봉사모집글_팀원_등록(saveRecruitment, newUser4);

        clear();

        //when & then
        assertThatThrownBy(() -> spService.participate(saveRecruitment.getRecruitmentNo(), saveSchedule.getScheduleNo(), newUser4.getUserNo()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("INSUFFICIENT_CAPACITY");
    }

    @Test
    @Transactional
    @DisplayName("일정 참가 신청을 중복하다.")
    public void schedule_participating_duplication(){
        //given
        User newUser = 사용자_등록("구본식");
        Participant newParticipant = 봉사모집글_팀원_등록(saveRecruitment, newUser);
        일정_참여자_추가(saveSchedule, newParticipant, State.PARTICIPATING);
        clear();

        //when & then
        assertThatThrownBy(() -> spService.participate(saveRecruitment.getRecruitmentNo(), saveSchedule.getScheduleNo(), newUser.getUserNo()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("DUPLICATE_PARTICIPATION");
    }

    @Test
    @Transactional
    @DisplayName("일정 재신청에 성공하다.")
    public void schedule_reParticipating(){
        //given
        User newUser = 사용자_등록("구본식");
        Participant newParticipant = 봉사모집글_팀원_등록(saveRecruitment, newUser);
        일정_참여자_추가(saveSchedule, newParticipant, State.PARTICIPATION_CANCEL);
        clear();

        //when
        spService.participate(saveRecruitment.getRecruitmentNo(), saveSchedule.getScheduleNo(), newUser.getUserNo());

        //then
        ScheduleParticipation findSP = scheduleParticipationRepository.findByUserNoAndScheduleNo(newUser.getUserNo(), saveSchedule.getScheduleNo()).get();
        assertThat(findSP.getState()).isEqualTo(State.PARTICIPATING);
    }

    @Test
    @Transactional
    @DisplayName("일정 참여 취소 요청에 성공하다.")
    public void schedule_cancelParticipation(){
        //given
        User newUser = 사용자_등록("구본식");
        Participant newParticipant = 봉사모집글_팀원_등록(saveRecruitment, newUser);
        일정_참여자_추가(saveSchedule, newParticipant, State.PARTICIPATING);
        clear();

        //when
        spService.cancelRequest(saveSchedule.getScheduleNo(), newUser.getUserNo());

        //then
        ScheduleParticipation findSp = scheduleParticipationRepository.findByUserNoAndScheduleNo(newUser.getUserNo(), saveSchedule.getScheduleNo()).get();
        assertThat(findSp.getState()).isEqualTo(State.PARTICIPATION_CANCEL);
    }

    @Test
    @Transactional
    @DisplayName("유효한 상태가 아니므로 일정 참여 취소 요청에 실패하다.")
    public void schedule_cancelParticipation_invalid_state(){
        //given
        User newUser = 사용자_등록("구본식");
        Participant newParticipant = 봉사모집글_팀원_등록(saveRecruitment, newUser);
        일정_참여자_추가(saveSchedule, newParticipant, State.PARTICIPATION_CANCEL); //적절하지 않은 상태
        clear();

        //when & then
        assertThatThrownBy(() ->  spService.cancelRequest(saveSchedule.getScheduleNo(), newUser.getUserNo()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("INVALID_STATE");
    }


    private User 사용자_등록(String username){
        User createUser = User.createUser(username, username, username, username, Gender.M, LocalDate.now(), "picture",
                true, true, true, Role.USER, "kakao", username, null);
        return userRepository.save(createUser);
    }
    private Participant 봉사모집글_팀원_등록(Recruitment recruitment, User user){
        Participant participant = Participant.createParticipant(recruitment, user, State.JOIN_APPROVAL);
        return participantRepository.save(participant);
    }
    private ScheduleParticipation 일정_참여자_추가(Schedule schedule, Participant participant, State state){
        ScheduleParticipation scheduleParticipation = ScheduleParticipation.createScheduleParticipation(schedule, participant, state);
        return scheduleParticipationRepository.save(scheduleParticipation);
    }
    private void clear() {
        em.flush();
        em.clear();
    }

}