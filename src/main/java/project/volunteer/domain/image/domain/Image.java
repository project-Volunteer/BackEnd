package project.volunteer.domain.image.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.storage.domain.Storage;
import project.volunteer.global.common.auditing.BaseTimeEntity;

import javax.persistence.*;

@Getter
@Entity
@Table(name = "vlt_image")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "imageno")
    private Long imageNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "realwork_code", length = 20, nullable = false)
    private RealWorkCode realWorkCode; // 타입:log, recruitment, user

    @Column(nullable = false)
    private Long no; //로그 번호, 모집글 번호, 유저 번호

    @Column(name = "static_image_name", length = 10)
    private String staticImageName; //static 이미지 코드

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storageno")
    private Storage storage; //upload

    /**
     * Auditing - 생성인, 수정인 추가 필요
     */

    @Builder
    public Image(RealWorkCode realWorkCode, Long no, String staticImageName) {
        this.realWorkCode = realWorkCode;
        this.no = no;
        this.staticImageName = staticImageName;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }
}
