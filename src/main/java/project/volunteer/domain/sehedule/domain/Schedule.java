package project.volunteer.domain.sehedule.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.global.common.auditing.BaseTimeEntity;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.Timetable;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

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

    /**
     *  Auditing - 생성인, 수정인 추가 필요
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruitmentno")
    private Recruitment recruitment;

    @Builder
    public Schedule(Timetable timetable, String content,String organizationName,  Address address) {
        this.scheduleTimeTable = timetable;
        this.content = content;
        this.organizationName = organizationName;
        this.address = address;
    }

    public void setRecruitment(Recruitment recruitment) {
        this.recruitment = recruitment;
    }

}
