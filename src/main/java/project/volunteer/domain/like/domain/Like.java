package project.volunteer.domain.like.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.image.converter.RealWorkCodeConverter;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.auditing.BaseEntity;
import project.volunteer.global.common.component.RealWorkCode;

import javax.persistence.*;

@Getter
@Entity
@Table(name = "vlt_like")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Like extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "likeno")
    private Long likeNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userno")
    private User user;

    @Convert(converter = RealWorkCodeConverter.class)
    @Column(name = "realwork_code", length = 2, nullable = false)
    private RealWorkCode realWorkCode;

    @Column(nullable = false)
    private Long no;

    public static Like createLike(RealWorkCode code, Long no){
        Like createLike = new Like();
        createLike.realWorkCode = code;
        createLike.no = no;
        return createLike;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
