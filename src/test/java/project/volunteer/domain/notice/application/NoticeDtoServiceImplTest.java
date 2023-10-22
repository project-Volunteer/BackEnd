package project.volunteer.domain.notice.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.confirmation.dao.ConfirmationRepository;
import project.volunteer.domain.confirmation.domain.Confirmation;
import project.volunteer.domain.notice.application.dto.NoticeDetails;
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

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
class NoticeDtoServiceImplTest {

    @Autowired EntityManager em;
    @Autowired UserRepository userRepository;
    @Autowired RecruitmentRepository recruitmentRepository;
    @Autowired NoticeRepository noticeRepository;
    @Autowired ConfirmationRepository confirmationRepository;
    @Autowired NoticeDtoService noticeDtoService;

    User writer;
    Recruitment saveRecruitment;
    @BeforeEach
    void init(){
        //작성자 저장
        User writerUser = User.createUser("1234", "1234", "1234", "1234", Gender.M, LocalDate.now(), "picture",
                true, true, true, Role.USER, "kakao", "1234", null);
        writer = userRepository.save(writerUser);

        //모집글 저장
        Recruitment createRecruitment = Recruitment.createRecruitment("title", "content", VolunteeringCategory.CULTURAL_EVENT, VolunteeringType.IRREG,
                VolunteerType.TEENAGER, 3, true, "organization",
                Address.createAddress("11", "1111","details", "fullName"), Coordinate.createCoordinate(3.2F, 3.2F),
                Timetable.createTimetable(LocalDate.now(), LocalDate.now().plusMonths(3), HourFormat.AM, LocalTime.now(), 3), true);
        createRecruitment.setWriter(writer);
        saveRecruitment = recruitmentRepository.save(createRecruitment);
    }

    @Test
    @DisplayName("공지사항 리스트 조회에 성공하다.")
    @Transactional
    public void findNoticeDtos(){
        //given
        User loginUser = 사용자_추가("wow");
        Notice notice1 = 공지사항_등록("notice1", saveRecruitment);
        Notice notice2 = 공지사항_등록("notice2", saveRecruitment);
        Notice notice3 = 공지사항_등록("notice3", saveRecruitment);
        clear();

        //when
        List<NoticeDetails> findDtos = noticeDtoService.findNoticeDtos(saveRecruitment.getRecruitmentNo(), loginUser.getUserNo());

        //then
        assertThat(findDtos.size()).isEqualTo(3);
        for(NoticeDetails dto : findDtos){
            assertThat(dto.getIsChecked()).isFalse();
        }
    }

    @Test
    @DisplayName("공지사항 상세 조회에 성공하다.")
    @Transactional
    public void findNoticeDetails(){
        //given
        final String content = "notice1";
        User loginUser = 사용자_추가("wow");
        Notice notice = 공지사항_등록(content, saveRecruitment);
        clear();

        //when
        NoticeDetails noticeDto = noticeDtoService.findNoticeDto(notice.getNoticeNo(), loginUser.getUserNo());

        //then
        assertThat(noticeDto.getContent()).isEqualTo(content);
        assertThat(noticeDto.getIsChecked()).isFalse();
        assertThat(noticeDto.getCheckCnt()).isEqualTo(0);
    }

    @Test
    @DisplayName("읽음 확인한 공지사항 상세 조회에 성공하다.")
    @Transactional
    public void findNoticeDetails_read(){
        //given
        final String content = "notice";
        User loginUser = 사용자_추가("wow");
        Notice notice = 공지사항_등록(content, saveRecruitment);
        읽음_등록(RealWorkCode.NOTICE, notice.getNoticeNo(), loginUser);
        notice.increaseCheckNum();
        clear();

        //when
        NoticeDetails noticeDto = noticeDtoService.findNoticeDto(notice.getNoticeNo(), loginUser.getUserNo());

        //then
        assertThat(noticeDto.getIsChecked()).isTrue();
        assertThat(noticeDto.getCheckCnt()).isEqualTo(1);
    }

    @Test
    @DisplayName("3개 중 2개를 읽음 확인한 공지사항 리스트 조회에 성공하다.")
    @Transactional
    public void findNoticeDetails_readCancel(){
        //given
        User loginUser = 사용자_추가("wow");

        Notice notice1 = 공지사항_등록("notice1", saveRecruitment);
        읽음_등록(RealWorkCode.NOTICE, notice1.getNoticeNo(), loginUser);
        notice1.increaseCheckNum();

        Notice notice2 = 공지사항_등록("notice2", saveRecruitment);
        읽음_등록(RealWorkCode.NOTICE, notice2.getNoticeNo(), loginUser);
        notice2.increaseCheckNum();

        Notice notice3 = 공지사항_등록("notice3", saveRecruitment);
        clear();

        //when
        List<NoticeDetails> findDtos = noticeDtoService.findNoticeDtos(saveRecruitment.getRecruitmentNo(), loginUser.getUserNo());

        //then
        assertAll(
                () -> assertThat(findDtos.size()).isEqualTo(3),
                () -> assertThat(findDtos.get(0).getCheckCnt()).isEqualTo(1),
                () -> assertThat(findDtos.get(0).getIsChecked()).isTrue(),
                () -> assertThat(findDtos.get(1).getCheckCnt()).isEqualTo(1),
                () -> assertThat(findDtos.get(1).getIsChecked()).isTrue(),
                () -> assertThat(findDtos.get(2).getCheckCnt()).isEqualTo(0),
                () -> assertThat(findDtos.get(2).getIsChecked()).isFalse()
        );
    }


    private void clear() {
        em.flush();
        em.clear();
    }
    private User 사용자_추가(String value){
        User loginUser = User.createUser(value, value, value, value, Gender.M, LocalDate.now(), "picture",
                true, true, true, Role.USER, "kakao", value, null);
        return userRepository.save(loginUser);
    }
    private Notice 공지사항_등록(String content, Recruitment recruitment){
        Notice createNotice = Notice.createNotice(content);
        createNotice.setRecruitment(recruitment);
        return noticeRepository.save(createNotice);
    }
    private Confirmation 읽음_등록(RealWorkCode code, Long no, User user){
        Confirmation createConfirmation = Confirmation.createConfirmation(code, no);
        createConfirmation.setUser(user);
        return confirmationRepository.save(createConfirmation);
    }
}