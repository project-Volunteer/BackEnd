package project.volunteer.domain.recruitmentParticipation.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.auditing.BaseTimeEntity;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.domain.recruitmentParticipation.converter.StateConverter;

import javax.persistence.*;
@Getter
@Entity
@Table(name = "vlt_participant")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Participant extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participantno")
    private Long participantNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruitmentno")
    private Recruitment recruitment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userno")
    private User participant;

    @Convert(converter = StateConverter.class)
    @Column(length = 3, nullable = false)
    private ParticipantState state;

    @Builder
    public Participant(Recruitment recruitment, User participant, ParticipantState state){
        this.recruitment = recruitment;
        this.participant = participant;
        this.state = state;
    }

    public static Participant createParticipant(Recruitment recruitment, User participant, ParticipantState state){
        Participant createParticipant = new Participant();
        createParticipant.recruitment = recruitment;
        createParticipant.participant = participant;
        createParticipant.state = state;
        return createParticipant;
    }

    public Boolean isEqualState(ParticipantState state) {return this.state.equals(state);}

    public void updateState(ParticipantState state) {this.state = state;}

    public void removeUserAndRecruitment(){
        this.recruitment = null;
        this.participant = null;
    }
    public void delete(){
        this.state = ParticipantState.DELETED;
    }
}
