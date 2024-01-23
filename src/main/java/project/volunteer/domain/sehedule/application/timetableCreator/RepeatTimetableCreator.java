package project.volunteer.domain.sehedule.application.timetableCreator;

import java.util.List;
import project.volunteer.domain.recruitment.application.dto.RepeatPeriodCommand;
import project.volunteer.global.common.component.Timetable;

public interface RepeatTimetableCreator {
    boolean isSupported(RepeatPeriodCommand repeatPeriod);
    List<Timetable> create(Timetable recruitmentTimetable, RepeatPeriodCommand repeatPeriod);
}
