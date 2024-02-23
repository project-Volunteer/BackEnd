package project.volunteer.domain.scheduleParticipation.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.recruitmentParticipation.converter.StateConverter;
import project.volunteer.domain.recruitmentParticipation.domain.RecruitmentParticipation;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.global.common.auditing.BaseEntity;
import project.volunteer.global.common.component.ParticipantState;

import javax.persistence.*;

@Getter
@Entity
@Table(name = "vlt_schedule_participant")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleParticipation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_participant_no")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheduleno")
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruitment_participation_no")
    private RecruitmentParticipation recruitmentParticipation;

    @Convert(converter = StateConverter.class)
    @Column(length = 3, nullable = false)
    private ParticipantState state;

    public ScheduleParticipation(Schedule schedule, RecruitmentParticipation recruitmentParticipation, ParticipantState state) {
        this.schedule = schedule;
        this.recruitmentParticipation = recruitmentParticipation;
        this.state = state;
    }

    public Boolean canReParticipation() {
        return state.equals(ParticipantState.PARTICIPATION_CANCEL_APPROVAL);
    }

    public Boolean canCancel() {
        return state.equals(ParticipantState.PARTICIPATING);
    }

    public void changeState(ParticipantState state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "ScheduleParticipation{" +
                "scheduleParticipationNo=" + id +
                ", scheduleNo=" + schedule.getScheduleNo() +
                ", recruitmentParticipationNo=" + recruitmentParticipation.getId() +
                ", state=" + state +
                '}';
    }










    public static ScheduleParticipation createScheduleParticipation(Schedule schedule, RecruitmentParticipation recruitmentParticipation, ParticipantState state){
        ScheduleParticipation createScheduleParticipation = new ScheduleParticipation();
        createScheduleParticipation.schedule = schedule;
        createScheduleParticipation.recruitmentParticipation = recruitmentParticipation;
        createScheduleParticipation.state = state;
        return createScheduleParticipation;
    }

    public void delete(){
        this.state = ParticipantState.DELETED;
    }
    public void removeScheduleAndParticipant(){
        this.schedule = null;
        this.recruitmentParticipation = null;
    }

    public Boolean isEqualState(ParticipantState state) {return this.state.equals(state);}

}
