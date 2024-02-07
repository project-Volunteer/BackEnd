package project.volunteer.domain.recruitment.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.dao.RepeatPeriodRepository;
import project.volunteer.domain.recruitment.domain.Day;
import project.volunteer.domain.recruitment.domain.Period;
import project.volunteer.domain.recruitment.domain.RepeatPeriod;
import project.volunteer.domain.recruitment.domain.Week;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.*;
import project.volunteer.global.error.exception.BusinessException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class RecruitmentServiceImplTestForEdit {

    @PersistenceContext EntityManager em;
    @Autowired UserRepository userRepository;
    @Autowired
    RecruitmentCommandUseCase recruitmentService;
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
    private void init() {
        //작성자 등록
        writer = User.createUser("1234", "1234", "1234", "1234", Gender.M, LocalDate.now(), "1234",
                true, true, true, Role.USER, "kakao", "1234", null);
        userRepository.save(writer);

        //정기 모집글 등록
        Recruitment createRecruitment = Recruitment.createRecruitment("title", "content", VolunteeringCategory.CULTURAL_EVENT, VolunteeringType.REG,
                VolunteerType.TEENAGER, 3, true, "organization",
                Address.createAddress("11", "1111","details", "fullName"), Coordinate.createCoordinate(3.2F, 3.2F),
                Timetable.createTimetable(LocalDate.now(), LocalDate.now(), HourFormat.AM, LocalTime.now(), 3), true);
        createRecruitment.setWriter(writer);
        saveRecruitment = recruitmentRepository.save(createRecruitment);

        //반복 주기 저장
        RepeatPeriod period1 = RepeatPeriod.createRepeatPeriod(Period.MONTH, Week.FIRST, Day.MON);
        period1.setRecruitment(saveRecruitment);
        RepeatPeriod period2 = RepeatPeriod.createRepeatPeriod(Period.MONTH, Week.FIRST, Day.TUES);
        period2.setRecruitment(saveRecruitment);
        repeatPeriodRepository.save(period1);
        repeatPeriodRepository.save(period2);
    }

    @Test
    @Transactional
    public void 정기모집글_삭제_성공(){
        //given & when
        recruitmentService.deleteRecruitment(saveRecruitment.getRecruitmentNo());
        clear();

        //then
        Recruitment recruitment = recruitmentRepository.findById(saveRecruitment.getRecruitmentNo()).get();
        assertThat(recruitment.getIsDeleted()).isEqualTo(IsDeleted.Y);
    }

    @Test
    @Transactional
    public void 정기모집글_삭제_실패_없는모집글(){
        assertThatThrownBy(() -> recruitmentService.deleteRecruitment(Long.MAX_VALUE)) //없는 모집글 PK
                .isInstanceOf(BusinessException.class);
    }

    @Disabled
    @DisplayName("봉사 모집글 시 권한이 없는 경우 예외가 발생한다.")
    @Test
    @Transactional
    public void 정기모집글_삭제_권한예외(){
        //given
        User createUser = User.createUser("test", "test", "test", "test", Gender.M, LocalDate.now(), "test",
                true, true, true, Role.USER, "kakao", "test", null);
        userRepository.save(createUser);
        clear();

        //when & then
        assertThatThrownBy(() -> recruitmentService.deleteRecruitment(saveRecruitment.getRecruitmentNo()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("FORBIDDEN_RECRUITMENT");
    }

}