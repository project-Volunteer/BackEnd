package project.volunteer.domain.recruitment.domain;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.recruitment.domain.repeatPeriod.RepeatPeriod;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.auditing.BaseTimeEntity;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.Coordinate;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.global.common.component.Timetable;
import project.volunteer.domain.recruitment.converter.CategoryConverter;
import project.volunteer.domain.recruitment.converter.VolunteerTypeConverter;

import javax.persistence.*;
import java.time.LocalDate;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

@Getter
@Entity
@Table(name = "vlt_recruitment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recruitment extends BaseTimeEntity {
    private static final int MAX_ORGANIZATION_NAME_SIZE = 50;
    private static final int MAX_PARTICIPATION_NUM = 9999;
    private static final int MIN_PARTICIPATION_NUM = 1;
    private static final int MIN_CURRENT_PARTICIPATION_NUM = 0;
    private static final int MAX_TITLE_SIZE = 255;
    private static final int MAX_CONTENT_SIZE = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Column(name = "max_particiaption_num", nullable = false)
    private Integer maxParticipationNum;

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
    private Timetable timetable;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_deleted", length = 1, nullable = false)
    private IsDeleted isDeleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userno")
    private User writer;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "recruitment", cascade = CascadeType.ALL)
    private List<RepeatPeriod> repeatPeriods = new ArrayList<>();

    @Builder
    public Recruitment(String title, String content, VolunteeringCategory volunteeringCategory,
                       VolunteeringType volunteeringType, VolunteerType volunteerType, Integer maxParticipationNum,
                       Integer currentVolunteerNum, Boolean isIssued, String organizationName, Address address,
                       Coordinate coordinate, Timetable timetable, Integer viewCount, Integer likeCount,
                       Boolean isPublished, IsDeleted isDeleted, User writer) {
        validateOrganizationNameSize(organizationName);
        validateMaxParticipationNum(maxParticipationNum);
        validateCurrentParticipationNum(currentVolunteerNum, maxParticipationNum);
        validateTitleSize(title);
        validateContentSize(content);

        this.title = title;
        this.content = content;
        this.volunteeringCategory = volunteeringCategory;
        this.volunteeringType = volunteeringType;
        this.volunteerType = volunteerType;
        this.maxParticipationNum = maxParticipationNum;
        this.currentVolunteerNum = currentVolunteerNum;
        this.isIssued = isIssued;
        this.organizationName = organizationName;
        this.address = address;
        this.coordinate = coordinate;
        this.timetable = timetable;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.isPublished = isPublished;
        this.isDeleted = isDeleted;
        this.writer = writer;
    }

    public static Recruitment create(String title, String content, VolunteeringCategory volunteeringCategory,
                                     VolunteeringType volunteeringType,
                                     VolunteerType volunteerType, Integer maxParticipationNum, Boolean isIssued,
                                     String organizationName, Address address, Coordinate coordinate,
                                     Timetable timetable, Boolean isPublished, User writer) {
        return Recruitment.builder()
                .title(title)
                .content(content)
                .volunteeringCategory(volunteeringCategory)
                .volunteeringType(volunteeringType)
                .volunteerType(volunteerType)
                .isIssued(isIssued)
                .maxParticipationNum(maxParticipationNum)
                .isIssued(isIssued)
                .organizationName(organizationName)
                .address(address)
                .coordinate(coordinate)
                .timetable(timetable)
                .isPublished(isPublished)
                .writer(writer)
                .isDeleted(IsDeleted.N)
                .likeCount(0)
                .viewCount(0)
                .currentVolunteerNum(0)
                .build();
    }

    public void setRepeatPeriods(List<RepeatPeriod> repeatPeriods) {
        repeatPeriods.forEach(repeatPeriod -> repeatPeriod.assignRecruitment(this));
        this.repeatPeriods = repeatPeriods;
    }

    public void delete() {
        this.isDeleted = IsDeleted.Y;
        this.writer = null;
        this.repeatPeriods.forEach(RepeatPeriod::delete);
    }

    public Boolean isRegularRecruitment() {
        return volunteeringType.equals(VolunteeringType.REG);
    }

    public void checkDoneDate(LocalDate now) {
        if (isDone(now)) {
            throw new BusinessException(ErrorCode.EXPIRED_PERIOD_RECRUITMENT);
        }
    }

    public Boolean isOwner(Long userNo) {
        return this.writer.getUserNo()
                .equals(userNo);
    }

    public Boolean isDone(LocalDate now) {
        return this.timetable.isDoneByEndDate(now);
    }

    public Boolean isFull() {
        return maxParticipationNum.equals(currentVolunteerNum);
    }

    public boolean isLessParticipationNumThan(final int participationNum) {
        return this.maxParticipationNum < participationNum;
    }

    public void increaseParticipationNum(int addParticipationNum) {
        this.currentVolunteerNum += addParticipationNum;
        validateCurrentParticipationNum(currentVolunteerNum, maxParticipationNum);
    }

    public void decreaseParticipationNum(int subParticipationNum) {
        this.currentVolunteerNum -= subParticipationNum;
        validateCurrentParticipationNum(currentVolunteerNum, maxParticipationNum);
    }

    /**
     * 검증 메서드
     **/
    private void validateOrganizationNameSize(final String organizationName) {
        if (organizationName.isBlank() || MAX_ORGANIZATION_NAME_SIZE < organizationName.length()) {
            throw new BusinessException(ErrorCode.INVALID_ORGANIZATION_NAME_SIZE,
                    String.format("[%d]~[%d]", 1, MAX_ORGANIZATION_NAME_SIZE));
        }
    }

    private void validateMaxParticipationNum(final int participationNum) {
        if (MIN_PARTICIPATION_NUM > participationNum || MAX_PARTICIPATION_NUM < participationNum) {
            throw new BusinessException(ErrorCode.INVALID_PARTICIPATION_NUM,
                    String.format("[%d]~[%d]", MIN_PARTICIPATION_NUM, MAX_PARTICIPATION_NUM));
        }
    }

    private void validateTitleSize(final String title) {
        if (title.isBlank() || MAX_TITLE_SIZE < title.length()) {
            throw new BusinessException(ErrorCode.INVALID_TITLE_SIZE,
                    String.format("[%d]~[%d]", 1, MAX_TITLE_SIZE));
        }
    }

    private void validateContentSize(final String content) {
        if (content.isBlank() || MAX_CONTENT_SIZE < content.length()) {
            throw new BusinessException(ErrorCode.INVALID_CONTENT_SIZE,
                    String.format("[%d]~[%d]", 1, MAX_CONTENT_SIZE));
        }
    }

    private void validateCurrentParticipationNum(int currentVolunteerNum, int maxParticipationNum) {
        if (MIN_CURRENT_PARTICIPATION_NUM > currentVolunteerNum || maxParticipationNum < currentVolunteerNum) {
            throw new BusinessException(ErrorCode.INVALID_CURRENT_PARTICIPATION_NUM,
                    String.format("[%d]~[%d]", MIN_CURRENT_PARTICIPATION_NUM, maxParticipationNum));
        }
    }











    public void setWriter(User user) {
        this.writer = user;
    }

    public void setTimetable(Timetable timetable) {
        this.timetable = timetable;
    }

}
