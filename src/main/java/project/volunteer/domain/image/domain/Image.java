package project.volunteer.domain.image.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.global.common.auditing.BaseTimeEntity;
import project.volunteer.domain.image.converter.RealWorkCodeConverter;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.global.common.component.IsDeleted;

import javax.persistence.*;
import java.util.Optional;

@Getter
@Entity
@Table(name = "vlt_image", indexes = {
        @Index(name = "idx_no_realworkcode", columnList = "no, realwork_code")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "imageno")
    private Long imageNo;

    @Convert(converter = RealWorkCodeConverter.class)
    @Column(name = "realwork_code", length = 2, nullable = false)
    private RealWorkCode realWorkCode; // 타입:log, recruitment, user

    @Column(nullable = false)
    private Long no; //로그 번호, 모집글 번호, 유저 번호

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storageno")
    private Storage storage; //upload

    @Enumerated(EnumType.STRING)
    @Column(name = "is_deleted", length = 1, nullable = false)
    private IsDeleted isDeleted;

    /**
     * Auditing - 생성인, 수정인 추가 필요
     */

    @Builder
    public Image(RealWorkCode realWorkCode, Long no) {
        this.realWorkCode = realWorkCode;
        this.no = no;

        this.isDeleted = IsDeleted.N;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public void setDeleted(){
        this.isDeleted=IsDeleted.Y;
        //업로드 이미지 일경우
        Optional.of(storage).ifPresent(s -> s.setDeleted());
    }
}
