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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.participation.api.dto.ParticipantAddParam;
import project.volunteer.domain.participation.api.dto.ParticipantRemoveParam;
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
    @Autowired ParticipantRepository participantRepository;
    private User loginUser;
    private User writer;
    private Recruitment saveRecruitment;
    @BeforeEach
    public void init(){
        //로그인 사용자 저장
        User login = User.createUser("1234", "1234", "1234", "1234", Gender.M, LocalDate.now(), "1234",
                true, true, true, Role.USER, "kakao", "1234", null);
        loginUser = userRepository.save(login);

        //작성자 저장
        User writerUser = User.createUser("4321", "4321", "4321", "4321", Gender.M, LocalDate.now(), "4321",
                true, true, true, Role.USER, "kakao", "4321", null);
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

    private User 사용자_등록(String username){
        User createUser = User.createUser(username, username, username, username, Gender.M, LocalDate.now(), "picture",
                true, true, true, Role.USER, "kakao", username, null);
        return userRepository.save(createUser);
    }
    private Participant 참여자_상태_등록(User user, State state){
        Participant participant = Participant.createParticipant(saveRecruitment, user, state);
        return participantRepository.save(participant);
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
        //given
        final Long recruitmentNo = saveRecruitment.getRecruitmentNo();

        //when & then
        mockMvc.perform(post("/recruitment/{recruitmentNo}/join", recruitmentNo)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }
    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 봉사모집글_팀신청_실패_없는모집글() throws Exception {
        //given
        final Long recruitmentNo = Long.MAX_VALUE;

        //when & then
        mockMvc.perform(post("/recruitment/{recruitmentNo}/join", recruitmentNo)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 봉사모집글_팀신청_실패_종료된모집글() throws Exception {
        //given
        Timetable newTime = Timetable.builder()
                .hourFormat(HourFormat.AM)
                .progressTime(3)
                .startTime(LocalTime.now())
                .startDay(LocalDate.of(2023, 5, 13))
                .endDay(LocalDate.of(2023, 5, 14)) //봉사 활동 종료
                .build();
        recruitmentRepository.findById(saveRecruitment.getRecruitmentNo()).get().setVolunteeringTimeTable(newTime);
        clear();
        final Long recruitmentNo = saveRecruitment.getRecruitmentNo();

        //when & then
        mockMvc.perform(post("/recruitment/{recruitmentNo}/join", recruitmentNo)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 봉사모집글_팀신청_실패_중복신청() throws Exception {
        //given
        참여자_상태_등록(loginUser, State.JOIN_REQUEST);
        final Long recruitmentNo = saveRecruitment.getRecruitmentNo();
        clear();

        //when & then
        mockMvc.perform(post("/recruitment/{recruitmentNo}/join", recruitmentNo)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 봉사모집글_팀신청_실패_참가가능인원초과() throws Exception {
        //given
        User saveUser1 = 사용자_등록("구길동");
        User saveUser2 = 사용자_등록("구갈동");
        User saveUser3 = 사용자_등록("구동굴");
        참여자_상태_등록(saveUser1, State.JOIN_APPROVAL);
        참여자_상태_등록(saveUser2, State.JOIN_APPROVAL);
        참여자_상태_등록(saveUser3, State.JOIN_APPROVAL);

        final Long recruitmentNo = saveRecruitment.getRecruitmentNo();
        clear();

        //when & then
        mockMvc.perform(post("/recruitment/{recruitmentNo}/join", recruitmentNo)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 봉사모집글_팀신청취소_성공() throws Exception {
        //given
        참여자_상태_등록(loginUser, State.JOIN_REQUEST);
        final Long recruitmentNo = saveRecruitment.getRecruitmentNo();
        clear();

        //when & then
        mockMvc.perform(post("/recruitment/{recruitmentNo}/cancel", recruitmentNo)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 봉사모집글_팀신청취소_실패_잘못된상태() throws Exception {
        //given
        참여자_상태_등록(loginUser, State.JOIN_APPROVAL);
        final Long recruitmentNo = saveRecruitment.getRecruitmentNo();
        clear();

        //when & then
        mockMvc.perform(post("/recruitment/{recruitmentNo}/cancel", recruitmentNo)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @WithUserDetails(value = "4321", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 봉사모집글_팀신청승인_성공() throws Exception {
        //given
        final Long recruitmentNo = saveRecruitment.getRecruitmentNo();
        참여자_상태_등록(loginUser, State.JOIN_REQUEST);
        ParticipantAddParam dto = new ParticipantAddParam(List.of(loginUser.getUserNo()));
        clear();

        //when & then
        mockMvc.perform(post("/recruitment/{recruitmentNo}/approval", recruitmentNo)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dto)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 봉사모집글_팀신청승인_실패_권한없음() throws Exception {
        //given
        final Long recruitmentNo = saveRecruitment.getRecruitmentNo();
        참여자_상태_등록(loginUser, State.JOIN_REQUEST);
        ParticipantAddParam dto = new ParticipantAddParam(List.of(loginUser.getUserNo()));
        clear();

        //when & then
        mockMvc.perform(post("/recruitment/{recruitmentNo}/approval", recruitmentNo)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dto)))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @WithUserDetails(value = "4321", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 봉사모집글_팀신청승인_실패_잘못된상태() throws Exception {
        //given
        final Long recruitmentNo = saveRecruitment.getRecruitmentNo();
        참여자_상태_등록(loginUser, State.JOIN_CANCEL);
        ParticipantAddParam dto = new ParticipantAddParam(List.of(loginUser.getUserNo()));
        clear();

        //when & then
        mockMvc.perform(post("/recruitment/{recruitmentNo}/approval", recruitmentNo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @WithUserDetails(value = "4321", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 봉사모집글_팀신청승인_실패_승인가능인원초과() throws Exception {
        //given
        //승인 가능인원 1명
        final Long recruitmentNo = saveRecruitment.getRecruitmentNo();
        User saveUser1 = 사용자_등록("구길동");
        User saveUser2 = 사용자_등록("구갈동");
        User saveUser3 = 사용자_등록("구동굴");
        User saveUser4 = 사용자_등록("구갈염");
        참여자_상태_등록(saveUser1, State.JOIN_APPROVAL);
        참여자_상태_등록(saveUser2, State.JOIN_APPROVAL);
        참여자_상태_등록(saveUser3, State.JOIN_REQUEST);
        참여자_상태_등록(saveUser4, State.JOIN_REQUEST);
        List<Long> requestNos = List.of(saveUser3.getUserNo(), saveUser4.getUserNo());

        ParticipantAddParam dto = new ParticipantAddParam(requestNos);
        clear();

        //when & then
        mockMvc.perform(post("/recruitment/{recruitmentNo}/approval", recruitmentNo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @WithUserDetails(value = "4321", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 봉사모집글_팀원강제탈퇴_성공() throws Exception {
        //given
        final Long recruitmentNo = saveRecruitment.getRecruitmentNo();
        참여자_상태_등록(loginUser, State.JOIN_APPROVAL);
        ParticipantRemoveParam dto = new ParticipantRemoveParam(loginUser.getUserNo());
        clear();

        //then & then
        mockMvc.perform(post("/recruitment/{recruitmentNo}/kick", recruitmentNo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @WithUserDetails(value = "4321", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 봉사모집글_팀원강제탈퇴_실패_잘못된상태() throws Exception {
        //given
        final Long recruitmentNo = saveRecruitment.getRecruitmentNo();
        참여자_상태_등록(loginUser, State.JOIN_REQUEST);
        ParticipantRemoveParam dto = new ParticipantRemoveParam(loginUser.getUserNo());
        clear();

        //then & then
        mockMvc.perform(post("/recruitment/{recruitmentNo}/kick", recruitmentNo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @WithUserDetails(value = "4321", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void notNull_valid_테스트() throws Exception {
        //given
        final Long recruitmentNo = saveRecruitment.getRecruitmentNo();
        ParticipantRemoveParam dto = new ParticipantRemoveParam(null);

        //when & then
        mockMvc.perform(post("/recruitment/{recruitmentNo}/kick", recruitmentNo)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @WithUserDetails(value = "4321", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void notEmpty_valid_테스트() throws Exception {
        //given
        final Long recruitmentNo = saveRecruitment.getRecruitmentNo();
        ParticipantAddParam dto = new ParticipantAddParam(new ArrayList<>());

        //when & then
        mockMvc.perform(post("/recruitment/{recruitmentNo}/approval", recruitmentNo)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

}