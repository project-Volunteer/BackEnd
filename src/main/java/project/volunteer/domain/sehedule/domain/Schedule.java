package project.volunteer.domain.sehedule.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.global.common.auditing.BaseTimeEntity;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.global.common.component.Timetable;

import javax.persistence.*;
import java.time.LocalDate;

@Getter
@Entity
@Table(name = "vlt_schedule")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Schedule extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scheduleno")
    private Long scheduleNo;
    @Embedded
    private Timetable scheduleTimeTable;
    @Column(name = "organization_name", length = 50, nullable = false)
    private String organizationName;
    @Embedded
    private Address address;
    @Column(length = 50)
    private String content;

    @Column(name = "volunteer_num", nullable = false)
    private Integer volunteerNum;

    @Column(name = "current_volunteer_num", nullable = false)
    private Integer currentVolunteerNum;
    //낙관적 락 사용
//    @Version
//    private Integer version;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_deleted", length = 1, nullable = false)
    private IsDeleted isDeleted;

    /**
     *  Auditing - 생성인, 수정인 추가 필요
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruitmentno")
    private Recruitment recruitment;

    @Builder
    public Schedule(Timetable timetable, String content,String organizationName,  Address address, int volunteerNum) {
        this.scheduleTimeTable = timetable;
        this.content = content;
        this.organizationName = organizationName;
        this.address = address;
        this.volunteerNum = volunteerNum;

        this.isDeleted = IsDeleted.N;
        this.currentVolunteerNum = 0;
    }

    public static Schedule createSchedule(Timetable timetable, String content, String organizationName, Address address, int volunteerNum){
        Schedule schedule = new Schedule();
        schedule.scheduleTimeTable = timetable;
        schedule.content = content;
        schedule.organizationName = organizationName;
        schedule.address = address;
        schedule.volunteerNum = volunteerNum;

        schedule.isDeleted = IsDeleted.N;
        schedule.currentVolunteerNum = 0;
        return schedule;
    }

    public void changeSchedule(Timetable timetable, String content, String organizationName, Address address, int volunteerNum){
        this.scheduleTimeTable = timetable;
        this.content = content;
        this.organizationName = organizationName;
        this.address = address;
        this.volunteerNum = volunteerNum;
    }

    public void delete(){
        this.isDeleted = IsDeleted.Y;
    }
    public void setRecruitment(Recruitment recruitment) {
        this.recruitment = recruitment;
    }
    public void changeScheduleTime(Timetable timetable){
        this.scheduleTimeTable = timetable;
    }

    public void increaseParticipant(){this.currentVolunteerNum++;}
    public void decreaseParticipant(){this.currentVolunteerNum--;}

    public Boolean isFullParticipant(){return this.currentVolunteerNum==this.volunteerNum;}
    public Boolean isAvailableDate(){return this.scheduleTimeTable.getStartDay().isAfter(LocalDate.now());}

}
