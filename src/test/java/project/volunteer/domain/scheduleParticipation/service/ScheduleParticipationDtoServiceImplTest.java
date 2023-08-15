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
import project.volunteer.domain.scheduleParticipation.service.dto.CancelledParticipantList;
import project.volunteer.domain.scheduleParticipation.service.dto.CompletedParticipantList;
import project.volunteer.domain.scheduleParticipation.service.dto.ParticipatingParticipantList;
import project.volunteer.domain.sehedule.dao.ScheduleRepository;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.*;
import project.volunteer.global.common.dto.StateResponse;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class ScheduleParticipationDtoServiceImplTest {

    @PersistenceContext
    EntityManager em;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RecruitmentRepository recruitmentRepository;
    @Autowired
    ScheduleRepository scheduleRepository;
    @Autowired
    ParticipantRepository participantRepository;
    @Autowired
    ScheduleParticipationRepository scheduleParticipationRepository;
    @Autowired ScheduleParticipationDtoService spDtoService;

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
    @DisplayName("일정 참여 중 리스트 조회 시 3명의 참여자가 조회된다.")
    public void schedule_participant_list(){
        //given
        User newUser1 = 사용자_등록("rnqhstlr");
        Participant newParticipant1 = 봉사모집글_팀원_등록(saveRecruitment, newUser1);
        일정_참여자_상태_추가(saveSchedule, newParticipant1, ParticipantState.PARTICIPATING);

        User newUser2 = 사용자_등록("didthdms");
        Participant newParticipant2 = 봉사모집글_팀원_등록(saveRecruitment, newUser2);
        일정_참여자_상태_추가(saveSchedule, newParticipant2, ParticipantState.PARTICIPATING);

        User newUser3 = 사용자_등록("didghfhr");
        Participant newParticipant3 = 봉사모집글_팀원_등록(saveRecruitment, newUser3);
        일정_참여자_상태_추가(saveSchedule, newParticipant3, ParticipantState.PARTICIPATING);
        clear();

        //when
        List<ParticipatingParticipantList> findParticipants = spDtoService.findParticipatingParticipants(saveSchedule.getScheduleNo());

        //then
        assertThat(findParticipants.size()).isEqualTo(3);
        for(ParticipatingParticipantList p : findParticipants){
            assertThat(p.getProfile()).isEqualTo("picture");
        }
    }

    @Test
    @Transactional
    @DisplayName("일정 취소 요청 리스트 조회 시 2명의 취소 요청자가 조회된다.")
    public void schedule_cancel_participant_list(){
        //given
        User newUser1 = 사용자_등록("rnqhstlr");
        Participant newParticipant1 = 봉사모집글_팀원_등록(saveRecruitment, newUser1);
        일정_참여자_상태_추가(saveSchedule, newParticipant1, ParticipantState.PARTICIPATING);

        User newUser2 = 사용자_등록("didthdms");
        Participant newParticipant2 = 봉사모집글_팀원_등록(saveRecruitment, newUser2);
        일정_참여자_상태_추가(saveSchedule, newParticipant2, ParticipantState.PARTICIPATION_CANCEL);

        User newUser3 = 사용자_등록("didghfhr");
        Participant newParticipant3 = 봉사모집글_팀원_등록(saveRecruitment, newUser3);
        일정_참여자_상태_추가(saveSchedule, newParticipant3, ParticipantState.PARTICIPATION_CANCEL);
        clear();

        //when
        List<CancelledParticipantList> findCancellingParticipants = spDtoService.findCancelledParticipants(saveSchedule.getScheduleNo());


        //then
        assertThat(findCancellingParticipants.size()).isEqualTo(2);
        for(CancelledParticipantList p : findCancellingParticipants){
            assertThat(p.getProfile()).isEqualTo("picture");
        }
    }

    @Test
    @Transactional
    @DisplayName("일정 참여 완료 리스트 조회 시 2명의 참여 완료 미승인과 1명의 참여 완료 승인자가 조회된다.")
    public void schedule_completed_participant_list(){
        //given
        User newUser1 = 사용자_등록("ku");
        Participant newParticipant1 = 봉사모집글_팀원_등록(saveRecruitment, newUser1);
        일정_참여자_상태_추가(saveSchedule, newParticipant1, ParticipantState.PARTICIPATION_COMPLETE_UNAPPROVED);

        User newUser2 = 사용자_등록("yangsoeun");
        Participant newParticipant2 = 봉사모집글_팀원_등록(saveRecruitment, newUser2);
        일정_참여자_상태_추가(saveSchedule, newParticipant2, ParticipantState.PARTICIPATION_COMPLETE_UNAPPROVED);

        User newUser3 = 사용자_등록("yanghorok");
        Participant newParticipant3 = 봉사모집글_팀원_등록(saveRecruitment, newUser3);
        일정_참여자_상태_추가(saveSchedule, newParticipant3, ParticipantState.PARTICIPATION_COMPLETE_APPROVAL);
        clear();

        //when
        List<CompletedParticipantList> findCompletedParticipants = spDtoService.findCompletedParticipants(saveSchedule.getScheduleNo());

        //then
        assertThat(findCompletedParticipants.size()).isEqualTo(3);
        for(CompletedParticipantList p : findCompletedParticipants){
            assertThat(p.getProfile()).isEqualTo("picture");
            assertThat(p.getStatus()).isIn(StateResponse.COMPLETE_APPROVED.name(), StateResponse.COMPLETE_UNAPPROVED.name());
        }
    }

    private User 사용자_등록(String username){
        User createUser = User.createUser(username, username, username, username, Gender.M, LocalDate.now(), "picture",
                true, true, true, Role.USER, "kakao", username, null);
        return userRepository.save(createUser);
    }
    private Participant 봉사모집글_팀원_등록(Recruitment recruitment, User user){
        Participant participant = Participant.createParticipant(recruitment, user, ParticipantState.JOIN_APPROVAL);
        return participantRepository.save(participant);
    }
    private ScheduleParticipation 일정_참여자_상태_추가(Schedule schedule, Participant participant, ParticipantState state){
        ScheduleParticipation scheduleParticipation = ScheduleParticipation.createScheduleParticipation(schedule, participant, state);
        return scheduleParticipationRepository.save(scheduleParticipation);
    }
    private void clear() {
        em.flush();
        em.clear();
    }
}