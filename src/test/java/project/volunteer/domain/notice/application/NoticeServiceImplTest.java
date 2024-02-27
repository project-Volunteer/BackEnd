package project.volunteer.domain.notice.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.confirmation.dao.ConfirmationRepository;
import project.volunteer.domain.notice.api.dto.request.NoticeAdd;
import project.volunteer.domain.notice.api.dto.request.NoticeEdit;
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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class NoticeServiceImplTest {
    @PersistenceContext EntityManager em;
    @Autowired UserRepository userRepository;
    @Autowired
    RecruitmentRepository recruitmentRepository;
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
        Recruitment createRecruitment = new Recruitment("title", "content", VolunteeringCategory.EDUCATION, VolunteeringType.REG,
                VolunteerType.ADULT, 9999,0,true, "unicef",
                new Address("111", "11", "test", "test"),
                new Coordinate(1.2F, 2.2F),
                new Timetable(LocalDate.of(2024, 1, 10), LocalDate.of(2024, 3, 3), HourFormat.AM,
                        LocalTime.now(), 10),
                0, 0, true, IsDeleted.N, writerUser);
        saveRecruitment = recruitmentRepository.save(createRecruitment);
    }

    @Test
    @DisplayName("봉사 모집글 공지사항 등록에 성공하다.")
    public void addNotice(){
        //given
        NoticeAdd dto = new NoticeAdd("test");

        //when
        Notice saveNotice = noticeService.addNotice(saveRecruitment, dto);
        clear();

        //then
        Notice findNotice = noticeRepository.findById(saveNotice.getNoticeNo()).get();
        assertThat(findNotice.getContent()).isEqualTo(saveNotice.getContent());
        assertThat(findNotice.getIsDeleted()).isEqualTo(IsDeleted.N);
    }

    @Test
    @DisplayName("봉사 모집글 공지사항 수정에 성공하다.")
    public void editNotice(){
        //given
        Notice saveNotice = 공지사항_등록("test");
        NoticeEdit dto = new NoticeEdit("change");

        //when
        noticeService.editNotice(saveNotice.getNoticeNo(), dto);
        clear();

        //then
        Notice findNotice = noticeRepository.findById(saveNotice.getNoticeNo()).get();
        assertThat(findNotice.getContent()).isEqualTo(dto.getContent());
        assertThat(findNotice.getIsDeleted()).isEqualTo(IsDeleted.N);
    }

    @Test
    @DisplayName("봉사 모집글 공지사항 삭제에 성공하다.")
    public void deleteNotice(){
        //given
        Notice saveNotice = 공지사항_등록("test");

        //when
        noticeService.deleteNotice(saveNotice.getNoticeNo());
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