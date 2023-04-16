package project.volunteer.domain.recruitment.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.image.domain.RealWorkCode;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.repeatPeriod.domain.Day;
import project.volunteer.domain.repeatPeriod.domain.Week;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.infra.s3.FileService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RecruitmentControllerTestForWrite {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RecruitmentRepository recruitmentRepository;
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private FileService fileService;
    @Autowired
    private MockMvc mockMvc;
    @PersistenceContext
    private EntityManager em;
    private void clear() {
        em.flush();
        em.clear();
    }
    private static final String WRITE_URL = "/recruitment";
    final String volunteeringCategory = "001";
    final String organizationName = "organization";
    final Boolean isIssued = true;
    final String volunteerType = "1"; //all
    final int volunteerNum = 5;
    final String volunteeringType1 = VolunteeringType.IRREG.name();
    final String volunteeringType2 = VolunteeringType.REG.name();
    final String startDay = "01-01-2000";
    final String endDay = "01-01-2000";
    final String hourFormat = HourFormat.AM.name();
    final String startTime = "10:00";
    final int progressTime = 10;
    final String title = "title";
    final String content = "content";
    final Boolean isPublished = true;
    final String period1 =""; //비정기
    final String period2 = "week"; //정기-매주
    final String period3 = "month"; //정기-매달
    final int week1and2 = 0; //비정기, 정기-매주
    final int week3 = Week.FIRST.getValue(); //정기-매달
    final List<Integer> days = List.of(Day.MON.getValue(), Day.TUES.getValue()); //정기-매주, 정기-매달
    final String sido = "11";
    final String sigungu = "11011";
    final String details = "details";
    final float latitude = 3.2F;
    final float longitude = 3.2F;
    final int type1 = 0; //static
    final int type2 = 1; //upload
    final String staticImage1 = "3"; //static
    final String staticImage2 = ""; //upload
    private MockMultipartFile getRealMockMultipartFile() throws IOException {
        return new MockMultipartFile(
                "picture.uploadImage", "file.PNG", "image/jpg", new FileInputStream("src/main/resources/static/test/file.PNG"));
    }
    private MockMultipartFile getFakeMockMultipartFile() throws IOException {
        return new MockMultipartFile(
                "picture.uploadImage", "".getBytes());
    }
    private MultiValueMap createRecruitmentForm_common() {
        MultiValueMap<String,String> info  = new LinkedMultiValueMap<>();
        info.add("volunteeringCategory", volunteeringCategory);
        info.add("organizationName", organizationName);
        info.add("isIssued", String.valueOf(isIssued));
        info.add("volunteerType", volunteerType);
        info.add("volunteerNum", String.valueOf(volunteerNum));
        info.add("startDay", startDay);
        info.add("endDay", endDay);
        info.add("hourFormat", hourFormat);
        info.add("startTime", startTime);
        info.add("progressTime", String.valueOf(progressTime));
        info.add("title", title);
        info.add("content", content);
        info.add("isPublished", String.valueOf(isPublished));
        info.add("address.sido", sido);
        info.add("address.sigungu", sigungu);
        info.add("address.details", details);
        info.add("address.latitude", String.valueOf(latitude));
        info.add("address.longitude", String.valueOf(longitude));
        return info;
    }
    @BeforeEach
    public void init() {
        //유저 로그인
        final String nickname = "nickname";
        final String email = "email@gmail.com";
        final Gender gender = Gender.M;
        final LocalDate birth = LocalDate.now();
        final String picture = "picture";
        final Boolean alarm = true;
        userRepository.save(User.builder().nickName(nickname)
                .email(email).gender(gender).birthDay(birth).picture(picture)
                .joinAlarmYn(alarm).beforeAlarmYn(alarm).noticeAlarmYn(alarm)
                .provider("kakao").providerId("1234").build());
        clear();
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION) //@BeforeEach 어노테이션부터 활성화하도록!!
    public void 모집글_비정기_정적이미지_등록_성공() throws Exception {
        //given
        MultiValueMap info = createRecruitmentForm_common();
        info.add("volunteeringType", volunteeringType1); //비정기
        info.add("period", period1); //비정기
        info.add("week", String.valueOf(week1and2)); //비정기
        info.add("days", ""); //비정기
        info.add("picture.type", String.valueOf(type1)); //정적 이미지
        info.add("picture.staticImage", staticImage1); //정적이미지

        //when & then
        mockMvc.perform(
                multipart(WRITE_URL)
                        .file(getFakeMockMultipartFile()) //정적이미지
                        .params(info)
                )
                .andExpect(status().isCreated())
                .andDo(print());
    }
    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 모집글_정기_매주_업로드이미지_등록_성공() throws Exception {
        //given
        MultiValueMap info = createRecruitmentForm_common();
        info.add("volunteeringType", volunteeringType2); //정기
        info.add("period", period2); //정기-매주
        info.add("week", String.valueOf(week1and2)); //정기-매주
        info.add("days[0]", String.valueOf(days.get(0))); //정기-매주
        info.add("days[1]", String.valueOf(days.get(1))); //정기-매주
        info.add("picture.type", String.valueOf(type2)); //업로드 이미지
        info.add("picture.staticImage", staticImage2); //업로드 이미지

        //when & then
        mockMvc.perform(
                multipart(WRITE_URL)
                        .file(getRealMockMultipartFile()) //업로드 이미지
                        .params(info)
                )
                .andExpect(status().isCreated());

        //finally(s3 업로드 이미지 삭제)
        //테스트 코드에서 저장한 모집글 게시물은 하나니깐 전체 조회해도 반드시 하나만 나올것이다. (좋지 않은 코드 같은데...)
        Recruitment recruitment = recruitmentRepository.findAll().get(0);
        Image image = imageRepository.findByRealWorkCodeAndNo(RealWorkCode.RECRUITMENT, recruitment.getRecruitmentNo()).get();
        fileService.deleteFile(image.getStorage().getFakeImageName());
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 모집글_정기_매달_업로드이미지_등록_성공() throws Exception {
        //given
        MultiValueMap info = createRecruitmentForm_common();
        info.add("volunteeringType", volunteeringType2); //정기
        info.add("period", period3); //정기-매달
        info.add("week", String.valueOf(week3)); //정기-매달
        info.add("days[0]", String.valueOf(days.get(0))); //정기-매달
        info.add("days[1]", String.valueOf(days.get(1))); //정기-매달
        info.add("picture.type", String.valueOf(type2)); //업로드 이미지
        info.add("picture.staticImage", staticImage2); //업로드 이미지

        //when & then
        mockMvc.perform(
                        multipart(WRITE_URL)
                                .file(getRealMockMultipartFile()) //업로드 이미지
                                .params(info)
                )
                .andExpect(status().isCreated());

        //finally(s3 업로드 이미지 삭제)
        //테스트 코드에서 저장한 모집글 게시물은 하나니깐 전체 조회해도 반드시 하나만 나올것이다. (좋지 않은 코드 같은데...)
        Recruitment recruitment = recruitmentRepository.findAll().get(0);
        Image image = imageRepository.findByRealWorkCodeAndNo(RealWorkCode.RECRUITMENT, recruitment.getRecruitmentNo()).get();
        fileService.deleteFile(image.getStorage().getFakeImageName());
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 모집글_저장_실패_없은카테고리코드() throws Exception {
        //given
        MultiValueMap<String,String> info  = new LinkedMultiValueMap<>();
        info.add("volunteeringCategory", "1000"); // -> 존재하지 않는 카테코리
        info.add("organizationName", organizationName);
        info.add("isIssued", String.valueOf(isIssued));
        info.add("volunteerType", volunteerType);
        info.add("volunteerNum", String.valueOf(volunteerNum));
        info.add("startDay", startDay);
        info.add("endDay", endDay);
        info.add("startTime", startTime);
        info.add("progressTime", String.valueOf(23));
        info.add("title", title);
        info.add("content", content);
        info.add("isPublished", String.valueOf(isPublished));
        info.add("address.sido", sido);
        info.add("address.sigungu", sigungu);
        info.add("address.details", details);
        info.add("address.latitude", String.valueOf(latitude));
        info.add("address.longitude", String.valueOf(longitude));
        info.add("volunteeringType",volunteeringType1);
        info.add("period", period1);
        info.add("week", String.valueOf(week1and2)); //비정기
        info.add("days", ""); //비정기
        info.add("picture.type", String.valueOf(type1)); //정적 이미지
        info.add("picture.staticImage", staticImage1); //정적이미지

        //when & then
        mockMvc.perform(
                        multipart(WRITE_URL)
                                .file(getFakeMockMultipartFile()) //정적이미지
                                .params(info)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 모집글폼_유효성검사_NotEmpty() throws Exception {
        //given
        MultiValueMap info = createRecruitmentForm_common();
        info.add("volunteeringType", ""); // -> null & 빈 값 허용안함
        info.add("period", period1); //비정기
        info.add("week", String.valueOf(week1and2)); //비정기
        info.add("days", ""); //비정기
        info.add("picture.type", String.valueOf(type1)); //정적 이미지
        info.add("picture.staticImage", staticImage1); //정적이미지

        //when & then
        mockMvc.perform(
                        multipart(WRITE_URL)
                                .file(getFakeMockMultipartFile()) //정적이미지
                                .params(info)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 모집글폼_유효성검사_Length() throws Exception {
        //given
        MultiValueMap info = createRecruitmentForm_common();
        info.add("volunteeringType", "testtesttest"); // -> length 초과
        info.add("period", period1); //비정기
        info.add("week", String.valueOf(week1and2)); //비정기
        info.add("days",""); //비정기
        info.add("picture.type", String.valueOf(type1)); //정적 이미지
        info.add("picture.staticImage", staticImage1); //정적이미지

        //when & then
        mockMvc.perform(
                        multipart(WRITE_URL)
                                .file(getFakeMockMultipartFile()) //정적이미지
                                .params(info)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 모집글폼_유효성검사_NotNull() throws Exception {
        //given
        MultiValueMap info = createRecruitmentForm_common();
        info.add("volunteeringType",volunteeringType1);
        info.add("period", null); // -> null 허용하지 않음
        info.add("week", String.valueOf(week1and2)); //비정기
        info.add("days", ""); //비정기
        info.add("picture.type", String.valueOf(type1)); //정적 이미지
        info.add("picture.staticImage", staticImage1); //정적이미지

        //when & then
        mockMvc.perform(
                        multipart(WRITE_URL)
                                .file(getFakeMockMultipartFile()) //정적이미지
                                .params(info)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 모집글폼_유효성검사_Range() throws Exception {
        //given
        MultiValueMap<String,String> info  = new LinkedMultiValueMap<>();
        info.add("volunteeringCategory", volunteeringCategory);
        info.add("organizationName", organizationName);
        info.add("isIssued", String.valueOf(isIssued));
        info.add("volunteerType", volunteerType);
        info.add("volunteerNum", String.valueOf(volunteerNum));
        info.add("startDay", startDay);
        info.add("endDay", endDay);
        info.add("startTime", startTime);
        info.add("progressTime", String.valueOf(25)); // -> range(1,24) 까지 허용
        info.add("title", title);
        info.add("content", content);
        info.add("isPublished", String.valueOf(isPublished));
        info.add("address.sido", sido);
        info.add("address.sigungu", sigungu);
        info.add("address.details", details);
        info.add("address.latitude", String.valueOf(latitude));
        info.add("address.longitude", String.valueOf(longitude));
        info.add("volunteeringType",volunteeringType1);
        info.add("period", period1);
        info.add("week", String.valueOf(week1and2)); //정기
        info.add("days", ""); //정기
        info.add("picture.type", String.valueOf(type1)); //정적 이미지
        info.add("picture.staticImage", staticImage1); //정적이미지

        //when & then
        mockMvc.perform(
                        multipart(WRITE_URL)
                                .file(getFakeMockMultipartFile()) //정적이미지
                                .params(info)
                )
                .andExpect(status().isBadRequest());
    }

}