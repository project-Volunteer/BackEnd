package project.volunteer.domain.participation.application;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
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
import project.volunteer.global.test.WithMockCustomUser;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
@Transactional
class ParticipationServiceImplTest {

    @PersistenceContext EntityManager em;
    @Autowired UserRepository userRepository;
    @Autowired RecruitmentRepository recruitmentRepository;
    @Autowired ParticipantRepository participantRepository;
    @Autowired ParticipationService participationService;
    private Recruitment saveRecruitment;
    private User loginUser;
    private List<User> joinUserList;

    @BeforeEach
    public void init(){
        //로그인 사용자 저장
        loginUser = userRepository.save(User.builder()
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

        //모집글 저장
        saveRecruitment = Recruitment.builder()
                .title("title")
                .content("content")
                .volunteeringCategory(VolunteeringCategory.ADMINSTRATION_ASSISTANCE)
                .volunteeringType(VolunteeringType.IRREG)
                .volunteerType(VolunteerType.TEENAGER)
                .volunteerNum(5)
                .isIssued(true)
                .organizationName("organization")
                .address(Address.builder()
                        .sido("111")
                        .sigungu("11111")
                        .details("details")
                        .build())
                .coordinate(Coordinate.builder()
                        .latitude(3.2F)
                        .longitude(3.2F)
                        .build())
                .timetable(Timetable.builder()
                        .startDay(LocalDate.now())
                        .endDay(LocalDate.now())
                        .hourFormat(HourFormat.AM)
                        .startTime(LocalTime.now())
                        .progressTime(2)
                        .build())
                .isPublished(true)
                .build();
        saveRecruitment.setWriter(loginUser);
        recruitmentRepository.save(saveRecruitment);

        clear();
    }

    private void joinUser(int count){
        joinUserList = new ArrayList<>();

        for(int i=0;i<count;i++){
            User joinUser = userRepository.save(User.builder()
                    .id("1234" + i)
                    .password("1234" + i)
                    .nickName("nickname" + i)
                    .email("email" + i + "@gmail.com")
                    .gender(Gender.M)
                    .birthDay(LocalDate.now())
                    .picture("picture" + i)
                    .joinAlarmYn(true).beforeAlarmYn(true).noticeAlarmYn(true)
                    .role(Role.USER)
                    .provider("kakao").providerId("1234" + i)
                    .build());

            joinUserList.add(joinUser);
        }
    }
    private void clear() {
        em.flush();
        em.clear();
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 팀신청_최초_성공(){
        //given && when
        participationService.participate(saveRecruitment.getRecruitmentNo());
        clear();

        //then
        Participant participant = participantRepository.findByRecruitment_RecruitmentNoAndParticipant_UserNo(saveRecruitment.getRecruitmentNo(),
                loginUser.getUserNo()).get();
        Assertions.assertThat(participant.getState()).isEqualTo(State.JOIN_REQUEST);
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 팀신청_재신청_성공(){
        //given
        participantRepository.save(Participant.builder()
                .participant(loginUser)
                .recruitment(saveRecruitment)
                .state(State.JOIN_CANCEL)
                .build());
        clear();

        //when
        participationService.participate(saveRecruitment.getRecruitmentNo());
        clear();

        //then
        Participant participant = participantRepository.findByRecruitment_RecruitmentNoAndParticipant_UserNo(saveRecruitment.getRecruitmentNo(),
                loginUser.getUserNo()).get();
        Assertions.assertThat(participant.getState()).isEqualTo(State.JOIN_REQUEST);
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 팀신청_실패_중복신청(){
        //given
        participantRepository.save(Participant.builder()
                .participant(loginUser)
                .recruitment(saveRecruitment)
                .state(State.JOIN_APPROVAL)
                .build());
        clear();

        //when && then
        Assertions.assertThatThrownBy(() -> participationService.participate(saveRecruitment.getRecruitmentNo()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("DUPLICATE_PARTICIPATION");
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 팀신청_실패_없는모집글(){

        Assertions.assertThatThrownBy(() -> participationService.participate(Long.MAX_VALUE))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("NOT_EXIST_RECRUITMENT");
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 팀신청_실패_삭제된모집글(){
        //given
        recruitmentRepository.findById(saveRecruitment.getRecruitmentNo()).get().setDeleted();
        clear();

        //when & then
        Assertions.assertThatThrownBy(() -> participationService.participate(saveRecruitment.getRecruitmentNo()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("NOT_EXIST_RECRUITMENT");
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 팀신청취소_성공(){
        //given
        participantRepository.save(Participant.builder()
                .participant(loginUser)
                .recruitment(saveRecruitment)
                .state(State.JOIN_REQUEST)
                .build());
        clear();

        //when
        participationService.cancelParticipation(saveRecruitment.getRecruitmentNo());
        clear();

        //then
        Participant participant = participantRepository.findByRecruitment_RecruitmentNoAndParticipant_UserNo(
                saveRecruitment.getRecruitmentNo(), loginUser.getUserNo()).get();
        Assertions.assertThat(participant.getState()).isEqualTo(State.JOIN_CANCEL);
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 팀신청취소_실패_잘못된상태(){
        //given
        participantRepository.save(Participant.builder()
                .participant(loginUser)
                .recruitment(saveRecruitment)
                .state(State.JOIN_APPROVAL)
                .build());
        clear();

        //when & then
        Assertions.assertThatThrownBy(() -> participationService.cancelParticipation(saveRecruitment.getRecruitmentNo()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("INVALID_STATE");
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 팀신청승인_성공(){
        //given
        joinUser(4);
        joinUserList.stream()
                .forEach(user -> participantRepository.save(
                        Participant.builder()
                                .participant(user)
                                .recruitment(saveRecruitment)
                                .state(State.JOIN_REQUEST)
                                .build()));
        List<Long> joinUserNoList = joinUserList.stream().map(user -> user.getUserNo()).collect(Collectors.toList());
        clear();

        //when
        participationService.approvalParticipant(saveRecruitment.getRecruitmentNo(), joinUserNoList);
        clear();

        //then
        List<Participant> participantList =
                participantRepository.findByRecruitment_RecruitmentNoAndParticipant_UserNoIn(saveRecruitment.getRecruitmentNo(), joinUserNoList);
        participantList.stream()
                .forEach(p -> Assertions.assertThat(p.getState()).isEqualTo(State.JOIN_APPROVAL));

    }

    @Test
    @WithMockCustomUser(tempValue= "3321") //인증용 객체 직접 할당
    public void 팀신청승인_실패_권한없음(){
        //given
        joinUser(4);
        joinUserList.stream()
                .forEach(user -> participantRepository.save(
                        Participant.builder()
                                .participant(user)
                                .recruitment(saveRecruitment)
                                .state(State.JOIN_REQUEST)
                                .build()));
        List<Long> joinUserNoList = joinUserList.stream().map(user -> user.getUserNo()).collect(Collectors.toList());
        clear();

        //when & then
        Assertions.assertThatThrownBy(
                () -> participationService.approvalParticipant(saveRecruitment.getRecruitmentNo(), joinUserNoList))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("FORBIDDEN_RECRUITMENT");
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 팀신청승인_실패_잘못된상태(){
        //given
        joinUser(4);
        for(int i=0;i<joinUserList.size()-1;i++){
            participantRepository.save(
                    Participant.builder()
                            .participant(joinUserList.get(i))
                            .recruitment(saveRecruitment)
                            .state(State.JOIN_REQUEST)
                            .build());
        }
        participantRepository.save(
                Participant.builder()
                        .participant(joinUserList.get(joinUserList.size()-1))
                        .recruitment(saveRecruitment)
                        .state(State.JOIN_CANCEL)
                        .build()
        );

        List<Long> joinUserNoList = joinUserList.stream().map(user -> user.getUserNo()).collect(Collectors.toList());
        clear();

        //when & then
        Assertions.assertThatThrownBy(
                () -> participationService.approvalParticipant(saveRecruitment.getRecruitmentNo(), joinUserNoList))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("INVALID_STATE");

    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 팀원강제탈퇴_성공(){
        //given
        joinUser(1);
        participantRepository.save(
                Participant.builder()
                        .participant(joinUserList.get(0))
                        .recruitment(saveRecruitment)
                        .state(State.JOIN_APPROVAL)
                        .build()
        );

        //when
        participationService.deportParticipant(saveRecruitment.getRecruitmentNo(),joinUserList.get(0).getUserNo());
        clear();

        //then
        Participant participant = participantRepository.findByRecruitment_RecruitmentNoAndParticipant_UserNo(saveRecruitment.getRecruitmentNo(),
                joinUserList.get(0).getUserNo()).get();
        Assertions.assertThat(participant.getState()).isEqualTo(State.DEPORT);
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 팀원강제탈퇴_실패_잘못된상태(){
        //given
        joinUser(1);
        participantRepository.save(
                Participant.builder()
                        .participant(joinUserList.get(0))
                        .recruitment(saveRecruitment)
                        .state(State.JOIN_REQUEST) //잘못된 상태
                        .build()
        );

        //when & then
        Assertions.assertThatThrownBy(() ->
                participationService.deportParticipant(saveRecruitment.getRecruitmentNo(), joinUserList.get(0).getUserNo()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("INVALID_STATE");
    }


}