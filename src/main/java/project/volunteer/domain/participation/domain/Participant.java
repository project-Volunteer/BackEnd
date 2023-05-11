package project.volunteer.domain.participation.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.auditing.BaseTimeEntity;
import project.volunteer.global.common.component.State;
import project.volunteer.domain.participation.converter.StateConverter;

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
    private State state;

    @Builder
    public Participant(Recruitment recruitment, User participant, State state){
        this.recruitment = recruitment;
        this.participant = participant;
        this.state = state;
    }

    public void joinRequest(){ //참가 요청
        this.state = State.JOIN_REQUEST;
    }
    public void joinCancel(){ this.state = State.JOIN_CANCEL; }
    public void joinApprove() { //참가 승인
        this.state = State.JOIN_APPROVAL;
    }
    public void deport() {this.state = State.DEPORT; }
}
