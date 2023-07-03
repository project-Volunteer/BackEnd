package project.volunteer.domain.notice.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.notice.application.dto.NoticeDetails;
import project.volunteer.domain.notice.application.dto.NoticeList;
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
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.Coordinate;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.common.component.Timetable;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class NoticeDtoServiceImplTest {

    @Autowired EntityManager em;
    @Autowired UserRepository userRepository;
    @Autowired RecruitmentRepository recruitmentRepository;
    @Autowired NoticeRepository noticeRepository;
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
                Address.createAddress("11", "1111","details"), Coordinate.createCoordinate(3.2F, 3.2F),
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
        List<NoticeList> findDtos = noticeDtoService.findNoticeDtos(saveRecruitment.getRecruitmentNo(), loginUser.getUserNo());

        //then
        assertThat(findDtos.size()).isEqualTo(3);
        for(NoticeList dto : findDtos){
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
        NoticeDetails noticeDto = noticeDtoService.findNoticeDto(saveRecruitment.getRecruitmentNo(), notice.getNoticeNo(), loginUser.getUserNo());

        //then
        assertThat(noticeDto.getNotice().getContent()).isEqualTo(content);
        assertThat(noticeDto.getNotice().getIsChecked()).isFalse();
        assertThat(noticeDto.getNotice().getCheckCnt()).isEqualTo(0);
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
}