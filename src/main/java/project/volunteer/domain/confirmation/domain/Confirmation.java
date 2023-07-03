package project.volunteer.domain.confirmation.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.image.converter.RealWorkCodeConverter;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.RealWorkCode;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Confirmation {

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
}
