package project.volunteer.domain.recruitment.domain.repeatPeriod;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.repeatPeriod.validator.PeriodValidation;
import project.volunteer.global.common.component.IsDeleted;

import javax.persistence.*;

@Getter
@Entity
@Table(name = "vlt_repeat_period")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RepeatPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    public RepeatPeriod(Period period, Week week, Day day, Recruitment recruitment, IsDeleted isDeleted) {
        this.period = period;
        this.week = week;
        this.day = day;
        this.recruitment = recruitment;
        this.isDeleted = isDeleted;
    }

    public static RepeatPeriod create(Period period, Week week, Day dayOfWeek, PeriodValidation periodValidation) {
        periodValidation.validate(period, week, dayOfWeek);
        return RepeatPeriod.builder()
                .period(period)
                .week(week)
                .day(dayOfWeek)
                .isDeleted(IsDeleted.N)
                .build();
    }

    public void assignRecruitment(Recruitment recruitment) {
        this.recruitment = recruitment;
    }















    public void setDeleted() {
        this.isDeleted = IsDeleted.Y;
    }

    public void removeRecruitment() {
        this.recruitment = null;
    }
}
