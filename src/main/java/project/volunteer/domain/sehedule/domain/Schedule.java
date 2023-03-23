package project.volunteer.domain.sehedule.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.global.common.auditing.BaseTimeEntity;

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

    @Column(name = "start_day", nullable = false)
    private LocalDate startDay;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "progress_time", columnDefinition = "TINYINT", length = 25, nullable = false)
    private Integer progressTime; //(1~24시간)

    @Column(length = 50)
    private String content;

    /**
     *  Auditing - 생성인, 수정인 추가 필요
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruitmentno")
    private Recruitment recruitment;

    @Builder
    public Schedule(LocalDate startDay, LocalTime startTime, Integer progressTime, String content) {
        this.startDay = startDay;
        this.startTime = startTime;
        this.progressTime = progressTime;
        this.content = content;
    }

    public void setRecruitment(Recruitment recruitment) {
        this.recruitment = recruitment;
    }
}
