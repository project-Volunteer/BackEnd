package project.volunteer.domain.sehedule.application.timetableCreator;

import java.util.List;
import project.volunteer.domain.recruitment.application.dto.command.RepeatPeriodCreateCommand;
import project.volunteer.global.common.component.Timetable;

public interface RepeatTimetableCreator {
    boolean isSupported(RepeatPeriodCreateCommand repeatPeriod);
    List<Timetable> create(Timetable recruitmentTimetable, RepeatPeriodCreateCommand repeatPeriod);
}
