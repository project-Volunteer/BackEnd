package project.volunteer.domain.notice.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.confirmation.dao.ConfirmationRepository;
import project.volunteer.domain.confirmation.domain.Confirmation;
import project.volunteer.domain.notice.api.dto.request.NoticeAdd;
import project.volunteer.domain.notice.api.dto.request.NoticeEdit;
import project.volunteer.domain.notice.dao.NoticeRepository;
import project.volunteer.domain.notice.domain.Notice;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class NoticeServiceImplTest {
    @PersistenceContext EntityManager em;
    @Autowired UserRepository userRepository;
    @Autowired RecruitmentRepository recruitmentRepository;
    @Autowired NoticeRepository noticeRepository;
    @Autowired ConfirmationRepository confirmationRepository;
    @Autowired NoticeService noticeService;

    Recruitment saveRecruitment;
    @BeforeEach
    void init(){
        //작성자 저장
        User writerUser = User.createUser("1234", "1234", "1234", "1234", Gender.M, LocalDate.now(), "1234",
                true, true, true, Role.USER, "kakao", "1234", null);
        userRepository.save(writerUser);

        //모집글 저장
        Recruitment createRecruitment = Recruitment.createRecruitment("title", "content", VolunteeringCategory.CULTURAL_EVENT, VolunteeringType.IRREG,
                VolunteerType.TEENAGER, 3, true, "organization",
                Address.createAddress("11", "1111","details"), Coordinate.createCoordinate(3.2F, 3.2F),
                Timetable.createTimetable(LocalDate.now(), LocalDate.now().plusMonths(3), HourFormat.AM, LocalTime.now(), 3), true);
        createRecruitment.setWriter(writerUser);
        saveRecruitment = recruitmentRepository.save(createRecruitment);
    }

    @Test
    @Transactional
    @DisplayName("봉사 모집글 공지사항 등록에 성공하다.")
    public void addNotice(){
        //given
        NoticeAdd dto = new NoticeAdd("test");

        //when
        Notice saveNotice = noticeService.addNotice(saveRecruitment.getRecruitmentNo(), dto);
        clear();

        //then
        Notice findNotice = noticeRepository.findById(saveNotice.getNoticeNo()).get();
        assertThat(findNotice.getContent()).isEqualTo(saveNotice.getContent());
        assertThat(findNotice.getIsDeleted()).isEqualTo(IsDeleted.N);
    }

    @Test
    @Transactional
    @DisplayName("봉사 모집글 기간 종료로 인해 공지사항 등록에 실패하다.")
    public void expiredPeriodRecruitment(){
        //given
        Timetable updateTimetable = Timetable.createTimetable(LocalDate.now().minusMonths(1), LocalDate.now().minusDays(1), HourFormat.AM, LocalTime.now(), 3);
        saveRecruitment.setVolunteeringTimeTable(updateTimetable);
        NoticeAdd dto = new NoticeAdd("test");
        clear();

        //when & then
        assertThatThrownBy(() -> noticeService.addNotice(saveRecruitment.getRecruitmentNo(), dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.EXPIRED_PERIOD_NOTICE.name());
    }

    @Test
    @Transactional
    @DisplayName("봉사 모집글 공지사항 수정에 성공하다.")
    public void editNotice(){
        //given
        Notice saveNotice = 공지사항_등록("test");
        NoticeEdit dto = new NoticeEdit("change");

        //when
        noticeService.editNotice(saveRecruitment.getRecruitmentNo(), saveNotice.getNoticeNo(), dto);
        clear();

        //then
        Notice findNotice = noticeRepository.findById(saveNotice.getNoticeNo()).get();
        assertThat(findNotice.getContent()).isEqualTo(dto.getContent());
        assertThat(findNotice.getIsDeleted()).isEqualTo(IsDeleted.N);
    }

    @Test
    @Transactional
    @DisplayName("봉사 모집글 공지사항 삭제에 성공하다.")
    public void deleteNotice(){
        //given
        Notice saveNotice = 공지사항_등록("test");

        //when
        noticeService.deleteNotice(saveRecruitment.getRecruitmentNo(), saveNotice.getNoticeNo());
        clear();

        //then
        Notice findNotice = noticeRepository.findById(saveNotice.getNoticeNo()).get();
        assertThat(findNotice.getIsDeleted()).isEqualTo(IsDeleted.Y);
    }

    @Test
    @Transactional
    @DisplayName("공지사항 읽음에 성공하다.")
    public void readNotice(){
        //given
        User loginUser = 사용자_추가("read user");
        Notice saveNotice = 공지사항_등록("read test");

        //when
        noticeService.readNotice(saveRecruitment.getRecruitmentNo(), saveNotice.getNoticeNo(), loginUser.getUserNo());
        clear();

        //then
        Notice findNotice = noticeRepository.findById(saveNotice.getNoticeNo()).get();
        Confirmation findConfirmation = confirmationRepository.findConfirmation(loginUser.getUserNo(), RealWorkCode.NOTICE, saveNotice.getNoticeNo()).get();

        assertThat(findNotice.getCheckedNum()).isEqualTo(1);
        assertThat(findConfirmation.getRealWorkCode()).isEqualTo(RealWorkCode.NOTICE);
        assertThat(findConfirmation.getNo()).isEqualTo(saveNotice.getNoticeNo());
        assertThat(findConfirmation.getUser().getUserNo()).isEqualTo(loginUser.getUserNo());
    }

    @Test
    @Transactional
    @DisplayName("공지사항 읽음을 중복으로 시도하다.")
    public void duplicatedReadNotice(){
        //given
        User loginUser = 사용자_추가("read user");
        Notice saveNotice = 공지사항_등록("read test");
        읽음_등록(RealWorkCode.NOTICE, saveNotice.getNoticeNo(), loginUser);
        saveNotice.increaseCheckNum();
        clear();

        //when & then
        assertThatThrownBy(() -> noticeService.readNotice(saveRecruitment.getRecruitmentNo(), saveNotice.getNoticeNo(), loginUser.getUserNo()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.INVALID_CONFIRMATION.name());
    }

    @Test
    @Transactional
    @DisplayName("공지사항 읽음 해제에 성공하다.")
    public void readCancelNotice(){
        //given
        User loginUser = 사용자_추가("read user");
        Notice saveNotice = 공지사항_등록("read test");
        읽음_등록(RealWorkCode.NOTICE, saveNotice.getNoticeNo(), loginUser);
        saveNotice.increaseCheckNum();

        //when
        noticeService.readCancelNotice(saveRecruitment.getRecruitmentNo(), saveNotice.getNoticeNo(), loginUser.getUserNo());
        clear();

        //then
        Notice findNotice = noticeRepository.findById(saveNotice.getNoticeNo()).get();
        Optional<Confirmation> findConfirmation = confirmationRepository.findConfirmation(loginUser.getUserNo(), RealWorkCode.NOTICE, saveNotice.getNoticeNo());

        assertThat(findNotice.getCheckedNum()).isEqualTo(0);
        assertThat(findConfirmation).isEqualTo(Optional.empty());
    }

    @Test
    @Transactional
    @DisplayName("공지사항 읽음이 존재하지 않는 상황에 읽음 해제를 시도하다.")
    public void notExistConfirmation(){
        //given
        final String userName = "user";
        final String content = "test";
        User loginUser = 사용자_추가(userName);
        Notice saveNotice = 공지사항_등록(content);

        //when & then
        assertThatThrownBy(() -> noticeService.readCancelNotice(saveRecruitment.getRecruitmentNo(), saveNotice.getNoticeNo(), loginUser.getUserNo()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.NOT_EXIST_CONFIRMATION.name());
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