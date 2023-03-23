package project.volunteer.domain.recruitment.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.auditing.BaseTimeEntity;

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

    @Column(length = 10, nullable = false)
    private String country;

    @Column(length = 50, nullable = false)
    private String details;

    @Column(nullable = false)
    private Float latitude;

    @Column(nullable = false)
    private Float longitude;

    @Column(name = "start_day", nullable = false)
    private LocalDate startDay;

    @Column(name = "end_day", nullable = false)
    private LocalDate endDay;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "progress_time", columnDefinition = "TINYINT", length = 25, nullable = false)
    private Integer progressTime; //(1~24시간)

    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished;

    @Column(name = "static_image_code", length = 20)
    private String staticImageCode; //static 이미지 번호(코드)

    /**
     * Auditing - 작성자, 수정인 추가 필요
     * 유저 연관관계 매핑 추가(임시)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userno")
    private User user;

    @Builder
    public Recruitment(String title, String content, VolunteeringCategory volunteeringCategory, VolunteeringType volunteeringType,
                       VolunteerType volunteerType, Integer volunteerNum, Boolean isIssued,
                       String organizationName, String country, String details, Float latitude, Float longitude,
                       LocalDate startDay, LocalDate endDay, LocalTime startTime, Integer progressTime,
                       Boolean isPublished) {

        this.title = title;
        this.content = content;
        this.volunteeringCategory = volunteeringCategory;
        this.volunteeringType = volunteeringType;
        this.volunteerType = volunteerType;
        this.volunteerNum = volunteerNum;
        this.isIssued = isIssued;
        this.organizationName = organizationName;
        this.country = country;
        this.details = details;
        this.latitude = latitude;
        this.longitude = longitude;
        this.startDay = startDay;
        this.endDay = endDay;
        this.startTime = startTime;
        this.progressTime = progressTime;
        this.isPublished = isPublished;

        this.likeCount = 0;
        this.viewCount = 0;
    }

    public void setUser(User user){
        this.user = user;
    }

}
