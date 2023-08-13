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
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import javax.persistence.*;
import java.time.LocalDate;

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

    @Column(name = "current_volunteer_num", nullable = false)
    private Integer currentVolunteerNum;

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
        this.currentVolunteerNum = 0;
    }

    public static Recruitment createRecruitment(String title, String content, VolunteeringCategory volunteeringCategory, VolunteeringType volunteeringType,
                                                VolunteerType volunteerType, Integer volunteerNum, Boolean isIssued,
                                                String organizationName, Address address, Coordinate coordinate,
                                                Timetable timetable, Boolean isPublished){
        Recruitment createRecruitment = new Recruitment();
        createRecruitment.title = title;
        createRecruitment.content = content;
        createRecruitment.volunteeringCategory = volunteeringCategory;
        createRecruitment.volunteeringType = volunteeringType;
        createRecruitment.volunteerType = volunteerType;
        createRecruitment.volunteerNum = volunteerNum;
        createRecruitment.isIssued = isIssued;
        createRecruitment.organizationName = organizationName;
        createRecruitment.address = address;
        createRecruitment.coordinate = coordinate;
        createRecruitment.VolunteeringTimeTable = timetable;
        createRecruitment.isPublished = isPublished;

        createRecruitment.likeCount = 0;
        createRecruitment.viewCount = 0;
        createRecruitment.isDeleted = IsDeleted.N;
        createRecruitment.currentVolunteerNum = 0;
        return createRecruitment;
    }

    public void setWriter(User user){
        this.writer = user;
    }

    public void setDeleted(){this.isDeleted=IsDeleted.Y;}
    public void removeUser(){this.writer = null;}

    public void setIsPublished(Boolean isPublished){this.isPublished=isPublished;}

    public void setVolunteeringTimeTable(Timetable timetable){
        this.VolunteeringTimeTable = timetable;
    }

    public Boolean isRecruitmentOwner(Long userNo){
        if(this.getWriter().getUserNo().equals(userNo)) {
           return true;
        }
        return false;
    }

    public void increaseTeamMember(){this.currentVolunteerNum++;}
    public void decreaseTeamMember(){this.currentVolunteerNum--;}

    public Boolean isFullTeamMember(){return this.currentVolunteerNum == this.volunteerNum;}

    public Integer getAvailableTeamMemberCount(){
        return this.volunteerNum - this.currentVolunteerNum;
    }

    public Boolean isAvailableDate(){
        return this.VolunteeringTimeTable.getEndDay().isAfter(LocalDate.now());
    }

    public Boolean isDoneDate(){return this.VolunteeringTimeTable.getEndDay().isBefore(LocalDate.now());}
}
