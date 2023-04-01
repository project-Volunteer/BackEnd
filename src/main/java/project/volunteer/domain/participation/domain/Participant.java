package project.volunteer.domain.participation.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.user.domain.User;

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

    @Column(name = "is_approved")
    private Boolean isApproved;

    @Builder
    public Participant(Recruitment recruitment, User participant) {

        this.recruitment = recruitment;
        this.participant = participant;
        this.isApproved = false; //신청했을 때 초기 승인값 false
    }

    public void approve() { //승인하기
        this.isApproved = true;
    }
}
