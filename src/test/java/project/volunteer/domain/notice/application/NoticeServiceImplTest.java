package project.volunteer.domain.notice.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.notice.api.dto.NoticeAdd;
import project.volunteer.domain.notice.api.dto.NoticeEdit;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class NoticeServiceImplTest {
    @PersistenceContext EntityManager em;
    @Autowired UserRepository userRepository;
    @Autowired RecruitmentRepository recruitmentRepository;
    @Autowired NoticeRepository noticeRepository;
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


    private Notice 공지사항_등록(String content){
        Notice createNotice = Notice.createNotice(content);
        return noticeRepository.save(createNotice);
    }
    private void clear() {
        em.flush();
        em.clear();
    }
}