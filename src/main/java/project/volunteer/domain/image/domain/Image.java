package project.volunteer.domain.image.domain;

import lombok.Getter;
import project.volunteer.domain.storage.domain.Storage;
import project.volunteer.global.common.auditing.BaseTimeEntity;

import javax.persistence.*;

@Getter
@Entity
@Table(name = "vlt_image")
public class Image extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "imageno")
    private Long imageNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "realwork_code", length = 20, nullable = false)
    private RealWorkType realWorkCode; // 타입:log, recruitment, user

    @Column(nullable = false)
    private Long no; //로그 번호, 모집글 번호, 유저 번호

    @Column(name = "static_image_name", length = 10)
    private String staticImageName; //static 이미지 코드

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storageno")
    private Storage storage;

    /**
     * Auditing - 생성인, 수정인 추가 필요
     */

}
