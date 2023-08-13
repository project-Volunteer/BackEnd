package project.volunteer.domain.participation.application;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
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
import project.volunteer.global.common.dto.StateResponse;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
                VolunteerType.TEENAGER, 4, true, "organization",
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
    private Participant 참여자_상태_등록(User user, ParticipantState state){
        Participant participant = Participant.createParticipant(saveRecruitment, user, state);
        return participantRepository.save(participant);
    }
    private Recruitment 저장된_모집글_가져오기(){
        return recruitmentRepository.findById(saveRecruitment.getRecruitmentNo()).get();
    }
    private Participant 사용자및참여자상태추가(String signName, ParticipantState state){
        //신규 사용자 가입
        User newUser= User.createUser(signName, "password", signName, "test@naver.com", Gender.M, LocalDate.now(), "picture",
                true, true, true, Role.USER, "kakao", signName, null);
        User saveUser = userRepository.save(newUser);

        //봉사 팀원 등록
        Participant participant = Participant.createParticipant(saveRecruitment, saveUser, state);
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
        assertThat(findParticipant.getState()).isEqualTo(ParticipantState.JOIN_REQUEST);
    }

    @Test
    @Transactional
    public void 팀신청_재신청_성공(){
        //given
        User saveUser = 사용자_등록("홍길동");
        Participant participant = 참여자_상태_등록(saveUser, ParticipantState.JOIN_CANCEL);
        clear();

        //when
        participationService.participate(saveUser.getUserNo(), saveRecruitment.getRecruitmentNo());

        //then
        Participant findParticipant = participantRepository.findByRecruitment_RecruitmentNoAndParticipant_UserNo(saveRecruitment.getRecruitmentNo(),
                saveUser.getUserNo()).get();
        assertThat(findParticipant.getState()).isEqualTo(ParticipantState.JOIN_REQUEST);
    }

    @Test
    @Transactional
    public void 팀신청_실패_중복신청(){
        //given
        User saveUser = 사용자_등록("홍길동");
        Participant participant = 참여자_상태_등록(saveUser, ParticipantState.JOIN_REQUEST);
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
        Recruitment findRecruitment = 저장된_모집글_가져오기();
        findRecruitment.setDeleted();
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
        Recruitment findRecruitment = 저장된_모집글_가져오기();
        findRecruitment.setVolunteeringTimeTable(newTime);
        clear();

        //when & then
        Assertions.assertThatThrownBy(() -> participationService.participate(saveUser.getUserNo(), saveRecruitment.getRecruitmentNo()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.EXPIRED_PERIOD_RECRUITMENT.name());
    }

    @Test
    @Transactional
    public void 팀신청_실패_참여가능인원초과(){
        //given
        Recruitment findRecruitment = 저장된_모집글_가져오기();
        User saveUser1 = 사용자_등록("홍길동");
        User saveUser2 = 사용자_등록("구본식");
        User saveUser3 = 사용자_등록("구길동");
        User saveUser4 = 사용자_등록("박하선");
        User saveUser5 = 사용자_등록("박구선");
        참여자_상태_등록(saveUser1, ParticipantState.JOIN_APPROVAL);
        findRecruitment.increaseTeamMember();
        참여자_상태_등록(saveUser2, ParticipantState.JOIN_APPROVAL);
        findRecruitment.increaseTeamMember();
        참여자_상태_등록(saveUser3, ParticipantState.JOIN_APPROVAL);
        findRecruitment.increaseTeamMember();
        참여자_상태_등록(saveUser4, ParticipantState.JOIN_APPROVAL);
        findRecruitment.increaseTeamMember();
        clear();

        //when & then
        Assertions.assertThatThrownBy(() -> participationService.participate(saveUser5.getUserNo(), saveRecruitment.getRecruitmentNo()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("INSUFFICIENT_CAPACITY");
    }

    @Test
    @Transactional
    public void 팀신청취소_성공(){
        //given
        User saveUser = 사용자_등록("홍길동");
        참여자_상태_등록(saveUser, ParticipantState.JOIN_REQUEST);

        //when
        participationService.cancelParticipation(saveUser.getUserNo(), saveRecruitment.getRecruitmentNo());
        clear();

        //then
        Participant participant = participantRepository.findByRecruitment_RecruitmentNoAndParticipant_UserNo(
                saveRecruitment.getRecruitmentNo(), saveUser.getUserNo()).get();
        assertThat(participant.getState()).isEqualTo(ParticipantState.JOIN_CANCEL);
    }

    @Test
    @Transactional
    public void 팀신청취소_실패_잘못된상태(){
        //given
        User saveUser = 사용자_등록("홍길동");
        참여자_상태_등록(saveUser, ParticipantState.JOIN_APPROVAL);
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
        참여자_상태_등록(saveUser1, ParticipantState.JOIN_REQUEST);
        참여자_상태_등록(saveUser2, ParticipantState.JOIN_REQUEST);
        참여자_상태_등록(saveUser3, ParticipantState.JOIN_REQUEST);
        List<Long> requestNos = List.of(saveUser1.getUserNo(), saveUser2.getUserNo(), saveUser3.getUserNo());

        //when
        participationService.approvalParticipant(saveRecruitment.getRecruitmentNo(), requestNos);
        clear();

        //then
        Recruitment findRecruitment = recruitmentRepository.findById(saveRecruitment.getRecruitmentNo()).get();
        List<Participant> participantList =
                participantRepository.findByRecruitment_RecruitmentNoAndParticipant_UserNoIn(saveRecruitment.getRecruitmentNo(), requestNos);
        participantList.stream()
                .forEach(p -> assertThat(p.getState()).isEqualTo(ParticipantState.JOIN_APPROVAL));
        assertThat(findRecruitment.getCurrentVolunteerNum()).isEqualTo(3);
    }

    @Disabled
    @Test
    @Transactional
    public void 팀신청승인_실패_권한없음(){
        //given
        User saveUser1 = 사용자_등록("홍길동");
        User saveUser2 = 사용자_등록("구본식");
        User saveUser3 = 사용자_등록("구길동");
        참여자_상태_등록(saveUser1, ParticipantState.JOIN_REQUEST);
        참여자_상태_등록(saveUser2, ParticipantState.JOIN_REQUEST);
        참여자_상태_등록(saveUser3, ParticipantState.JOIN_REQUEST);
        List<Long> requestNos = List.of(saveUser1.getUserNo(), saveUser2.getUserNo(), saveUser3.getUserNo());
        clear();

        //when & then
        Assertions.assertThatThrownBy(
                () -> participationService.approvalParticipant(saveRecruitment.getRecruitmentNo(), requestNos))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("FORBIDDEN_RECRUITMENT");
    }

    @Test
    @Transactional
    public void 팀신청승인_실패_잘못된상태(){
        //given
        User saveUser1 = 사용자_등록("홍길동");
        참여자_상태_등록(saveUser1, ParticipantState.JOIN_APPROVAL);
        List<Long> requestNos = List.of(saveUser1.getUserNo());
        clear();

        //when & then
        Assertions.assertThatThrownBy(
                () -> participationService.approvalParticipant(saveRecruitment.getRecruitmentNo(),requestNos))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("INVALID_STATE");
    }

    @Test
    @Transactional
    public void 팀신청승인_실패_승인가능인원초과(){
        //given
        Recruitment findRecruitment = 저장된_모집글_가져오기();

        //남은 승인 가능한 인원 1명
        User saveUser1 = 사용자_등록("홍길동");
        User saveUser2 = 사용자_등록("구본식");
        User saveUser3 = 사용자_등록("구길동");
        User saveUser4 = 사용자_등록("구혜선");
        User saveUser5 = 사용자_등록("박구서");
        참여자_상태_등록(saveUser1, ParticipantState.JOIN_APPROVAL);
        findRecruitment.increaseTeamMember();
        참여자_상태_등록(saveUser2, ParticipantState.JOIN_APPROVAL);
        findRecruitment.increaseTeamMember();
        참여자_상태_등록(saveUser3, ParticipantState.JOIN_APPROVAL);
        findRecruitment.increaseTeamMember();

        참여자_상태_등록(saveUser4, ParticipantState.JOIN_REQUEST);
        참여자_상태_등록(saveUser5, ParticipantState.JOIN_REQUEST);
        List<Long> requestNos = List.of(saveUser3.getUserNo(), saveUser4.getUserNo());
        clear();

        //when & then
        Assertions.assertThatThrownBy(() -> participationService.approvalParticipant(saveRecruitment.getRecruitmentNo(), requestNos))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("INSUFFICIENT_APPROVAL_CAPACITY");
    }

    @Test
    @Transactional
    public void 팀원강제탈퇴_성공(){
        //given
        Recruitment recruitment = 저장된_모집글_가져오기();
        User saveUser = 사용자_등록("홍길동");
        참여자_상태_등록(saveUser, ParticipantState.JOIN_APPROVAL);
        recruitment.increaseTeamMember();

        //when
        participationService.deportParticipant(saveRecruitment.getRecruitmentNo(),saveUser.getUserNo());
        clear();

        //then
        Recruitment findRecruitment = recruitmentRepository.findById(saveRecruitment.getRecruitmentNo()).get();
        Participant participant = participantRepository.findByRecruitment_RecruitmentNoAndParticipant_UserNo(saveRecruitment.getRecruitmentNo(),
                saveUser.getUserNo()).get();
        assertThat(participant.getState()).isEqualTo(ParticipantState.DEPORT);
        assertThat(findRecruitment.getCurrentVolunteerNum()).isEqualTo(0);
    }

    @Test
    @Transactional
    public void 팀원강제탈퇴_실패_잘못된상태(){
        //given
        User saveUser = 사용자_등록("홍길동");
        참여자_상태_등록(saveUser, ParticipantState.JOIN_REQUEST);

        //when & then
        Assertions.assertThatThrownBy(() ->
                participationService.deportParticipant(saveRecruitment.getRecruitmentNo(), saveUser.getUserNo()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("INVALID_STATE");
    }

    //TODO: 퍼사드 메서드 테스트 코드로 리팩토링하기
//    @DisplayName("봉사 모집글 신청자/승인자 각 인원이 3명이 된다.")
//    @Test
//    @Transactional
//    public void searchParticipantState() throws IOException {
//        //given
//        봉사모집글_팀원_상태추가("홍길동", ParticipantState.JOIN_APPROVAL);
//        봉사모집글_팀원_상태추가("구하라", ParticipantState.JOIN_APPROVAL);
//        봉사모집글_팀원_상태추가("스프링", ParticipantState.JOIN_APPROVAL);
//        봉사모집글_팀원_상태추가("ORM", ParticipantState.JOIN_REQUEST);
//        봉사모집글_팀원_상태추가("JPA", ParticipantState.JOIN_REQUEST);
//        봉사모집글_팀원_상태추가("트랜잭션", ParticipantState.JOIN_REQUEST);
//
//        //when
//        RecruitmentDetails details = .findRecruitmentAndWriterDto(saveRecruitment.getRecruitmentNo());
//        clear();
//
//        //then
//        assertThat(details.getRequiredVolunteer().size()).isEqualTo(3);
//        assertThat(details.getApprovalVolunteer().size()).isEqualTo(3);
//    }

    @DisplayName("모집글 첫 참가 신청으로 로그인 사용자 상태가 신청 가능 상태가 된다.")
    @Test
    @Transactional
    public void loginUserAvailableStateByFirst() throws IOException {
        //given
        User newUser = 사용자_등록("new");
        clear();

        //when
        String status = participationService.findParticipationState(saveRecruitment, newUser.getUserNo());

        //then
        assertThat(status).isEqualTo(StateResponse.AVAILABLE.getId());
    }

    @DisplayName("모집글 팀 탈퇴로 인해 로그인 사용자 상태가 신청 가능 상태가 된다.")
    @Test
    @Transactional
    public void loginUserAvailableStateByQuit() throws IOException {
        //given
        Participant p = 사용자및참여자상태추가("new", ParticipantState.QUIT);

        //when
        String status = participationService.findParticipationState(saveRecruitment, p.getParticipant().getUserNo());
        clear();

        //then
        assertThat(status).isEqualTo(StateResponse.AVAILABLE.getId());
    }

    @DisplayName("모집글 팀 신청으로 인해 로그인 사용자 상태가 승인 대기 상태가 된다.")
    @Test
    @Transactional
    public void loginUserPendingState() throws IOException {
        //given
        Participant p = 사용자및참여자상태추가("new", ParticipantState.JOIN_REQUEST);

        //when
        String status = participationService.findParticipationState(saveRecruitment, p.getParticipant().getUserNo());
        clear();

        //then
        assertThat(status).isEqualTo(StateResponse.PENDING.getId());
    }

    @DisplayName("모집글 팀 승인으로 인해 로그인 사용자 상태가 승인 완료 상태가 된다.")
    @Test
    @Transactional
    public void loginUserApprovedState() throws IOException {
        //given
        Participant p = 사용자및참여자상태추가("new", ParticipantState.JOIN_APPROVAL);

        //when
        String status = participationService.findParticipationState(saveRecruitment, p.getParticipant().getUserNo());
        clear();

        //then
        assertThat(status).isEqualTo(StateResponse.APPROVED.getId());
    }

    @DisplayName("모집 기간 만료로 인해 로그인 사용자 상태가 모집 마감 상태가 된다.")
    @Test
    @Transactional
    public void loginUserDoneStateByFinishEndDay() throws IOException {
        //given
        User newUser = 사용자_등록("new");
        //봉사 모집글 시간 정보 변경
        saveRecruitment.setVolunteeringTimeTable(
                Timetable.createTimetable(LocalDate.now().minusDays(2), LocalDate.now().minusDays(1), HourFormat.AM,
                        LocalTime.now(), 3)
        );
        clear();

        //when
        String status = participationService.findParticipationState(saveRecruitment, newUser.getUserNo());

        //then
        assertThat(status).isEqualTo(StateResponse.DONE.getId());
    }

    @DisplayName("팀원 모집인원 초과로 인해 로그인 사용자 상태가 모집 마감 상태가 된다.")
    @Test
    @Transactional
    public void loginUserDoneStateByVolunteerNum() throws IOException {
        //given
        Recruitment findRecruitment = 저장된_모집글_가져오기();
        User newUser = 사용자_등록("new");
        //현재 팀원 최대 인원 4명으로 설정됨
        사용자및참여자상태추가("스프링", ParticipantState.JOIN_APPROVAL);
        findRecruitment.increaseTeamMember();
        사용자및참여자상태추가("ORM", ParticipantState.JOIN_APPROVAL);
        findRecruitment.increaseTeamMember();
        사용자및참여자상태추가("JPA", ParticipantState.JOIN_APPROVAL);
        findRecruitment.increaseTeamMember();
        사용자및참여자상태추가("트랜잭션", ParticipantState.JOIN_APPROVAL);
        findRecruitment.increaseTeamMember();
        clear();

        //when
        Recruitment recruitment = 저장된_모집글_가져오기();
        String status =participationService.findParticipationState(recruitment, newUser.getUserNo());

        //then
        assertThat(status).isEqualTo(StateResponse.FULL.getId());
    }

}