package project.volunteer.domain.recruitment.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.auditing.BaseTimeEntity;
import project.volunteer.global.common.component.Timetable;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Entity
@Table(name = "vlt_recruitment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recruitment extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recruitment_no")
    private Long recruitmentNo;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "volunteering_category", length = 30, nullable = false)
    private VolunteeringCategory volunteeringCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "volunteering_type", length = 5, nullable = false)
    private VolunteeringType volunteeringType;

    @Enumerated(EnumType.STRING)
    @Column(name = "volunteer_type", length = 10, nullable = false)
    private VolunteerType volunteerType;

    @Column(name = "volunteer_num", nullable = false)
    private Integer volunteerNum;

    @Column(name = "is_issued", nullable = false)
    private Boolean isIssued;




    @Column(name = "organization_name", length = 50, nullable = false)
    private String organizationName;

    @Column(length = 5, nullable = false)
    private String sido;

    @Column(length = 10, nullable = false)
    private String sigungu;

    @Column(length = 50, nullable = false)
    private String details;

    @Column(nullable = false)
    private Float latitude;

    @Column(nullable = false)
    private Float longitude;





    @Embedded
    private Timetable VolunteeringTimeTable;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished;

    /**
     * Auditing - 작성자, 수정인 추가 필요
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userno")
    private User writer;

    @Builder
    public Recruitment(String title, String content, VolunteeringCategory volunteeringCategory, VolunteeringType volunteeringType,
                       VolunteerType volunteerType, Integer volunteerNum, Boolean isIssued,
                       String organizationName, String sido, String sigungu, String details, Float latitude, Float longitude,
                       Timetable timetable, Boolean isPublished) {

        this.title = title;
        this.content = content;
        this.volunteeringCategory = volunteeringCategory;
        this.volunteeringType = volunteeringType;
        this.volunteerType = volunteerType;
        this.volunteerNum = volunteerNum;
        this.isIssued = isIssued;
        this.organizationName = organizationName;
        this.sido = sido;
        this.sigungu = sigungu;
        this.details = details;
        this.latitude = latitude;
        this.longitude = longitude;
        this.VolunteeringTimeTable = timetable;
        this.isPublished = isPublished;

        this.likeCount = 0;
        this.viewCount = 0;
    }

    public void setWriter(User user){
        this.writer = user;
    }

}
