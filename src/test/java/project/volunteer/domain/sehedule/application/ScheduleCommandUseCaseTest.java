package project.volunteer.domain.sehedule.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.amazonaws.services.kms.model.NotFoundException;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import project.volunteer.common.ServiceTest;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.sehedule.application.dto.ScheduleCreateCommand;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.Coordinate;
import project.volunteer.global.common.component.HourFormat;
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
            .participationNum(15)
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
        final ScheduleCreateCommand command = createCommand("unicef", "조심히", 10);

        //when
        Long scheduleNo = scheduleCommandUseCase.addSchedule(recruitment, command);

        //then
        assertThat(findBy(scheduleNo)).extracting("organizationName", "content", "volunteerNum")
                .containsExactly(command.getOrganizationName(), command.getContent(), command.getMaxParticipationNum());

    }

    private ScheduleCreateCommand createCommand(String organizationName, String content, int participationNum) {
        Timetable timetable = new Timetable(LocalDate.now(), LocalDate.now(), HourFormat.AM, LocalTime.now(), 10);
        Address address = new Address("1111", "111", "삼성 아파트", "대구광역시 북구 삼성 아파트");
        return new ScheduleCreateCommand(timetable, organizationName, address, content, participationNum);
    }

    private Schedule findBy(Long scheduleNo) {
        return scheduleRepository.findById(scheduleNo)
                .orElseThrow(() -> new NotFoundException("일정이 존재하지 않습니다."));
    }

}
