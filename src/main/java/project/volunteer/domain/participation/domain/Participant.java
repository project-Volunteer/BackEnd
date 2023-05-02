package project.volunteer.domain.participation.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.State;
import project.volunteer.domain.participation.converter.StateConverter;

import javax.persistence.*;
@Getter
@Entity
@Table(name = "vlt_participant")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Participant {

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
    @Column(length = 2, nullable = false)
    private State state;

    @Builder
    public Participant(Recruitment recruitment, User participant) {

        this.recruitment = recruitment;
        this.participant = participant;
        this.state = State.JOIN_REQUEST; //초기는 참가신청 상태
    }

    public void approve() { //참가 승인
        this.state = State.JOIN_APPROVAL;
    }
}
