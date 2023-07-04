package project.volunteer.domain.notice.api;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
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
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.Coordinate;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.common.component.Timetable;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class NoticeControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired RecruitmentRepository recruitmentRepository;
    @Autowired NoticeRepository noticeRepository;

    User writer;
    Recruitment saveRecruitment;
    @BeforeEach
    void init(){
        //작성자 저장
        User writerUser = User.createUser("nct_1234", "nct_1234", "nct_1234", "nct_1234", Gender.M, LocalDate.now(), "picture",
                true, true, true, Role.USER, "kakao", "nct_1234", null);
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
    @DisplayName("봉사 공지사항 등록 요청에 성공하다.")
    @WithUserDetails(value = "nct_1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void noticeAddRequest() throws Exception {
        //given
        final String addNoticeContent = "add";
        NoticeAdd dto = new NoticeAdd(addNoticeContent);

        //when & then
        mockMvc.perform(post("/recruitment/{recruitmentNo}/notice", saveRecruitment.getRecruitmentNo())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(toJson(dto)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("봉사 공지사항 수정 요청에 성공하다.")
    @WithUserDetails(value = "nct_1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void noticeEditRequest() throws Exception {
        //given
        final String addNoticeContent = "add";
        final String editNoticeContent = "edit";
        Notice saveNotice = 공지사항_등록(addNoticeContent, saveRecruitment);
        NoticeEdit dto = new NoticeEdit(editNoticeContent);

        //when & then
        mockMvc.perform(put("/recruitment/{recruitmentNo}/notice/{noticeNo}", saveRecruitment.getRecruitmentNo(), saveNotice.getNoticeNo())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(toJson(dto)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("봉사 공지사항 삭제 요청에 성공하다.")
    @WithUserDetails(value = "nct_1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void noticeDeleteRequest() throws Exception {
        //given
        final String addNoticeContent = "add";
        Notice saveNotice = 공지사항_등록(addNoticeContent, saveRecruitment);

        //when & then
        mockMvc.perform(delete("/recruitment/{recruitmentNo}/notice/{noticeNo}", saveRecruitment.getRecruitmentNo(), saveNotice.getNoticeNo())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("봉사 공지사항 단일 조회 요청에 성공하다.")
    @WithUserDetails(value = "nct_1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void noticeDetailsRequest() throws Exception {
        //given
        final String addNoticeContent = "add";
        Notice saveNotice = 공지사항_등록(addNoticeContent, saveRecruitment);

        //when
        ResultActions resultActions = mockMvc.perform(get("/recruitment/{recruitmentNo}/notice/{noticeNo}", saveRecruitment.getRecruitmentNo(), saveNotice.getNoticeNo())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notice.content").value(addNoticeContent))
                .andExpect(jsonPath("$.notice.checkCnt").value(0))
                .andExpect(jsonPath("$.notice.commentsCnt").value(0))
                .andExpect(jsonPath("$.notice.isChecked").value(false));
    }

    @Test
    @DisplayName("봉사 공지사항 복수 조회 요청에 성공하다.")
    @WithUserDetails(value = "nct_1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void noticeListRequest() throws Exception {
        //given
        final String addNoticeContent1 = "add1";
        final String addNoticeContent2 = "add2";
        final String addNoticeContent3 = "add3";
        Notice saveNotice1 = 공지사항_등록(addNoticeContent1, saveRecruitment);
        Notice saveNotice2 = 공지사항_등록(addNoticeContent2, saveRecruitment);
        Notice saveNotice3 = 공지사항_등록(addNoticeContent3, saveRecruitment);

        //when
        ResultActions resultActions = mockMvc.perform(get("/recruitment/{recruitmentNo}/notice", saveRecruitment.getRecruitmentNo())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print());

        //then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.noticeList[0].content").value(addNoticeContent1))
                .andExpect(jsonPath("$.noticeList[1].content").value(addNoticeContent2))
                .andExpect(jsonPath("$.noticeList[2].content").value(addNoticeContent3));
    }

    private Notice 공지사항_등록(String content, Recruitment recruitment){
        Notice createNotice = Notice.createNotice(content);
        createNotice.setRecruitment(recruitment);
        return noticeRepository.save(createNotice);
    }
    private <T> String toJson(T data) throws JsonProcessingException {
        return objectMapper.writeValueAsString(data);
    }
}