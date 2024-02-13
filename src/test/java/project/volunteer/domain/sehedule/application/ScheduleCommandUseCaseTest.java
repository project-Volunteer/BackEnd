package project.volunteer.domain.sehedule.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.amazonaws.services.kms.model.NotFoundException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.support.ServiceTest;
import project.volunteer.domain.recruitment.application.dto.command.RepeatPeriodCreateCommand;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Day;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Period;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Week;
import project.volunteer.domain.sehedule.application.dto.command.RegularScheduleCreateCommand;
import project.volunteer.domain.sehedule.application.dto.command.ScheduleUpsertCommand;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.Coordinate;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.global.common.component.Timetable;

public class ScheduleCommandUseCaseTest extends ServiceTest {
    private final User writer = new User("test", "test", "test", "test@email.com", Gender.M, LocalDate.now(),
                    "http://", true, true, true, Role.USER, "kakao", "1234", null);
    private final Address address = new Address("111", "11", "test", "test");
    private final Coordinate coordinate = new Coordinate(1.2F, 2.2F);
    private final Timetable timetable = new Timetable(LocalDate.now(), LocalDate.now(), HourFormat.AM, LocalTime.now(),
            10);
    private final Recruitment recruitment = Recruitment.builder()
            .title("title")
            .content("content")
            .volunteeringCategory(VolunteeringCategory.EDUCATION)
            .volunteerType(VolunteerType.ADULT)
            .volunteeringType(VolunteeringType.IRREG)
            .maxParticipationNum(999)
            .currentVolunteerNum(0)
            .isIssued(true)
            .organizationName("organization")
            .address(address)
            .coordinate(coordinate)
            .timetable(timetable)
            .viewCount(0)
            .likeCount(0)
            .isPublished(true)
            .isDeleted(IsDeleted.N)
            .writer(writer)
            .build();

    @BeforeEach
    void setUp() {
        userRepository.save(writer);
        recruitmentRepository.save(recruitment);
    }

    @DisplayName("일정을 생성하고 저장한다.")
    @Test
    void addSchedule() {
        //given
        final ScheduleUpsertCommand command = createCommand("unicef", "조심히", 10);

        //when
        Long scheduleNo = scheduleCommandUseCase.addSchedule(recruitment, command);

        //then
        assertThat(findScheduleBy(scheduleNo)).extracting("organizationName", "content", "volunteerNum")
                .containsExactly(command.getOrganizationName(), command.getContent(), command.getMaxParticipationNum());
    }

    @DisplayName("일정 정보를 수정한다.")
    @Test
    void editSchedule() {
        // given
        final Long scheduleNo = createAndSaveSchedule();
        final ScheduleUpsertCommand changeCommand = createCommand("change", "change", 10);

        // when
        scheduleCommandUseCase.editSchedule(scheduleNo, recruitment, changeCommand);

        // then
        assertThat(findScheduleBy(scheduleNo)).extracting("content", "organizationName", "volunteerNum")
                .containsExactlyInAnyOrder(changeCommand.getContent(), changeCommand.getOrganizationName(),
                        changeCommand.getMaxParticipationNum());
    }

    @DisplayName("매주 토요일, 일요일 반복하는 일정을 생성하고 저장한다.")
    @Test
    void addWeeklySchedule() {
        //given
        final RegularScheduleCreateCommand command = createCommand(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 29),
                Period.WEEK, null, List.of(Day.SAT, Day.SUN));

        //when
        scheduleCommandUseCase.addRegularSchedule(recruitment, command);

        //then
        assertThat(findScheduleByRecruitmentNo(recruitment.getRecruitmentNo())).hasSize(16);
    }

    @DisplayName("매달 셋째주 토요일, 일요일 반복하는 일정을 생성하고 저장한다.")
    @Test
    void addMonthlySchedule() {
        //given
        final RegularScheduleCreateCommand command = createCommand(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 29),
                Period.MONTH, Week.THIRD, List.of(Day.SAT, Day.SUN));

        //when
        scheduleCommandUseCase.addRegularSchedule(recruitment, command);

        //then
        assertThat(findScheduleByRecruitmentNo(recruitment.getRecruitmentNo())).hasSize(6);
    }

    @DisplayName("일정 삭제 시, 삭제 플레그 값을 업데이트한다.")
    @Test
    void deleteSchedule() {
        //given
        final Long schedule = createAndSaveSchedule();

        //when
        scheduleCommandUseCase.deleteSchedule(schedule);

        //then
        assertThat(findScheduleBy(schedule).getIsDeleted()).isEqualByComparingTo(IsDeleted.Y);
    }

    @DisplayName("모집글에 존재하는 일정들을 모두 삭제한다.")
    @Test
    void deleteAllScheduleExistInRecruitment() {
        //given
        List<Long> scheduleNos = List.of(createAndSaveSchedule(), createAndSaveSchedule(), createAndSaveSchedule());

        //when
        scheduleCommandUseCase.deleteAllSchedule(recruitment.getRecruitmentNo());

        //then
        assertThat(findScheduleByRecruitmentNo(recruitment.getRecruitmentNo())).hasSize(3)
                .extracting("isDeleted")
                .containsExactly(IsDeleted.Y, IsDeleted.Y, IsDeleted.Y);
    }

    private ScheduleUpsertCommand createCommand(String organizationName, String content, int participationNum) {
        return new ScheduleUpsertCommand(timetable, organizationName, address, content, participationNum);
    }

    private RegularScheduleCreateCommand createCommand(LocalDate recruitmentStartDate, LocalDate recruitmentEndDate,
                                                       Period period, Week week, List<Day> dayOfWeeks) {
        Timetable timetable = new Timetable(recruitmentStartDate, recruitmentEndDate, HourFormat.PM, LocalTime.now(),
                10);
        RepeatPeriodCreateCommand repeatPeriod = new RepeatPeriodCreateCommand(period, week, dayOfWeeks);
        return new RegularScheduleCreateCommand(timetable, repeatPeriod, "test", address, "test", 10);
    }

    private Long createAndSaveSchedule() {
        Schedule schedule = Schedule.builder()
                .timetable(timetable)
                .content("test")
                .organizationName("test")
                .address(address)
                .participationNum(10)
                .isDeleted(IsDeleted.N)
                .currentVolunteerNum(8)
                .recruitment(recruitment)
                .build();
        return scheduleRepository.save(schedule).getScheduleNo();
    }

    private Schedule findScheduleBy(Long scheduleNo) {
        return scheduleRepository.findById(scheduleNo)
                .orElseThrow(() -> new NotFoundException("일정이 존재하지 않습니다."));
    }

    private List<Schedule> findScheduleByRecruitmentNo(Long recruitmentNo) {
        return scheduleRepository.findByRecruitment_RecruitmentNo(recruitmentNo);
    }

}
