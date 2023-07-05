package project.volunteer.domain.like.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.logboard.domain.Logboard;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.auditing.BaseEntity;

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
    @JoinColumn(name = "logboardno")
    private Logboard logboard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userno")
    private User user;

    @Column(name = "create_by", nullable = false)
    private Long createUserNo;
    
    @Column(name = "like_ok", nullable = false)
    private Boolean likeOk;

	
}
