package project.volunteer.domain.notice.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.global.common.auditing.BaseTimeEntity;
import project.volunteer.global.common.component.IsDeleted;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "vlt_notice")
public class Notice extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "noticeno")
    private Long noticeNo;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private IsDeleted isDeleted;

    @Column(name = "checked_num", nullable = false)
    private Integer checkedNum;

    @Column(name = "comment_num", nullable = false)
    private Integer commentNum;

    @Version //낙관적 락 사용
    private Integer version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruitmentno")
    private Recruitment recruitment;

    /**
     * Auditing - 작성자, 수정인 추가 필요
     */

    public static Notice createNotice(String content){
        Notice createNotice = new Notice();
        createNotice.content = content;

        createNotice.isDeleted = IsDeleted.N;
        createNotice.checkedNum = 0;
        createNotice.commentNum = 0;
        return createNotice;
    }
    public Notice updateNotice(String content){
        this.content = content;
        return this;
    }

    public void delete(){this.isDeleted = IsDeleted.Y;}
    public void setRecruitment(Recruitment recruitment){
        this.recruitment = recruitment;
    }
    public void increaseCheckNum(){this.checkedNum++;}
    public void decreaseCheckNum(){this.checkedNum--;}

}
