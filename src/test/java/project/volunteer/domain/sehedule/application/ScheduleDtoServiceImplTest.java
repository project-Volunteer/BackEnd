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
import project.volunteer.domain.scheduleParticipation.dao.ScheduleParticipationRepository;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;
import project.volunteer.domain.sehedule.application.dto.ScheduleDetails;
import project.volunteer.domain.sehedule.dao.ScheduleRepository;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.*;
import project.volunteer.global.common.response.StateResponse;
import project.volunteer.global.error.exception.BusinessException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
class ScheduleDtoServiceImplTest {

    @PersistenceContext EntityManager em;
    @Autowired UserRepository userRepository;
    @Autowired RecruitmentRepository recruitmentRepository;
    @Autowired ScheduleRepository scheduleRepository;
    @Autowired ScheduleDtoService scheduleDtoService;
    @Autowired ParticipantRepository participantRepository;
    @Autowired ScheduleParticipationRepository scheduleParticipationRepository;

    User writer;
    Recruitment saveRecruitment;
    List<Participant> teamMembers = new ArrayList<>();

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
        Address address = Address.createAddress("1", "111", "test");
        Timetable timetable = Timetable.createTimetable(LocalDate.now(), LocalDate.now(), HourFormat.AM, LocalTime.now(), 10);
        Coordinate coordinate = Coordinate.createCoordinate(3.2F, 3.2F);

        //봉사 모집글 저장
        saveRecruitment =
                Recruitment.createRecruitment("test", "test", VolunteeringCategory.EDUCATION, VolunteeringType.IRREG,
                        VolunteerType.TEENAGER, 10, true, "test", address, coordinate, timetable, true);
        saveRecruitment.setWriter(writer);
        recruitmentRepository.save(saveRecruitment);

        //봉사 팀원 저장
        for(int i=0;i<5;i++){
            User createUser = User.createUser("test" + i, "test" + i, "test" + i, "test" + i, Gender.M, LocalDate.now(), "test" + i,
                    true, true, true, Role.USER, "kakao", "test" + i, null);
            User saveUser = userRepository.save(createUser);

            Participant createParticipant = Participant.createParticipant(saveRecruitment, saveUser, ParticipantState.JOIN_APPROVAL);
            Participant save = participantRepository.save(createParticipant);
            teamMembers.add(save);
        }
    }

    private Schedule 스케줄_등록(LocalDate startDay, int volunteerNum){
        Timetable timetable = Timetable.createTimetable(startDay, startDay, HourFormat.AM, LocalTime.now(), 10);
        Address address = Address.createAddress("1", "111", "test");

        Schedule schedule = Schedule.createSchedule(timetable, "test" ,"test", address, volunteerNum);
        schedule.setRecruitment(saveRecruitment);
        return scheduleRepository.save(schedule);
    }
    private ScheduleParticipation 스케줄_참여자_등록(Schedule schedule, Participant participant, ParticipantState state){
        ScheduleParticipation sp = ScheduleParticipation.createScheduleParticipation(schedule, participant, state);
        return scheduleParticipationRepository.save(sp);
    }

    @Test
    @Transactional
    @DisplayName("3개의 스케줄 중 모집 중이며 가장 가까운 schedule3 일정 조회에 성공한다.")
    public void findClosestSchedule(){
        //given
        Schedule schedule1 = 스케줄_등록(LocalDate.now(), 3);
        Schedule schedule2 = 스케줄_등록(LocalDate.now().plusMonths(3) , 3);
        Schedule schedule3 = 스케줄_등록(LocalDate.now().plusMonths(2), 3);

        //when
        ScheduleDetails closestSchedule = scheduleDtoService.findClosestSchedule(
                saveRecruitment.getRecruitmentNo(), teamMembers.get(0).getParticipant().getUserNo());
        clear();

        //then
        assertAll(
                () -> assertThat(closestSchedule.getContent()).isEqualTo(schedule3.getContent()),
                () -> assertThat(closestSchedule.getStartDay()).isEqualTo(schedule3.getScheduleTimeTable()
                        .getStartDay().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"))),
                () -> assertThat(closestSchedule.getHourFormat()).isEqualTo(schedule3.getScheduleTimeTable().getHourFormat().getViewName()),
                () -> assertThat(closestSchedule.getStartTime()).isEqualTo(schedule3.getScheduleTimeTable()
                        .getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"))),
                () -> assertThat(closestSchedule.getVolunteerNum()).isEqualTo(schedule3.getVolunteerNum()),
                () ->  assertThat(closestSchedule.getActiveVolunteerNum()).isEqualTo(0),
                () ->  assertThat(closestSchedule.getState()).isEqualTo(StateResponse.AVAILABLE.name()));
    }

    @Test
    @Transactional
    @DisplayName("존재하지 않는 봉사 모집글 이므로 에러가 발생한다.")
    public void notExistRecruitment(){

        assertThatThrownBy(() -> scheduleDtoService.findClosestSchedule(
                    Long.MAX_VALUE, teamMembers.get(0).getParticipant().getUserNo()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("NOT_EXIST_RECRUITMENT");
    }

    @Disabled
    @Test
    @Transactional
    @DisplayName("팀원이 아니므로 일정 조회를 하지 못한다.")
    public void notVolunteerTeamMember(){
        //given
        User createUser = User.createUser("temp", "temp", "temp", "temp", Gender.M, LocalDate.now(), "temp",
                true, true, true, Role.USER, "kakao", "temp", null);
        User save = userRepository.save(createUser);
        clear();

        //when & then
        assertThatThrownBy(() -> scheduleDtoService.findClosestSchedule(saveRecruitment.getRecruitmentNo(), save.getUserNo()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("FORBIDDEN_RECRUITMENT_TEAM");
    }

    @Test
    @Transactional
    @DisplayName("참여 가능한 가장 가까운 일정 조회가 없어 결과가 널이 된다")
    public void notExistNearestSchedule(){
        //given
        Schedule schedule1 = 스케줄_등록(LocalDate.now(), 3);
        Schedule schedule2 = 스케줄_등록(LocalDate.now() , 3);
        Schedule schedule3 = 스케줄_등록(LocalDate.now(),3);

        //when
        ScheduleDetails closestSchedule = scheduleDtoService.findClosestSchedule(
                saveRecruitment.getRecruitmentNo(), teamMembers.get(0).getParticipant().getUserNo());

        //then
        assertThat(closestSchedule).isNull();
    }

    @Test
    @Transactional
    @DisplayName("일정 최대 참가자 수를 초과하여 FULL 상태가 된다.")
    public void doneState(){
        //given
        Schedule schedule1 = 스케줄_등록(LocalDate.now().plusMonths(3), 3);
        스케줄_참여자_등록(schedule1, teamMembers.get(0), ParticipantState.PARTICIPATING);
        schedule1.increaseParticipant();
        스케줄_참여자_등록(schedule1, teamMembers.get(1), ParticipantState.PARTICIPATING);
        schedule1.increaseParticipant();
        스케줄_참여자_등록(schedule1, teamMembers.get(2), ParticipantState.PARTICIPATING);
        schedule1.increaseParticipant();

        //when
        ScheduleDetails closestSchedule = scheduleDtoService.findClosestSchedule(saveRecruitment.getRecruitmentNo(),
                teamMembers.get(3).getParticipant().getUserNo());

        //then
        assertThat(closestSchedule.getState()).isEqualTo(StateResponse.FULL.name());
    }

    @Test
    @Transactional
    @DisplayName("첫 일정 참가이므로 available 상태가 된다.")
    public void availableStateByFirstParticipation(){
        //given
        Schedule schedule1 = 스케줄_등록(LocalDate.now().plusDays(1), 3);
        스케줄_참여자_등록(schedule1, teamMembers.get(0), ParticipantState.PARTICIPATING);
        schedule1.increaseParticipant();
        스케줄_참여자_등록(schedule1, teamMembers.get(1), ParticipantState.PARTICIPATING);
        schedule1.increaseParticipant();

        //when
        ScheduleDetails closestSchedule = scheduleDtoService.findClosestSchedule(saveRecruitment.getRecruitmentNo(),
                teamMembers.get(2).getParticipant().getUserNo());

        //then
        assertThat(closestSchedule.getState()).isEqualTo(StateResponse.AVAILABLE.name());
    }

    @Test
    @Transactional
    @DisplayName("최근 신청 상태가 일정 취소 승인이므로 available 상태가 된다.")
    public void availableStateByCancelApprove(){
        //given
        Schedule schedule1 = 스케줄_등록(LocalDate.now().plusMonths(3), 3);
        스케줄_참여자_등록(schedule1, teamMembers.get(0), ParticipantState.PARTICIPATING);
        schedule1.increaseParticipant();
        스케줄_참여자_등록(schedule1, teamMembers.get(1), ParticipantState.PARTICIPATING);
        schedule1.increaseParticipant();
        스케줄_참여자_등록(schedule1, teamMembers.get(2), ParticipantState.PARTICIPATION_CANCEL_APPROVAL);

        //when
        ScheduleDetails closestSchedule = scheduleDtoService.findClosestSchedule(saveRecruitment.getRecruitmentNo(),
                teamMembers.get(2).getParticipant().getUserNo());

        //then
        assertThat(closestSchedule.getState()).isEqualTo(StateResponse.AVAILABLE.name());
    }

    @Test
    @Transactional
    @DisplayName("최근 신청 상태가 일정 취소 신청이므로 cancelling 상태가 된다.")
    public void cancellingState(){
        //given
        Schedule schedule1 = 스케줄_등록(LocalDate.now().plusMonths(3), 3);
        스케줄_참여자_등록(schedule1, teamMembers.get(0), ParticipantState.PARTICIPATING);
        schedule1.increaseParticipant();
        스케줄_참여자_등록(schedule1, teamMembers.get(1), ParticipantState.PARTICIPATING);
        schedule1.increaseParticipant();
        스케줄_참여자_등록(schedule1, teamMembers.get(2), ParticipantState.PARTICIPATION_CANCEL);
        schedule1.increaseParticipant();

        //when
        ScheduleDetails closestSchedule = scheduleDtoService.findClosestSchedule(saveRecruitment.getRecruitmentNo(),
                teamMembers.get(2).getParticipant().getUserNo());

        //then
        assertThat(closestSchedule.getState()).isEqualTo(StateResponse.CANCELLING.name());
    }

    @Test
    @DisplayName("최근 신청 상태가 참여 승인 이므로 participating 상태가 된다.")
    @Transactional
    public void participatingState(){
        //given
        Schedule schedule1 = 스케줄_등록(LocalDate.now().plusMonths(3), 3);
        스케줄_참여자_등록(schedule1, teamMembers.get(0), ParticipantState.PARTICIPATING);
        schedule1.increaseParticipant();
        스케줄_참여자_등록(schedule1, teamMembers.get(1), ParticipantState.PARTICIPATING);
        schedule1.increaseParticipant();
        스케줄_참여자_등록(schedule1, teamMembers.get(2), ParticipantState.PARTICIPATING);
        schedule1.increaseParticipant();

        //when
        ScheduleDetails closestSchedule = scheduleDtoService.findClosestSchedule(saveRecruitment.getRecruitmentNo(),
                teamMembers.get(2).getParticipant().getUserNo());

        //then
        assertThat(closestSchedule.getState()).isEqualTo(StateResponse.PARTICIPATING.name());
    }

    @Test
    @Transactional
    @DisplayName("3개의 스케줄 중 모집 중이고 삭제되지 않은 가장 가까운 schedule3 일정 조회에 성공한다.")
    public void findClosestScheduleAndNotDeleted(){
        //given
        Schedule schedule1 = 스케줄_등록(LocalDate.now().plusMonths(2), 3);
        schedule1.delete(); //스케줄 삭제
        Schedule schedule2 = 스케줄_등록(LocalDate.now().plusMonths(6) , 3);
        Schedule schedule3 = 스케줄_등록(LocalDate.now().plusMonths(4), 3);

        //when
        ScheduleDetails closestSchedule = scheduleDtoService.findClosestSchedule(
                saveRecruitment.getRecruitmentNo(), teamMembers.get(0).getParticipant().getUserNo());
        clear();

        //then
        assertAll(
                () -> assertThat(closestSchedule.getContent()).isEqualTo(schedule3.getContent()),
                () -> assertThat(closestSchedule.getStartDay()).isEqualTo(schedule3.getScheduleTimeTable()
                        .getStartDay().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"))),
                () -> assertThat(closestSchedule.getHourFormat()).isEqualTo(schedule3.getScheduleTimeTable().getHourFormat().getViewName()),
                () -> assertThat(closestSchedule.getStartTime()).isEqualTo(schedule3.getScheduleTimeTable()
                        .getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"))),
                () -> assertThat(closestSchedule.getVolunteerNum()).isEqualTo(schedule3.getVolunteerNum()),
                () ->  assertThat(closestSchedule.getActiveVolunteerNum()).isEqualTo(0),
                () ->  assertThat(closestSchedule.getState()).isEqualTo(StateResponse.AVAILABLE.name()));
    }

    @Test
    @Transactional
    @DisplayName("캘린더를 통한 스케줄 상세 조회에 성공하다.")
    public void findCalendarSchedule(){
        //given
        Schedule schedule = 스케줄_등록(LocalDate.now().plusDays(2), 3);

        //when
        ScheduleDetails scheduleDetails = scheduleDtoService.findCalendarSchedule(
                saveRecruitment.getRecruitmentNo(), schedule.getScheduleNo(), teamMembers.get(0).getParticipant().getUserNo());

        //then
        assertAll(
                () -> assertThat(scheduleDetails.getContent()).isEqualTo(schedule.getContent()),
                () -> assertThat(scheduleDetails.getStartDay()).isEqualTo(schedule.getScheduleTimeTable()
                        .getStartDay().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"))),
                () -> assertThat(scheduleDetails.getHourFormat()).isEqualTo(schedule.getScheduleTimeTable().getHourFormat().getViewName()),
                () -> assertThat(scheduleDetails.getStartTime()).isEqualTo(schedule.getScheduleTimeTable()
                        .getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"))),
                () -> assertThat(scheduleDetails.getVolunteerNum()).isEqualTo(schedule.getVolunteerNum()),
                () ->  assertThat(scheduleDetails.getActiveVolunteerNum()).isEqualTo(0),
                () ->  assertThat(scheduleDetails.getState()).isEqualTo(StateResponse.AVAILABLE.name()));
    }

    @Test
    @Transactional
    @DisplayName("스케줄 삭제로 인해 캘린 스케줄 상세 조회에 실패하다.")
    public void deletedSchedule(){
        //given
        Schedule schedule = 스케줄_등록(LocalDate.now(), 3);
        schedule.delete();
        clear();

        //when & then
        assertThatThrownBy(() -> scheduleDtoService.findCalendarSchedule(
                saveRecruitment.getRecruitmentNo(), schedule.getScheduleNo(), teamMembers.get(0).getParticipant().getUserNo()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("NOT_EXIST_SCHEDULE");
    }
}