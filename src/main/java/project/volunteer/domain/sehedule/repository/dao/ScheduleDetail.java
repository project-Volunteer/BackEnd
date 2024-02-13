package project.volunteer.domain.sehedule.repository.dao;

import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.Timetable;

@Getter
@NoArgsConstructor
public class ScheduleDetail {
    private Long no;
    private String content;
    private int maxParticipationNum;
    private int currentParticipationNum;
    private Address address;
    private Timetable timetable;
    private Boolean hasData;

    public ScheduleDetail(Long no, String content, int maxParticipationNum, int activeVolunteerNum, Address address,
                          Timetable timetable) {
        this.no = no;
        this.content = content;
        this.maxParticipationNum = maxParticipationNum;
        this.currentParticipationNum = activeVolunteerNum;
        this.address = address;
        this.timetable = timetable;
        this.hasData = true;
    }

    public boolean isDone(LocalDate now) {
        return timetable.isDoneByStartDate(now);
    }

    public boolean isFull() {
        return maxParticipationNum == currentParticipationNum;
    }

}
