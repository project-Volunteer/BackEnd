package project.volunteer.domain.recruitment.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.image.application.ImageService;
import project.volunteer.domain.image.application.dto.ImageParam;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.image.domain.ImageType;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.domain.recruitment.application.RecruitmentService;
import project.volunteer.domain.recruitment.application.dto.RecruitmentParam;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.repeatPeriod.application.RepeatPeriodService;
import project.volunteer.domain.repeatPeriod.application.dto.RepeatPeriodParam;
import project.volunteer.domain.repeatPeriod.domain.Day;
import project.volunteer.domain.repeatPeriod.domain.Week;
import project.volunteer.domain.storage.domain.Storage;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.infra.s3.FileService;
import project.volunteer.global.test.WithMockCustomUser;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class RecruitmentControllerTestForEdit {

    @PersistenceContext EntityManager em;
    @Autowired UserRepository userRepository;
    @Autowired RecruitmentService recruitmentService;
    @Autowired ImageService imageService;
    @Autowired ImageRepository imageRepository;
    @Autowired FileService fileService;
    @Autowired RepeatPeriodService repeatPeriodService;
    @Autowired MockMvc mockMvc;

    private Long saveRecruitmentNo;
    private Long deleteImageNo;
    private User writer;
    private final String DELETE_URL = "/recruitment";
    @BeforeEach
    public void initUser(){
         writer = userRepository.save(User.builder()
                .id("rctfe1234")
                .password("rctfe1234")
                .nickName("rctfe1234")
                .email("rctfe1234@gmail.com")
                .gender(Gender.M)
                .birthDay(LocalDate.now())
                .picture("picture")
                .joinAlarmYn(true).beforeAlarmYn(true).noticeAlarmYn(true)
                .role(Role.USER)
                .provider("kakao").providerId("rctfe1234")
                .build());
        clear();
    }
    @AfterEach
    public void deleteS3Image() { //S3에 테스트를 위해 저장한 이미지 삭제
        if(deleteImageNo != null) {
            Image image = imageRepository.findById(deleteImageNo).get();
            Storage storage = image.getStorage();
            fileService.deleteFile(storage.getFakeImageName());
        }
    }
    private void clear() {
        em.flush();
        em.clear();
    }
    private void setRegRecruitment(){
        //정기-모집글 등록
        String category = "001";
        String volunteeringType = VolunteeringType.REG.name();
        String volunteerType = "1"; //all
        Boolean isIssued = true;
        String sido = "11";
        String sigungu = "1111";
        String organizationName = "name";
        String details = "details";
        Float latitude = 3.2F, longitude = 3.2F;
        Integer volunteerNum = 5;
        String startDay = "01-01-2000";
        String endDay = "01-01-2000";
        String hourFormat = HourFormat.AM.name();
        String startTime = "01:01";
        Integer progressTime = 3;
        String title = "title", content = "content";
        Boolean isPublished = true;
        RecruitmentParam saveRecruitDto = new RecruitmentParam(category, organizationName, sido, sigungu, details, latitude, longitude,
                isIssued, volunteerType, volunteerNum, volunteeringType, startDay, endDay, hourFormat, startTime, progressTime, title, content, isPublished);
        saveRecruitmentNo = recruitmentService.addRecruitment(writer.getUserNo(), saveRecruitDto);

        //정기-반복주기 등록
        String period = "month";
        int week = Week.FIRST.getValue();
        List<Integer> days = List.of(Day.MON.getValue(), Day.TUES.getValue());
        RepeatPeriodParam savePeriodDto = new RepeatPeriodParam(period, week, days);
        repeatPeriodService.addRepeatPeriod(saveRecruitmentNo, savePeriodDto);
    }
    private void setImage(RealWorkCode realWorkCode, Long no) throws IOException {
        ImageParam staticImageDto = ImageParam.builder()
                .code(realWorkCode)
                .imageType(ImageType.UPLOAD)
                .no(no)
                .staticImageCode(null)
                .uploadImage(getMockMultipartFile())
                .build();
        deleteImageNo = imageService.addImage(staticImageDto);
    }
    private MockMultipartFile getMockMultipartFile() throws IOException {
        return new MockMultipartFile(
                "file", "file.PNG", "image/jpg", new FileInputStream("src/main/resources/static/test/file.PNG"));
    }

    @DisplayName("정기 모집글 삭제 테스트(반복주기,이미지 포함)")
    @Test
    @WithUserDetails(value = "rctfe1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 정기모집글_삭제_성공() throws Exception {
        //given
        setRegRecruitment();
        setImage(RealWorkCode.RECRUITMENT, saveRecruitmentNo);

        //when & then
        mockMvc.perform(delete(DELETE_URL + "/{recruitmentNo}", saveRecruitmentNo))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @DisplayName("정기 모집글 방장이 아닌 사용자가 삭제를 시도하다.")
    @Test
    @WithMockCustomUser()
    public void 정기모집글_삭제_실패_권한없음() throws Exception {
        //given
        setRegRecruitment();
        setImage(RealWorkCode.RECRUITMENT, saveRecruitmentNo);

        //when & then
        mockMvc.perform(delete(DELETE_URL + "/{recruitmentNo}", saveRecruitmentNo))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @WithUserDetails(value = "rctfe1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 정기모집글_삭제_실패_없는모집글() throws Exception {

        mockMvc.perform(delete(DELETE_URL + "/{recruitmentNo}", Long.MAX_VALUE))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

}