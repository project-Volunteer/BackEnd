package project.volunteer.domain.participation.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.participation.api.dto.ParticipantAddParam;
import project.volunteer.domain.participation.api.dto.ParticipantRemoveParam;
import project.volunteer.domain.participation.api.dto.ParticipationParam;
import project.volunteer.domain.participation.application.ParticipationService;
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

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class ParticipationControllerTest {

    @Autowired ObjectMapper objectMapper;
    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired UserRepository userRepository;
    @Autowired RecruitmentRepository recruitmentRepository;
    @Autowired ParticipationService participationService;
    @Autowired ParticipantRepository participantRepository;
    private User loginUser;
    private User writer;
    private Recruitment saveRecruitment;
    private final String joinPath = "/recruitment/join";
    private final String cancelPath = "/recruitment/cancel";
    private final String approvalPath = "/recruitment/approval";
    private final String deportPath = "/recruitment/kick";
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

        //작성자 저장
        writer = userRepository.save(User.builder()
                .id("4321")
                .password("4321")
                .nickName("4321")
                .email("email4321@gmail.com")
                .gender(Gender.M)
                .birthDay(LocalDate.now())
                .picture("picture4321")
                .joinAlarmYn(true).beforeAlarmYn(true).noticeAlarmYn(true)
                .role(Role.USER)
                .provider("kakao").providerId("4321")
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
        saveRecruitment.setWriter(writer);
        recruitmentRepository.save(saveRecruitment);

        clear();
    }
    private void clear() {
        em.flush();
        em.clear();
    }
    private <T> String toJson(T data) throws JsonProcessingException {
        return objectMapper.writeValueAsString(data);
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 봉사모집글_팀신청_성공() throws Exception {

        ParticipationParam dto = new ParticipationParam(saveRecruitment.getRecruitmentNo());

        mockMvc.perform(post(joinPath)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isOk())
                .andDo(print());
    }
    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 봉사모집글_팀신청_실패_없는모집글() throws Exception {

        ParticipationParam dto = new ParticipationParam(Long.MAX_VALUE);

        mockMvc.perform(post(joinPath)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 봉사모집글_팀신청_실패_중복신청() throws Exception {
        //given
        participationService.participate(saveRecruitment.getRecruitmentNo()); //팀 신청 요청
        clear();

        ParticipationParam dto = new ParticipationParam(saveRecruitment.getRecruitmentNo());

        //when & then
        mockMvc.perform(post(joinPath)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 봉사모집글_팀신청취소_성공() throws Exception {
        //given
        participationService.participate(saveRecruitment.getRecruitmentNo()); //팀 신청 요청

        ParticipationParam dto = new ParticipationParam(saveRecruitment.getRecruitmentNo());

        //when & then
        mockMvc.perform(post(cancelPath)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dto)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 봉사모집글_팀신청취소_실패_잘못된상태() throws Exception {
        //given
        participantRepository.save(
                Participant.builder()
                        .participant(loginUser)
                        .recruitment(saveRecruitment)
                        .state(State.JOIN_APPROVAL) //잘못된 상태
                        .build()
        );
        clear();

        ParticipationParam dto = new ParticipationParam(saveRecruitment.getRecruitmentNo());

        //when & then
        mockMvc.perform(post(cancelPath)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @WithUserDetails(value = "4321", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 봉사모집글_팀신청승인_성공() throws Exception {
        //given
        participantRepository.save(
                Participant.builder()
                        .participant(loginUser)
                        .recruitment(saveRecruitment)
                        .state(State.JOIN_REQUEST)
                        .build()
        );
        clear();

        ParticipantAddParam dto =
                new ParticipantAddParam(saveRecruitment.getRecruitmentNo(), List.of(loginUser.getUserNo()));

        //when & then
        mockMvc.perform(post(approvalPath)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dto)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 봉사모집글_팀신청승인_실패_권한없음() throws Exception {
        //given
        participationService.participate(saveRecruitment.getRecruitmentNo());
        clear();

        ParticipantAddParam dto =
                new ParticipantAddParam(saveRecruitment.getRecruitmentNo(), List.of(loginUser.getUserNo()));

        //when & then
        mockMvc.perform(post(approvalPath)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dto)))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @WithUserDetails(value = "4321", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 봉사모집글_팀신청승인_실패_잘못된상태() throws Exception {
        //given
        participantRepository.save(
                Participant.builder()
                        .participant(loginUser)
                        .recruitment(saveRecruitment)
                        .state(State.JOIN_CANCEL) //잘못된 상태
                        .build()
        );
        clear();

        ParticipantAddParam dto =
                new ParticipantAddParam(saveRecruitment.getRecruitmentNo(), List.of(loginUser.getUserNo()));

        //when & then
        mockMvc.perform(post(approvalPath)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @WithUserDetails(value = "4321", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 봉사모집글_팀원강제탈퇴_성공() throws Exception {
        //given
        participantRepository.save(
                Participant.builder()
                        .participant(loginUser)
                        .recruitment(saveRecruitment)
                        .state(State.JOIN_APPROVAL)
                        .build()
        );
        clear();

        ParticipantRemoveParam dto =
                new ParticipantRemoveParam(saveRecruitment.getRecruitmentNo(), loginUser.getUserNo());

        //then & then
        mockMvc.perform(post(deportPath)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @WithUserDetails(value = "4321", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 봉사모집글_팀원강제탈퇴_실패_잘못된상태() throws Exception {
        //given
        participantRepository.save(
                Participant.builder()
                        .participant(loginUser)
                        .recruitment(saveRecruitment)
                        .state(State.JOIN_CANCEL)
                        .build()
        );
        clear();

        ParticipantRemoveParam dto =
                new ParticipantRemoveParam(saveRecruitment.getRecruitmentNo(), loginUser.getUserNo());

        //then & then
        mockMvc.perform(post(deportPath)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void notNull_valid_테스트() throws Exception {
        //given
        ParticipationParam dto = new ParticipationParam(null);

        //when & then
        mockMvc.perform(post(joinPath)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @WithUserDetails(value = "4321", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void notEmpty_valid_테스트() throws Exception {
        //given
        ParticipantAddParam dto = new ParticipantAddParam(1L, new ArrayList<>());
        
        //when & then
        mockMvc.perform(post(approvalPath)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());

    }

}