package project.volunteer.domain.confirmation.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.image.converter.RealWorkCodeConverter;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.auditing.BaseTimeEntity;
import project.volunteer.global.common.component.RealWorkCode;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Confirmation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "confirmation_no")
    private Long confirmationNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userno")
    private User user;

    @Convert(converter = RealWorkCodeConverter.class)
    @Column(name = "realwork_code", length = 2, nullable = false)
    private RealWorkCode realWorkCode;

    @Column(nullable = false)
    private Long no;

    /**
     * Auditing - 작성자, 수정인 추가 필요
     */

    public static Confirmation createConfirmation(RealWorkCode code, Long no){
        Confirmation createConfirmation = new Confirmation();
        createConfirmation.realWorkCode = code;
        createConfirmation.no = no;
        return createConfirmation;
    }
    public void setUser(User user){
        this.user = user;
    }
}
