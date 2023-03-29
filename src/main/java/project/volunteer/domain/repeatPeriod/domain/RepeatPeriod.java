package project.volunteer.domain.repeatPeriod.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.recruitment.domain.Recruitment;

import javax.persistence.*;

@Getter
@Entity
@Table(name = "vlt_repeat_period")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RepeatPeriod {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "repeat_period_no")
    private Long repeatPeriodNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "period", length = 10, nullable = false)
    private Period period;

    @Enumerated(EnumType.STRING)
    @Column(name = "week", length = 10)
    private Week week;

    @Enumerated(EnumType.STRING)
    @Column(name = "day", length = 10, nullable = false)
    private Day day;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruitmentno")
    private Recruitment recruitment;

    @Builder
    public RepeatPeriod(Period period, Week week, Day day) {
        this.period = period;
        this.week = week;
        this.day = day;
    }

    public void setRecruitment(Recruitment recruitment){
        this.recruitment = recruitment;
    }
}
