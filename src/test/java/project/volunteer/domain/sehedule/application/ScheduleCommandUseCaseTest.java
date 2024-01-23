package project.volunteer.domain.sehedule.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.amazonaws.services.kms.model.NotFoundException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import project.volunteer.common.ServiceTest;
import project.volunteer.domain.recruitment.application.dto.RepeatPeriodCommand;
import project.volunteer.domain.recruitment.domain.Day;
import project.volunteer.domain.recruitment.domain.Period;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.domain.Week;
import project.volunteer.domain.sehedule.application.dto.RegularScheduleCreateCommand;
import project.volunteer.domain.sehedule.application.dto.ScheduleUpsertCommand;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.Coordinate;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.global.common.component.Timetable;

public class ScheduleCommandUseCaseTest extends ServiceTest {
    private final Address address = new Address("111", "11", "test", "test");
    private final Coordinate coordinate = new Coordinate(1.2F, 2.2F);
    private final Timetable timetable = new Timetable(LocalDate.now(), LocalDate.now(), HourFormat.AM, LocalTime.now(),
            10);
    private final Recruitment recruitment = Recruitment.builder()
            .title("title")
            .content("content")
            .volunteeringCategory(VolunteeringCategory.EDUCATION)
            .volunteeringType(VolunteeringType.REG)
            .volunteerType(VolunteerType.ADULT)
            .participationNum(999)
            .isIssued(true)
            .organizationName("unicef")
            .address(address)
            .coordinate(coordinate)
            .timetable(timetable)
            .isPublished(true)
            .build();

    @BeforeEach
    void setUp() {
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
        assertThat(findBy(scheduleNo)).extracting("organizationName", "content", "volunteerNum")
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
        assertThat(findBy(scheduleNo)).extracting("content", "organizationName", "volunteerNum")
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
        List<Long> scheduleNos = scheduleCommandUseCase.addRegulaerSchedule(recruitment, command);

        //then
        assertThat(scheduleNos).hasSize(16);
    }

    @DisplayName("매달 셋째주 토요일, 일요일 반복하는 일정을 생성하고 저장한다.")
    @Test
    void addMonthlySchedule() {
        //given
        final RegularScheduleCreateCommand command = createCommand(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 29),
                Period.MONTH, Week.THIRD, List.of(Day.SAT, Day.SUN));

        //when
        List<Long> scheduleNos = scheduleCommandUseCase.addRegulaerSchedule(recruitment, command);

        //then
        assertThat(scheduleNos).hasSize(6);
    }

    private ScheduleUpsertCommand createCommand(String organizationName, String content, int participationNum) {
        return new ScheduleUpsertCommand(timetable, organizationName, address, content, participationNum);
    }

    private RegularScheduleCreateCommand createCommand(LocalDate recruitmentStartDate, LocalDate recruitmentEndDate,
                                                       Period period, Week week, List<Day> dayOfWeeks) {
        Timetable timetable = new Timetable(recruitmentStartDate, recruitmentEndDate, HourFormat.PM, LocalTime.now(),
                10);
        RepeatPeriodCommand repeatPeriod = new RepeatPeriodCommand(period, week, dayOfWeeks);
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

    private Schedule findBy(Long scheduleNo) {
        return scheduleRepository.findById(scheduleNo)
                .orElseThrow(() -> new NotFoundException("일정이 존재하지 않습니다."));
    }

}
