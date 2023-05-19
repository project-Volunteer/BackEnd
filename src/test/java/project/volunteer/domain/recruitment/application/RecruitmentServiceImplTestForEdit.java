package project.volunteer.domain.recruitment.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.repeatPeriod.application.RepeatPeriodService;
import project.volunteer.domain.repeatPeriod.dao.RepeatPeriodRepository;
import project.volunteer.domain.repeatPeriod.domain.Day;
import project.volunteer.domain.repeatPeriod.domain.Period;
import project.volunteer.domain.repeatPeriod.domain.RepeatPeriod;
import project.volunteer.domain.repeatPeriod.domain.Week;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.*;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.test.WithMockCustomUser;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class RecruitmentServiceImplTestForEdit {

    @PersistenceContext EntityManager em;
    @Autowired UserRepository userRepository;
    @Autowired RecruitmentService recruitmentService;
    @Autowired RecruitmentRepository recruitmentRepository;
    @Autowired RepeatPeriodService repeatPeriodService;
    @Autowired RepeatPeriodRepository repeatPeriodRepository;

    User writer;
    Recruitment saveRecruitment;
    private void clear() {
        em.flush();
        em.clear();
    }
    @BeforeEach
    private void initRegRecruitment() {
        writer = userRepository.save(User.builder()
                .id("1234")
                .password("1234")
                .nickName("nickname")
                .email("email@gmail.com")
                .gender(Gender.M)
                .birthDay(LocalDate.now())
                .picture("picture")
                .joinAlarmYn(true).beforeAlarmYn(true).noticeAlarmYn(true)
                .role(Role.USER)
                .provider("kakao").providerId("1234")
                .build());

        saveRecruitment = recruitmentRepository.save(
                Recruitment.builder()
                        .title("1234")
                        .content("1234")
                        .volunteeringCategory(VolunteeringCategory.EDUCATION)
                        .volunteeringType(VolunteeringType.REG)
                        .volunteerType(VolunteerType.ALL)
                        .volunteerNum(9999)
                        .isIssued(true)
                        .organizationName("1234")
                        .address(
                                Address.builder()
                                        .sido("1")
                                        .sigungu("1234")
                                        .details("1234")
                                        .build()
                        )
                        .coordinate(
                                Coordinate.builder()
                                        .longitude(3.2F)
                                        .latitude(3.2F)
                                        .build()
                        )
                        .timetable(
                                Timetable.builder()
                                        .progressTime(3)
                                        .startDay(LocalDate.now())
                                        .endDay(LocalDate.now())
                                        .startTime(LocalTime.now())
                                        .hourFormat(HourFormat.AM)
                                        .build()
                        )
                        .isPublished(true)
                        .build());
        saveRecruitment.setWriter(writer);

        for(int i=1;i<=2;i++){
            RepeatPeriod period = repeatPeriodRepository.save(
                    RepeatPeriod.builder()
                            .period(Period.MONTH)
                            .week(Week.FIRST)
                            .day(Day.ofValue(i))
                            .build());
            period.setRecruitment(saveRecruitment);
        }
        clear();
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 정기모집글_삭제_성공_반복주기포함(){
        //given & when
        recruitmentService.deleteRecruitment(saveRecruitment.getRecruitmentNo());
        clear();

        //then
        Recruitment recruitment = recruitmentRepository.findById(saveRecruitment.getRecruitmentNo()).get();
        assertThat(recruitment.getIsDeleted()).isEqualTo(IsDeleted.Y);

        List<RepeatPeriod> findPeriod = repeatPeriodRepository.findByRecruitment_RecruitmentNo(saveRecruitment.getRecruitmentNo());
        findPeriod.stream()
                .forEach(p -> assertThat(p.getIsDeleted()).isEqualTo(IsDeleted.Y));
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 정기모집글_삭제_실패_없는모집글(){
        assertThatThrownBy(() -> recruitmentService.deleteRecruitment(Long.MAX_VALUE)) //없는 모집글 PK
                .isInstanceOf(BusinessException.class);
    }

    @DisplayName("봉사 모집글 시 권한이 없는 경우 예외가 발생한다.")
    @Test
    @WithMockCustomUser(tempValue = "temp")
    public void 정기모집글_삭제_권한예외(){
        assertThatThrownBy(() -> recruitmentService.deleteRecruitment(saveRecruitment.getRecruitmentNo()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("FORBIDDEN_RECRUITMENT");
    }

}