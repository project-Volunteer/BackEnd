package project.volunteer.domain.recruitment.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.auditing.BaseTimeEntity;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.Coordinate;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.global.common.component.Timetable;
import project.volunteer.domain.recruitment.converter.CategoryConverter;
import project.volunteer.domain.recruitment.converter.VolunteerTypeConverter;

import javax.persistence.*;

@Getter
@Entity
@Table(name = "vlt_recruitment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recruitment extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recruitmentno")
    private Long recruitmentNo;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String content;

    @Convert(converter = CategoryConverter.class)
    @Column(name = "volunteering_category", length = 3, nullable = false)
    private VolunteeringCategory volunteeringCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "volunteering_type", length = 5, nullable = false)
    private VolunteeringType volunteeringType;

    @Convert(converter = VolunteerTypeConverter.class)
    @Column(name = "volunteer_type", length = 2, nullable = false)
    private VolunteerType volunteerType;

    @Column(name = "volunteer_num", nullable = false)
    private Integer volunteerNum;

    @Column(name = "is_issued", nullable = false)
    private Boolean isIssued;

    @Column(name = "organization_name", length = 50, nullable = false)
    private String organizationName;

    @Embedded
    private Address address;

    @Embedded
    private Coordinate coordinate;

    @Embedded
    private Timetable VolunteeringTimeTable;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_deleted", length = 1, nullable = false)
    private IsDeleted isDeleted;

    /**
     * Auditing - 작성자, 수정인 추가 필요
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userno")
    private User writer;

    @Builder
    public Recruitment(String title, String content, VolunteeringCategory volunteeringCategory, VolunteeringType volunteeringType,
                       VolunteerType volunteerType, Integer volunteerNum, Boolean isIssued,
                       String organizationName, Address address, Coordinate coordinate,
                       Timetable timetable, Boolean isPublished) {

        this.title = title;
        this.content = content;
        this.volunteeringCategory = volunteeringCategory;
        this.volunteeringType = volunteeringType;
        this.volunteerType = volunteerType;
        this.volunteerNum = volunteerNum;
        this.isIssued = isIssued;
        this.organizationName = organizationName;
        this.address = address;
        this.coordinate = coordinate;
        this.VolunteeringTimeTable = timetable;
        this.isPublished = isPublished;

        this.likeCount = 0;
        this.viewCount = 0;
        this.isDeleted = IsDeleted.N;
    }

    public void setWriter(User user){
        this.writer = user;
    }

    public void setDeleted(){this.isDeleted=IsDeleted.Y;}

    public void setIsPublished(Boolean isPublished){this.isPublished=isPublished;}

    public void setVolunteeringTimeTable(Timetable timetable){
        this.VolunteeringTimeTable = timetable;
    }

}
