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
import project.volunteer.global.error.exception.ErrorCode;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
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
                Address.createAddress("11", "1111","details", "fullName"), Coordinate.createCoordinate(3.2F, 3.2F),
                Timetable.createTimetable(LocalDate.now(), LocalDate.now().plusMonths(3), HourFormat.AM, LocalTime.now(), 3), true);
        createRecruitment.setWriter(writer);
        saveRecruitment = recruitmentRepository.save(createRecruitment);

        //일정 저장
        Schedule createSchedule = Schedule.createSchedule(
                Timetable.createTimetable(
                        LocalDate.now().plusMonths(1), LocalDate.now().plusMonths(1),
                        HourFormat.AM, LocalTime.now(), 3),
                "content", "organization",
                Address.createAddress("11", "1111", "details", "fullName"), 3);
        createSchedule.setRecruitment(saveRecruitment);
        saveSchedule = scheduleRepository.save(createSchedule);
    }

    @Test
    @DisplayName("일정 첫 참가에 성공하다.")
    public void schedule_participating(){
        //given
        User newUser = 사용자_등록("kubonsik");
        Participant participant = 봉사모집글_팀원_등록(saveRecruitment, newUser);

        //when
        spService.participate(saveSchedule, participant);
        clear();

        //then
        ScheduleParticipation findSP = scheduleParticipationRepository.findByUserNoAndScheduleNo(newUser.getUserNo(), saveSchedule.getScheduleNo()).get();
        assertThat(findSP.getState()).isEqualTo(ParticipantState.PARTICIPATING);
        assertThat(saveSchedule.getCurrentVolunteerNum()).isEqualTo(1);
    }

    //TODO: 일정 서비스 테스트로 이동해야할 테스트
//    @Test
//    @DisplayName("일정 기간 종료로 인해 참가 신청에 실패하다.")
//    public void schedule_period_end(){
//        //given
//        User newUser = 사용자_등록("kubonsik");
//        봉사모집글_팀원_등록(saveRecruitment, newUser);
//
//        Timetable changeTime = Timetable.createTimetable(
//                LocalDate.now().minusDays(1), LocalDate.now().minusDays(1), HourFormat.AM,LocalTime.now(),3);
//        saveSchedule.changeScheduleTime(changeTime);
//        clear();
//
//        //when & then
//        assertThatThrownBy(() -> spService.participate(saveRecruitment.getRecruitmentNo(), saveSchedule.getScheduleNo(), newUser.getUserNo()))
//                .isInstanceOf(BusinessException.class)
//                .hasMessageContaining(ErrorCode.EXPIRED_PERIOD_SCHEDULE.name());
//    }

    @Test
    @DisplayName("일정 모집 인원 초가로 인해 참가 신청에 실패하다.")
    public void schedule_volunteerNum_insufficient(){
        //given
        User newUser1 = 사용자_등록("kubonsik");
        Participant newParticipant1 = 봉사모집글_팀원_등록(saveRecruitment, newUser1);
        일정_참여자_상태_추가(saveSchedule, newParticipant1, ParticipantState.PARTICIPATING);
        saveSchedule.increaseParticipant();

        User newUser2 = 사용자_등록("honggildong");
        Participant newParticipant2 = 봉사모집글_팀원_등록(saveRecruitment, newUser2);
        일정_참여자_상태_추가(saveSchedule, newParticipant2, ParticipantState.PARTICIPATING);
        saveSchedule.increaseParticipant();

        User newUser3 = 사용자_등록("kuhara");
        Participant newParticipant3 = 봉사모집글_팀원_등록(saveRecruitment, newUser3);
        일정_참여자_상태_추가(saveSchedule, newParticipant3, ParticipantState.PARTICIPATING);
        saveSchedule.increaseParticipant();

        User newUser4 = 사용자_등록("parkhayoung");
        Participant participant = 봉사모집글_팀원_등록(saveRecruitment, newUser4);
        clear();

        //when & then
        assertThatThrownBy(() -> spService.participate(saveSchedule, participant))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("INSUFFICIENT_CAPACITY");
    }

    @Test
    @DisplayName("일정 참가 신청을 중복하다.")
    public void schedule_participating_duplication(){
        //given
        User newUser = 사용자_등록("kubonsik");
        Participant newParticipant = 봉사모집글_팀원_등록(saveRecruitment, newUser);
        일정_참여자_상태_추가(saveSchedule, newParticipant, ParticipantState.PARTICIPATING);
        saveSchedule.increaseParticipant();
        clear();

        //when & then
        assertThatThrownBy(() -> spService.participate(saveSchedule, newParticipant))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("DUPLICATE_PARTICIPATION");
    }

    @Test
    @DisplayName("일정 재신청에 성공하다.")
    public void schedule_reParticipating(){
        //given
        User newUser = 사용자_등록("kubonsik");
        Participant newParticipant = 봉사모집글_팀원_등록(saveRecruitment, newUser);
        일정_참여자_상태_추가(saveSchedule, newParticipant, ParticipantState.PARTICIPATION_CANCEL_APPROVAL);
        clear();

        //when
        spService.participate(saveSchedule, newParticipant);

        //then
        ScheduleParticipation findSP = scheduleParticipationRepository.findByUserNoAndScheduleNo(newUser.getUserNo(), saveSchedule.getScheduleNo()).get();
        assertThat(findSP.getState()).isEqualTo(ParticipantState.PARTICIPATING);
    }

    @Test
    @DisplayName("일정 참여 취소 요청에 성공하다.")
    public void schedule_cancelParticipation(){
        //given
        User newUser = 사용자_등록("kubonsik");
        Participant newParticipant = 봉사모집글_팀원_등록(saveRecruitment, newUser);
        일정_참여자_상태_추가(saveSchedule, newParticipant, ParticipantState.PARTICIPATING);
        saveSchedule.increaseParticipant();
        clear();

        //when
        spService.cancel(saveSchedule, newParticipant);

        //then
        ScheduleParticipation findSp = scheduleParticipationRepository.findByUserNoAndScheduleNo(newUser.getUserNo(), saveSchedule.getScheduleNo()).get();
        assertThat(findSp.getState()).isEqualTo(ParticipantState.PARTICIPATION_CANCEL);
    }

    @Test
    @Transactional
    @DisplayName("유효한 상태가 아니므로 일정 참여 취소 요청에 실패하다.")
    public void schedule_cancelParticipation_invalid_state(){
        //given
        User newUser = 사용자_등록("kubonsik");
        Participant newParticipant = 봉사모집글_팀원_등록(saveRecruitment, newUser);
        일정_참여자_상태_추가(saveSchedule, newParticipant, ParticipantState.PARTICIPATION_CANCEL); //적절하지 않은 상태
        clear();

        //when & then
        assertThatThrownBy(() ->  spService.cancel(saveSchedule, newParticipant))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("INVALID_STATE");
    }

    @Test
    @DisplayName("일정 참여 취소 요청 승인에 성공하다.")
    public void schedule_cancelApprove(){
        //given
        User newUser = 사용자_등록("kubonsik");
        Participant newParticipant = 봉사모집글_팀원_등록(saveRecruitment, newUser);
        saveSchedule.increaseParticipant();
        ScheduleParticipation newSp = 일정_참여자_상태_추가(saveSchedule, newParticipant, ParticipantState.PARTICIPATION_CANCEL);

        //when
        spService.approvalCancellation(saveSchedule, newSp.getScheduleParticipationNo());
        clear();

        //then
        ScheduleParticipation findSp = scheduleParticipationRepository.findById(newSp.getScheduleParticipationNo()).get();
        assertThat(findSp.getState()).isEqualTo(ParticipantState.PARTICIPATION_CANCEL_APPROVAL);
        assertThat(saveSchedule.getCurrentVolunteerNum()).isEqualTo(0);
    }

    @Test
    @Transactional
    @DisplayName("일정 참여 완료 승인에 성공하다.")
    public void schedule_completeApprove(){
        //given
        User newUser1 = 사용자_등록("kubonsik");
        Participant newParticipant1 = 봉사모집글_팀원_등록(saveRecruitment, newUser1);
        ScheduleParticipation newSp1 = 일정_참여자_상태_추가(saveSchedule, newParticipant1, ParticipantState.PARTICIPATION_COMPLETE_UNAPPROVED);

        User newUser2 = 사용자_등록("yangsoeun");
        Participant newParticipant2 = 봉사모집글_팀원_등록(saveRecruitment, newUser2);
        ScheduleParticipation newSp2 = 일정_참여자_상태_추가(saveSchedule, newParticipant2, ParticipantState.PARTICIPATION_COMPLETE_UNAPPROVED);

        List<Long> spNos = List.of(newSp1.getScheduleParticipationNo(), newSp2.getScheduleParticipationNo());
        clear();

        //when
        spService.approvalCompletion(spNos);

        //then
        ScheduleParticipation findSp1 = scheduleParticipationRepository.findById(newSp1.getScheduleParticipationNo()).get();
        ScheduleParticipation findSp2 = scheduleParticipationRepository.findById(newSp2.getScheduleParticipationNo()).get();
        assertThat(findSp1.getState()).isEqualTo(ParticipantState.PARTICIPATION_COMPLETE_APPROVAL);
        assertThat(findSp2.getState()).isEqualTo(ParticipantState.PARTICIPATION_COMPLETE_APPROVAL);
    }

    @Test
    @Transactional
    @DisplayName("유효한 상태가 아니므로 일정 참여 완료 승인에 실패하다.")
    public void schedule_completeApprove_invalid_state(){
        //given
        User newUser1 = 사용자_등록("kubonsik");
        Participant newParticipant1 = 봉사모집글_팀원_등록(saveRecruitment, newUser1);
        ScheduleParticipation newSp1 = 일정_참여자_상태_추가(saveSchedule, newParticipant1, ParticipantState.PARTICIPATION_COMPLETE_UNAPPROVED);

        User newUser2 = 사용자_등록("yangsoeun");
        Participant newParticipant2 = 봉사모집글_팀원_등록(saveRecruitment, newUser2);
        ScheduleParticipation newSp2 = 일정_참여자_상태_추가(saveSchedule, newParticipant2, ParticipantState.PARTICIPATION_COMPLETE_APPROVAL); //유효하지 않은 상태

        List<Long> spNos = List.of(newSp1.getScheduleParticipationNo(), newSp2.getScheduleParticipationNo());
        clear();

        //when & then
        assertThatThrownBy(() -> spService.approvalCompletion(spNos))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.INVALID_STATE.name());
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