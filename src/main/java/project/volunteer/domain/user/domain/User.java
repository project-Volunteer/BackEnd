package project.volunteer.domain.user.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.global.common.auditing.BaseTimeEntity;

import javax.persistence.*;
import java.time.LocalDate;

@Getter
@Entity
@Table(name = "vlt_user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userno")
    private Long userNo;
    
    @Column(name = "id", length = 100, nullable = false)
    private String id;
    
    @Column(name = "password", length = 100, nullable = false)
    private String password;
    
    @Column(name = "nick_name", length = 13, nullable = false)
    private String nickName;

    @Column(length = 50, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(name = "birth_day",nullable = false)
    private LocalDate birthDay;

    @Column(length = 500)
    private String picture;

    @Column(name = "joinalarm_yn", nullable = false)
    private Boolean joinAlarmYn;

    @Column(name = "noticealaram_yn", nullable = false)
    private Boolean noticeAlarmYn;

    @Column(name = "beforealarm_yn", nullable = false)
    private Boolean beforeAlarmYn;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private Role role;
    
    @Column(length = 50, nullable = false)
    private String provider;
    
    @Column(name = "provider_id", length = 50, nullable = false)
    private String providerId;
    
    @Column(name = "refresh_token", length = 255)
    private String refreshToken;

    /**
     * Auditing - 생성인, 수정인 추가 필요
     */
    
    @Builder
    public User(String id, String password, String nickName, String email, Gender gender, LocalDate birthDay, String picture, Boolean joinAlarmYn,
                Boolean noticeAlarmYn, Boolean beforeAlarmYn, Role role, String provider,String providerId, String refreshToken) {
        this.id = id;
        this.password = password;
        this.nickName = nickName;
        this.email = email;
        this.gender = gender;
        this.birthDay = birthDay;
        this.picture = picture;
        this.joinAlarmYn = joinAlarmYn;
        this.noticeAlarmYn = noticeAlarmYn;
        this.beforeAlarmYn = beforeAlarmYn;
        this.role = role;
        this.provider = provider;
        this.providerId = providerId;
        this.refreshToken = refreshToken;
    }

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

    //테스트 케이스를 위한 것
    public void setUserNo(Long userNo){
        this.userNo = userNo;
    }

}
