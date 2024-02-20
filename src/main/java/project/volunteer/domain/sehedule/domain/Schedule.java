package project.volunteer.domain.sehedule.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.global.common.auditing.BaseTimeEntity;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.global.common.component.Timetable;

import javax.persistence.*;
import java.time.LocalDate;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

@Getter
@Entity
@Table(name = "vlt_schedule")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Schedule extends BaseTimeEntity {
    private static final int MAX_ORGANIZATION_NAME_SIZE = 50;
    private static final int MAX_CONTENT_SIZE = 50;
    private static final int MAX_PARTICIPATION_NUM = 9999;
    private static final int MIN_PARTICIPATION_NUM = 1;
    private static final int MIN_CURRENT_PARTICIPATION_NUM = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scheduleno")
    private Long scheduleNo;

    @Embedded
    private Timetable scheduleTimeTable;

    @Column(name = "organization_name", length = 50, nullable = false)
    private String organizationName;

    @Embedded
    private Address address;

    @Column(length = 50)
    private String content;

    @Column(name = "volunteer_num", nullable = false)
    private Integer volunteerNum;

    @Column(name = "current_volunteer_num", nullable = false)
    private Integer currentVolunteerNum;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_deleted", length = 1, nullable = false)
    private IsDeleted isDeleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruitmentno")
    private Recruitment recruitment;

    /**
     * 생성 메서드
     **/
    @Builder
    public Schedule(Timetable timetable, String content, String organizationName, Address address,
                    int participationNum,
                    IsDeleted isDeleted, int currentVolunteerNum, Recruitment recruitment) {
        validateContentSize(content);
        validateOrganizationNameSize(organizationName);
        validateParticipationNum(participationNum, recruitment);
        validateCurrentParticipationNum(participationNum, currentVolunteerNum);

        this.scheduleTimeTable = timetable;
        this.content = content;
        this.organizationName = organizationName;
        this.address = address;
        this.volunteerNum = participationNum;
        this.isDeleted = isDeleted;
        this.currentVolunteerNum = currentVolunteerNum;
        this.recruitment = recruitment;
    }

    public static Schedule create(Recruitment recruitment, Timetable timetable, String content, String organizationName,
                                  Address address, int participationNum) {
        return Schedule.builder()
                .timetable(timetable)
                .content(content)
                .organizationName(organizationName)
                .address(address)
                .participationNum(participationNum)
                .isDeleted(IsDeleted.N)
                .currentVolunteerNum(0)
                .recruitment(recruitment)
                .build();
    }

    /**
     * 변경 메서드
     */
    public void change(Recruitment recruitment, Timetable timetable, String content, String organizationName,
                       Address address,
                       int participationNum) {
        validateContentSize(content);
        validateOrganizationNameSize(organizationName);
        validateParticipationNum(participationNum, recruitment);
        validateCurrentParticipationNum(participationNum, this.currentVolunteerNum);

        this.scheduleTimeTable = timetable;
        this.content = content;
        this.organizationName = organizationName;
        this.address = address;
        this.volunteerNum = participationNum;
    }

    public void delete() {
        this.isDeleted = IsDeleted.Y;
    }

    public void increaseParticipationNum(int addParticipationNum) {
        this.currentVolunteerNum += addParticipationNum;
        validateCurrentParticipationNum(this.volunteerNum, this.currentVolunteerNum);
    }

    public void decreaseParticipant() {
        this.currentVolunteerNum--;
    }

    public Boolean isFull() {
        return this.currentVolunteerNum.equals(this.volunteerNum);
    }

    public Boolean isAvailableDate() {
        return this.scheduleTimeTable.getStartDay().isAfter(LocalDate.now());
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

    private void validateContentSize(final String content) {
        if (MAX_CONTENT_SIZE < content.length()) {
            throw new BusinessException(ErrorCode.INVALID_CONTENT_SIZE,
                    String.format("[%d]~[%d]", 0, MAX_CONTENT_SIZE));
        }
    }

    private void validateParticipationNum(final int participationNum, final Recruitment recruitment) {
        if (MIN_PARTICIPATION_NUM > participationNum || MAX_PARTICIPATION_NUM < participationNum) {
            throw new BusinessException(ErrorCode.INVALID_PARTICIPATION_NUM,
                    String.format("[%d]~[%d]", MIN_PARTICIPATION_NUM, MAX_PARTICIPATION_NUM));
        }

        if (recruitment.isLessParticipationNumThan(participationNum)) {
            throw new BusinessException(ErrorCode.EXCEED_PARTICIPATION_NUM_THAN_RECRUITMENT_PARTICIPATION_NUM,
                    String.format("Recruitment = [%d], Schedule = [%d]", recruitment.getMaxParticipationNum(),
                            participationNum));
        }
    }

    private void validateCurrentParticipationNum(final int participationNum, final int currentParticipationNum) {
        if (MIN_CURRENT_PARTICIPATION_NUM > currentParticipationNum || currentParticipationNum > participationNum) {
            throw new BusinessException(ErrorCode.INVALID_CURRENT_PARTICIPATION_NUM,
                    String.format("[%d]~[%d]", MIN_CURRENT_PARTICIPATION_NUM, participationNum));
        }
    }

    public void checkDoneDate(LocalDate currentDate) {
        if (scheduleTimeTable.getStartDay().isBefore(currentDate)) {
            throw new BusinessException(ErrorCode.EXPIRED_PERIOD_SCHEDULE);
        }
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "scheduleNo=" + scheduleNo +
                ", scheduleTimeTable=" + scheduleTimeTable +
                ", organizationName='" + organizationName + '\'' +
                ", address=" + address +
                ", content='" + content + '\'' +
                ", volunteerNum=" + volunteerNum +
                ", currentVolunteerNum=" + currentVolunteerNum +
                ", isDeleted=" + isDeleted +
                ", recruitmentNo=" + recruitment.getRecruitmentNo() +
                '}';
    }

}
