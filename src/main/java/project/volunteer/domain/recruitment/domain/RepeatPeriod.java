package project.volunteer.domain.recruitment.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.global.common.component.IsDeleted;

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
    @Column(name = "day_of_week", length = 10, nullable = false)
    private Day day;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruitmentno")
    private Recruitment recruitment;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_deleted", length = 1, nullable = false)
    private IsDeleted isDeleted;

    @Builder
    public RepeatPeriod(Period period, Week week, Day day) {
        this.period = period;
        this.week = week;
        this.day = day;

        this.isDeleted = IsDeleted.N;
    }

    public static RepeatPeriod createRepeatPeriod(Period period, Week week, Day day){
        RepeatPeriod repeatPeriod = new RepeatPeriod();
        repeatPeriod.period = period;
        repeatPeriod.week = week;
        repeatPeriod.day = day;
        repeatPeriod.isDeleted = IsDeleted.N;
        return repeatPeriod;
    }

    public void setRecruitment(Recruitment recruitment){
        this.recruitment = recruitment;
    }

    public void setDeleted(){
        this.isDeleted=IsDeleted.Y;
    }
    public void removeRecruitment(){this.recruitment = null;}
}
