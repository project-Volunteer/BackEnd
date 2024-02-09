package project.volunteer.domain.confirmation.application;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.confirmation.dao.ConfirmationRepository;
import project.volunteer.domain.confirmation.domain.Confirmation;
import project.volunteer.domain.notice.dao.NoticeRepository;
import project.volunteer.domain.notice.domain.Notice;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.repository.RecruitmentRepository;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.*;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ConfirmationServiceImplTest {

    @PersistenceContext EntityManager em;
    @Autowired UserRepository userRepository;
    @Autowired RecruitmentRepository recruitmentRepository;
    @Autowired NoticeRepository noticeRepository;
    @Autowired ConfirmationRepository confirmationRepository;
    @Autowired ConfirmationService confirmationService;

    Recruitment saveRecruitment;
    @BeforeEach
    void init(){
        //작성자 저장
        User writerUser = User.createUser("1234", "1234", "1234", "1234", Gender.M, LocalDate.now(), "1234",
                true, true, true, Role.USER, "kakao", "1234", null);
        userRepository.save(writerUser);

        //모집글 저장
        Recruitment createRecruitment = new Recruitment(0L, "title", "content", VolunteeringCategory.EDUCATION, VolunteeringType.REG,
                VolunteerType.ADULT, 9999,0,true, "unicef",
                new Address("111", "11", "test", "test"),
                new Coordinate(1.2F, 2.2F),
                new Timetable(LocalDate.of(2024, 1, 10), LocalDate.of(2024, 3, 3), HourFormat.AM,
                        LocalTime.now(), 10),
                0, 0, true, IsDeleted.N, writerUser, null);
        saveRecruitment = recruitmentRepository.save(createRecruitment);
    }

    @Test
    @DisplayName("읽음 등록에 성공하다.")
    public void addConfirmation(){
        //given
        User loginUser = 사용자_추가("read user");
        Notice saveNotice = 공지사항_등록("read test");

        //when
        confirmationService.addConfirmation(loginUser, RealWorkCode.NOTICE, saveNotice.getNoticeNo());
        clear();

        //then
        Confirmation findConfirmation = confirmationRepository.findConfirmation(loginUser.getUserNo(), RealWorkCode.NOTICE, saveNotice.getNoticeNo()).get();

        assertThat(findConfirmation.getRealWorkCode()).isEqualTo(RealWorkCode.NOTICE);
        assertThat(findConfirmation.getNo()).isEqualTo(saveNotice.getNoticeNo());
        assertThat(findConfirmation.getUser().getUserNo()).isEqualTo(loginUser.getUserNo());
    }

    @Test
    @DisplayName("읽음을 중복으로 시도하다.")
    public void duplicatedConfirmation(){
        //given
        User loginUser = 사용자_추가("read user");
        Notice saveNotice = 공지사항_등록("read test");
        읽음_등록(RealWorkCode.NOTICE, saveNotice.getNoticeNo(), loginUser);
        clear();

        //when & then
        assertThatThrownBy(() -> confirmationService.addConfirmation(loginUser, RealWorkCode.NOTICE, saveNotice.getNoticeNo()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.INVALID_CONFIRMATION.name());
    }

    @Test
    @DisplayName("읽음 해제(삭제)에 성공하다.")
    public void deleteConfirmation(){
        //given
        User loginUser = 사용자_추가("read user");
        Notice saveNotice = 공지사항_등록("read test");
        읽음_등록(RealWorkCode.NOTICE, saveNotice.getNoticeNo(), loginUser);

        //when
        confirmationService.deleteConfirmation(loginUser.getUserNo(), RealWorkCode.NOTICE, saveNotice.getNoticeNo());
        clear();

        //then
        Optional<Confirmation> confirmation = confirmationRepository.findConfirmation(loginUser.getUserNo(), RealWorkCode.NOTICE, saveNotice.getNoticeNo());
        assertThat(confirmation).isEqualTo(Optional.empty());
    }

    @Test
    @DisplayName("읽음이 존재하지 않는 상황에 읽음 해제를 시도하다.")
    public void notExistConfirmation(){
        //given
        final String userName = "user";
        final String content = "test";
        User loginUser = 사용자_추가(userName);
        Notice saveNotice = 공지사항_등록(content);

        //when & then
        assertThatThrownBy(() -> confirmationService.deleteConfirmation(loginUser.getUserNo(), RealWorkCode.NOTICE, saveNotice.getNoticeNo()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.NOT_EXIST_CONFIRMATION.name());
    }

    @Test
    @DisplayName("공지사항 리스트에 해당하는 모든 읽음 정보를 삭제하다.")
    public void deleteAllConfirmation(){
        //given
        Notice saveNotice1 = 공지사항_등록("test1");
        Notice saveNotice2 = 공지사항_등록("test2");
        User user1 = 사용자_추가("user1");
        User user2 = 사용자_추가("user2");
        User user3 = 사용자_추가("user2");

        읽음_등록(RealWorkCode.NOTICE, saveNotice1.getNoticeNo(), user1);
        읽음_등록(RealWorkCode.NOTICE, saveNotice1.getNoticeNo(), user2);
        읽음_등록(RealWorkCode.NOTICE, saveNotice1.getNoticeNo(), user3);
        읽음_등록(RealWorkCode.NOTICE, saveNotice2.getNoticeNo(), user1);
        읽음_등록(RealWorkCode.NOTICE, saveNotice2.getNoticeNo(), user2);
        읽음_등록(RealWorkCode.NOTICE, saveNotice2.getNoticeNo(), user3);
        List<Long> nos = List.of(saveNotice1.getNoticeNo(), saveNotice2.getNoticeNo());

        //when
        confirmationService.deleteAllConfirmation(RealWorkCode.NOTICE, nos);
        clear();

        //then
        List<Confirmation> result = confirmationRepository.findByRealWorkCodeAndNoIn(RealWorkCode.NOTICE, nos);
        Assertions.assertThat(result.size()).isEqualTo(0);
    }

    private User 사용자_추가(String value){
        User loginUser = User.createUser(value, value, value, value, Gender.M, LocalDate.now(), "picture",
                true, true, true, Role.USER, "kakao", value, null);
        return userRepository.save(loginUser);
    }
    private Notice 공지사항_등록(String content){
        Notice createNotice = Notice.createNotice(content);
        return noticeRepository.save(createNotice);
    }
    private Confirmation 읽음_등록(RealWorkCode code, Long no, User user){
        Confirmation createConfirmation = Confirmation.createConfirmation(code, no);
        createConfirmation.setUser(user);
        return confirmationRepository.save(createConfirmation);
    }
    private void clear() {
        em.flush();
        em.clear();
    }
}