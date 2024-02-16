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
@Table(name = "vlt_recruitment_participation")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecruitmentParticipation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recruitment_participation_no")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruitmentno")
    private Recruitment recruitment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userno")
    private User user;

    @Convert(converter = StateConverter.class)
    @Column(length = 3, nullable = false)
    private ParticipantState state;

    @Builder
    public RecruitmentParticipation(Recruitment recruitment, User participant, ParticipantState state) {
        this.recruitment = recruitment;
        this.user = participant;
        this.state = state;
    }

    public boolean canRejoin() {
        return !state.equals(ParticipantState.JOIN_REQUEST) && !state.equals(ParticipantState.JOIN_APPROVAL);
    }

    public boolean canCancel() {
        return state.equals(ParticipantState.JOIN_REQUEST);
    }

    public boolean canApproval() {
        return state.equals(ParticipantState.JOIN_REQUEST);
    }

    public boolean canDeport() {
        return state.equals(ParticipantState.JOIN_APPROVAL);
    }

    public void changeState(ParticipantState state) {
        this.state = state;
    }











    public static RecruitmentParticipation createParticipant(Recruitment recruitment, User participant,
                                                             ParticipantState state) {
        RecruitmentParticipation createParticipant = new RecruitmentParticipation();
        createParticipant.recruitment = recruitment;
        createParticipant.user = participant;
        createParticipant.state = state;
        return createParticipant;
    }

    public Boolean isEqualState(ParticipantState state) {
        return this.state.equals(state);
    }

    public void removeUserAndRecruitment() {
        this.recruitment = null;
        this.user = null;
    }

    public void delete() {
        this.state = ParticipantState.DELETED;
    }
}
