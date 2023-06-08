package project.volunteer.domain.participation.application;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.participation.dao.ParticipantRepository;
import project.volunteer.domain.participation.domain.Participant;
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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@SpringBootTest
class ParticipationServiceImplTest {

    @PersistenceContext EntityManager em;
    @Autowired UserRepository userRepository;
    @Autowired RecruitmentRepository recruitmentRepository;
    @Autowired ParticipantRepository participantRepository;
    @Autowired ParticipationService participationService;

    private Recruitment saveRecruitment;
    private User writer;

    @BeforeEach
    public void init(){
        //로그인 사용자 저장
        User writerUser = User.createUser("1234", "1234", "1234", "1234", Gender.M, LocalDate.now(), "1234",
                true, true, true, Role.USER, "kakao", "1234", null);
        writer = userRepository.save(writerUser);

        //모집글 저장
        Recruitment createRecruitment = Recruitment.createRecruitment("title", "content", VolunteeringCategory.CULTURAL_EVENT, VolunteeringType.IRREG,
                VolunteerType.TEENAGER, 3, true, "organization",
                Address.createAddress("11", "1111","details"), Coordinate.createCoordinate(3.2F, 3.2F),
                Timetable.createTimetable(LocalDate.now().plusMonths(3), LocalDate.now().plusMonths(3), HourFormat.AM, LocalTime.now(), 3), true);
        createRecruitment.setWriter(writer);
        saveRecruitment = recruitmentRepository.save(createRecruitment);

        clear();
    }
    private void clear() {
        em.flush();
        em.clear();
    }
    private User 사용자_등록(String username){
        User createUser = User.createUser(username, username, username, username, Gender.M, LocalDate.now(), "picture",
                true, true, true, Role.USER, "kakao", username, null);
        return userRepository.save(createUser);
    }
    private Participant 참여자_상태_등록(User user, State state){
        Participant participant = Participant.createParticipant(saveRecruitment, user, state);
        return participantRepository.save(participant);
    }

    @Test
    @Transactional
    public void 팀신청_최초_성공(){
        //given
        User saveUser = 사용자_등록("홍길동");

        //when
        participationService.participate(saveUser.getUserNo(), saveRecruitment.getRecruitmentNo());
        clear();

        //then
        Participant findParticipant =
                participantRepository.findByRecruitment_RecruitmentNoAndParticipant_UserNo(saveRecruitment.getRecruitmentNo(), saveUser.getUserNo()).get();
        Assertions.assertThat(findParticipant.getState()).isEqualTo(State.JOIN_REQUEST);
    }

    @Test
    @Transactional
    public void 팀신청_재신청_성공(){
        //given
        User saveUser = 사용자_등록("홍길동");
        Participant participant = 참여자_상태_등록(saveUser, State.JOIN_CANCEL);
        clear();

        //when
        participationService.participate(saveUser.getUserNo(), saveRecruitment.getRecruitmentNo());

        //then
        Participant findParticipant = participantRepository.findByRecruitment_RecruitmentNoAndParticipant_UserNo(saveRecruitment.getRecruitmentNo(),
                saveUser.getUserNo()).get();
        Assertions.assertThat(findParticipant.getState()).isEqualTo(State.JOIN_REQUEST);
    }

    @Test
    @Transactional
    public void 팀신청_실패_중복신청(){
        //given
        User saveUser = 사용자_등록("홍길동");
        Participant participant = 참여자_상태_등록(saveUser, State.JOIN_REQUEST);
        clear();

        //when && then
        Assertions.assertThatThrownBy(() -> participationService.participate(saveUser.getUserNo(), saveRecruitment.getRecruitmentNo()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("DUPLICATE_PARTICIPATION");
    }

    @Test
    @Transactional
    public void 팀신청_실패_없는모집글(){
        //given
        User saveUser = 사용자_등록("홍길동");

        //when & then
        Assertions.assertThatThrownBy(() -> participationService.participate(saveUser.getUserNo(), Long.MAX_VALUE))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("NOT_EXIST_RECRUITMENT");
    }

    @Test
    @Transactional
    public void 팀신청_실패_삭제된모집글(){
        //given
        User saveUser = 사용자_등록("홍길동");
        Recruitment findSaveRecruitment = recruitmentRepository.findById(saveRecruitment.getRecruitmentNo()).get();
        findSaveRecruitment.setDeleted();
        clear();

        //when & then
        Assertions.assertThatThrownBy(() -> participationService.participate(saveUser.getUserNo(), saveRecruitment.getRecruitmentNo()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("NOT_EXIST_RECRUITMENT");
    }

    @Test
    @Transactional
    public void 팀신청_실패_종료된모집글(){
        //given
        User saveUser = 사용자_등록("홍길동");
        Timetable newTime = Timetable.builder()
                .hourFormat(HourFormat.AM)
                .progressTime(3)
                .startTime(LocalTime.now())
                .startDay(LocalDate.of(2023, 5, 13))
                .endDay(LocalDate.of(2023, 5, 14)) //봉사 활동 종료
                .build();
        Recruitment findSaveRecruitment = recruitmentRepository.findById(saveRecruitment.getRecruitmentNo()).get();
        findSaveRecruitment.setVolunteeringTimeTable(newTime);
        clear();

        //when & then
        Assertions.assertThatThrownBy(() -> participationService.participate(saveUser.getUserNo(), saveRecruitment.getRecruitmentNo()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("NOT_EXIST_RECRUITMENT");
    }

    @Test
    @Transactional
    public void 팀신청_실패_참여가능인원초과(){
        //given
        User saveUser1 = 사용자_등록("홍길동");
        User saveUser2 = 사용자_등록("구본식");
        User saveUser3 = 사용자_등록("구길동");
        User saveUser4 = 사용자_등록("박하선");
        참여자_상태_등록(saveUser1, State.JOIN_APPROVAL);
        참여자_상태_등록(saveUser2, State.JOIN_APPROVAL);
        참여자_상태_등록(saveUser3, State.JOIN_APPROVAL);
        clear();

        //when & then
        Assertions.assertThatThrownBy(() -> participationService.participate(saveUser4.getUserNo(), saveRecruitment.getRecruitmentNo()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("INSUFFICIENT_CAPACITY");
    }

    @Test
    @Transactional
    public void 팀신청취소_성공(){
        //given
        User saveUser = 사용자_등록("홍길동");
        참여자_상태_등록(saveUser, State.JOIN_REQUEST);

        //when
        participationService.cancelParticipation(saveUser.getUserNo(), saveRecruitment.getRecruitmentNo());
        clear();

        //then
        Participant participant = participantRepository.findByRecruitment_RecruitmentNoAndParticipant_UserNo(
                saveRecruitment.getRecruitmentNo(), saveUser.getUserNo()).get();
        Assertions.assertThat(participant.getState()).isEqualTo(State.JOIN_CANCEL);
    }

    @Test
    @Transactional
    public void 팀신청취소_실패_잘못된상태(){
        //given
        User saveUser = 사용자_등록("홍길동");
        참여자_상태_등록(saveUser, State.JOIN_APPROVAL);
        clear();

        //when & then
        Assertions.assertThatThrownBy(() -> participationService.cancelParticipation(saveUser.getUserNo(), saveRecruitment.getRecruitmentNo()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("INVALID_STATE");
    }

    @Test
    @Transactional
    public void 팀신청승인_성공(){
        //given
        User saveUser1 = 사용자_등록("홍길동");
        User saveUser2 = 사용자_등록("구본식");
        User saveUser3 = 사용자_등록("구길동");
        참여자_상태_등록(saveUser1, State.JOIN_REQUEST);
        참여자_상태_등록(saveUser2, State.JOIN_REQUEST);
        참여자_상태_등록(saveUser3, State.JOIN_REQUEST);
        List<Long> requestNos = List.of(saveUser1.getUserNo(), saveUser2.getUserNo(), saveUser3.getUserNo());

        //when
        participationService.approvalParticipant(writer.getUserNo(), saveRecruitment.getRecruitmentNo(), requestNos);
        clear();

        //then
        List<Participant> participantList =
                participantRepository.findByRecruitment_RecruitmentNoAndParticipant_UserNoIn(saveRecruitment.getRecruitmentNo(), requestNos);
        participantList.stream()
                .forEach(p -> Assertions.assertThat(p.getState()).isEqualTo(State.JOIN_APPROVAL));
    }

    @Test
    @Transactional
    public void 팀신청승인_실패_권한없음(){
        //given
        User saveUser1 = 사용자_등록("홍길동");
        User saveUser2 = 사용자_등록("구본식");
        User saveUser3 = 사용자_등록("구길동");
        참여자_상태_등록(saveUser1, State.JOIN_REQUEST);
        참여자_상태_등록(saveUser2, State.JOIN_REQUEST);
        참여자_상태_등록(saveUser3, State.JOIN_REQUEST);
        List<Long> requestNos = List.of(saveUser1.getUserNo(), saveUser2.getUserNo(), saveUser3.getUserNo());
        clear();

        //when & then
        Assertions.assertThatThrownBy(
                () -> participationService.approvalParticipant(saveUser1.getUserNo(), saveRecruitment.getRecruitmentNo(), requestNos))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("FORBIDDEN_RECRUITMENT");
    }

    @Test
    @Transactional
    public void 팀신청승인_실패_잘못된상태(){
        //given
        User saveUser1 = 사용자_등록("홍길동");
        참여자_상태_등록(saveUser1, State.JOIN_APPROVAL);
        List<Long> requestNos = List.of(saveUser1.getUserNo());
        clear();

        //when & then
        Assertions.assertThatThrownBy(
                () -> participationService.approvalParticipant(writer.getUserNo(), saveRecruitment.getRecruitmentNo(),requestNos))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("INVALID_STATE");
    }

    @Test
    @Transactional
    public void 팀신청승인_실패_승인가능인원초과(){
        //given
        //남은 승인 가능한 인원 1명
        User saveUser1 = 사용자_등록("홍길동");
        User saveUser2 = 사용자_등록("구본식");
        User saveUser3 = 사용자_등록("구길동");
        User saveUser4 = 사용자_등록("구혜선");
        참여자_상태_등록(saveUser1, State.JOIN_APPROVAL);
        참여자_상태_등록(saveUser2, State.JOIN_APPROVAL);
        참여자_상태_등록(saveUser3, State.JOIN_REQUEST);
        참여자_상태_등록(saveUser4, State.JOIN_REQUEST);
        List<Long> requestNos = List.of(saveUser3.getUserNo(), saveUser4.getUserNo());
        clear();

        //when & then
        Assertions.assertThatThrownBy(() -> participationService.approvalParticipant(writer.getUserNo(), saveRecruitment.getRecruitmentNo(), requestNos))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("INSUFFICIENT_APPROVAL_CAPACITY");
    }

    @Test
    @Transactional
    public void 팀원강제탈퇴_성공(){
        //given
        User saveUser = 사용자_등록("홍길동");
        참여자_상태_등록(saveUser, State.JOIN_APPROVAL);

        //when
        participationService.deportParticipant(writer.getUserNo(), saveRecruitment.getRecruitmentNo(),saveUser.getUserNo());
        clear();

        //then
        Participant participant = participantRepository.findByRecruitment_RecruitmentNoAndParticipant_UserNo(saveRecruitment.getRecruitmentNo(),
                saveUser.getUserNo()).get();
        Assertions.assertThat(participant.getState()).isEqualTo(State.DEPORT);
    }

    @Test
    @Transactional
    public void 팀원강제탈퇴_실패_잘못된상태(){
        //given
        User saveUser = 사용자_등록("홍길동");
        참여자_상태_등록(saveUser, State.JOIN_REQUEST);

        //when & then
        Assertions.assertThatThrownBy(() ->
                participationService.deportParticipant(writer.getUserNo(), saveRecruitment.getRecruitmentNo(), saveUser.getUserNo()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("INVALID_STATE");
    }

}