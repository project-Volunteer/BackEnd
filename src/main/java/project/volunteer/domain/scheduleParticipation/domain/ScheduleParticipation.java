package project.volunteer.domain.scheduleParticipation.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.participation.converter.StateConverter;
import project.volunteer.domain.participation.domain.Participant;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.global.common.auditing.BaseEntity;
import project.volunteer.global.common.component.State;

import javax.persistence.*;

@Getter
@Entity
@Table(name = "vlt_schedule_participant")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleParticipation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_participant_no")
    private Long scheduleParticipationNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheduleno")
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participantno")
    private Participant participant;

    @Convert(converter = StateConverter.class)
    @Column(length = 3, nullable = false)
    private State state;

    public static ScheduleParticipation createScheduleParticipation(Schedule schedule, Participant participant, State state){
        ScheduleParticipation createScheduleParticipation = new ScheduleParticipation();
        createScheduleParticipation.schedule = schedule;
        createScheduleParticipation.participant = participant;
        createScheduleParticipation.state = state;
        return createScheduleParticipation;
    }

    public void delete(){
        this.state = State.DELETED;
    }

    public void participating(){
        this.state = State.PARTICIPATING;
    }

    public void cancelParticipation() { this.state = State.PARTICIPATION_CANCEL;}

}
