package project.volunteer.domain.sehedule.application;

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
import project.volunteer.domain.sehedule.application.dto.ScheduleParam;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;


@SpringBootTest
@Transactional
class ScheduleServiceImplTestForEdit {

    @PersistenceContext EntityManager em;
    @Autowired UserRepository userRepository;
    @Autowired RecruitmentRepository recruitmentRepository;
    @Autowired ScheduleRepository scheduleRepository;
    @Autowired ScheduleService scheduleService;
    @Autowired ParticipantRepository participantRepository;
    @Autowired ScheduleParticipationRepository scheduleParticipationRepository;

    User writer;
    Recruitment saveRecruitment;
    Schedule saveSchedule;
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
        Address address = Address.createAddress("1", "111", "test");
        Timetable timetable = Timetable.createTimetable(LocalDate.now(), LocalDate.now(), HourFormat.AM, LocalTime.now(), 10);
        Coordinate coordinate = Coordinate.createCoordinate(3.2F, 3.2F);

        //봉사 모집글 저장
        saveRecruitment =
                Recruitment.createRecruitment("test", "test", VolunteeringCategory.EDUCATION, VolunteeringType.IRREG,
                        VolunteerType.TEENAGER, 10, true, "test", address, coordinate, timetable, true);
        saveRecruitment.setWriter(writer);
        recruitmentRepository.save(saveRecruitment);

        //일정 등록
        saveSchedule = Schedule.createSchedule(timetable, "test", "organizaion", address, 8);
        saveSchedule.setRecruitment(saveRecruitment);
        scheduleRepository.save(saveSchedule);

        //봉사 팀원 및 일정 참여자 등록
        for(int i=0; i<5;i++){
            User user = User.createUser("test" + i, "test" + i, "test" + i, "test" + i, Gender.M, LocalDate.now(),
                    "test" + i, true, true, true, Role.USER, "kakao", "test" + i, null);
            userRepository.save(user);

            Participant participant = Participant.createParticipant(saveRecruitment, user, ParticipantState.JOIN_APPROVAL);
            participantRepository.save(participant);

            ScheduleParticipation scheduleParticipation =
                    ScheduleParticipation.createScheduleParticipation(saveSchedule, participant, ParticipantState.PARTICIPATING);
            scheduleParticipationRepository.save(scheduleParticipation);
            saveSchedule.increaseParticipant();
        }
    }

    @Test
    @DisplayName("봉사 일정 수정에 성공한다.")
    public void editSchedule(){
        //given
        final Timetable timetable = Timetable.createTimetable(LocalDate.now(), LocalDate.now(), HourFormat.AM, LocalTime.now(),3);
        final Address address = Address.createAddress("1", "111", "details");
        final String organizationName = "test";
        final String content = "test";
        final int volunteerNum = 6;
        ScheduleParam param = new ScheduleParam(timetable, organizationName, address, content, volunteerNum);

        //when
        Schedule schedule = scheduleService.editSchedule(saveSchedule.getScheduleNo(), saveRecruitment, param);
        clear();

        //then
        Schedule findSchedule = scheduleRepository.findById(schedule.getScheduleNo()).get();
        assertAll(
                () -> assertThat(findSchedule.getScheduleTimeTable().getProgressTime()).isEqualTo(timetable.getProgressTime()),
                () -> assertThat(findSchedule.getScheduleTimeTable().getStartDay()).isEqualTo(timetable.getStartDay()),
                () -> assertThat(findSchedule.getScheduleTimeTable().getEndDay()).isEqualTo(timetable.getEndDay()),
                () -> assertThat(findSchedule.getScheduleTimeTable().getHourFormat()).isEqualTo(timetable.getHourFormat()),
                () -> assertThat(findSchedule.getScheduleTimeTable().getStartTime().getHour()).isEqualTo(timetable.getStartTime().getHour()),
                () -> assertThat(findSchedule.getScheduleTimeTable().getStartTime().getMinute()).isEqualTo(timetable.getStartTime().getMinute()),
                () -> assertThat(findSchedule.getOrganizationName()).isEqualTo(organizationName),
                () -> assertThat(findSchedule.getContent()).isEqualTo(content),
                () -> assertThat(findSchedule.getVolunteerNum()).isEqualTo(volunteerNum),
                () -> assertThat(findSchedule.getAddress().getSido()).isEqualTo(address.getSido()),
                () -> assertThat(findSchedule.getAddress().getSigungu()).isEqualTo(address.getSigungu()),
                () -> assertThat(findSchedule.getAddress().getDetails()).isEqualTo(address.getDetails())
        );
    }

    @Test
    @DisplayName("유효하지않은 일정을 수정하고자 하여 에러가 발생한다.")
    public void notExistSchedule() {
        //given
        final Timetable timetable = Timetable.createTimetable(LocalDate.now(), LocalDate.now(), HourFormat.AM, LocalTime.now(),3);
        final Address address = Address.createAddress("1", "111", "details");
        final String organizationName = "test";
        final String content = "test";
        final int volunteerNum = 3;
        ScheduleParam param = new ScheduleParam(timetable, organizationName, address, content, volunteerNum);

        //when && then
        assertThatThrownBy(() -> scheduleService.editSchedule(Long.MAX_VALUE,saveRecruitment,  param))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("NOT_EXIST_SCHEDULE");
    }

    @Test
    @DisplayName("현재 일정에 참가중인 인원수보다 작은 인원수로 수정하여 오류가 발생한다.")
    public void insufficientScheduleVolunteerNum(){
        //given
        final Timetable timetable = Timetable.createTimetable(LocalDate.now(), LocalDate.now(), HourFormat.AM, LocalTime.now(),3);
        final Address address = Address.createAddress("1", "111", "details");
        final String organizationName = "test";
        final String content = "test";
        final int volunteerNum = 4; // 현재 일정에 참가중인 인원수는 5명!
        ScheduleParam param = new ScheduleParam(timetable, organizationName, address, content, volunteerNum);

        //when && then
        assertThatThrownBy(() -> scheduleService.editSchedule(saveSchedule.getScheduleNo(),saveRecruitment, param))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("INSUFFICIENT_CAPACITY_PARTICIPANT");
    }

    @Test
    @DisplayName("봉사 일정 삭제에 성공하다.")
    public void deleteSchedule(){
        //given & when
        scheduleService.deleteSchedule(saveSchedule.getScheduleNo());
        clear();

        //then
        Schedule findSchedule = scheduleRepository.findById(saveSchedule.getScheduleNo()).get();
        assertThat(findSchedule.getIsDeleted()).isEqualTo(IsDeleted.Y);
    }


}