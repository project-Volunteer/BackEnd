package project.volunteer.domain.recruitment.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.image.domain.RealWorkCode;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.infra.s3.FileService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
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
    final String volunteerType = "all";
    final int volunteerNum = 5;
    final String volunteeringType1 = "short";
    final String volunteeringType2 = "long";
    final String startDay = "01-01-2000";
    final String endDay = "01-01-2000";
    final String startTime = "10:00:00";
    final int progressTime = 10;
    final String title = "title";
    final String content = "content";
    final Boolean isPublished = true;
    final String period1 =""; //단기
    final String period2 = "week"; //장기-매주
    final String period3 = "month"; //장기-매달
    final String week1and2 = ""; //단기, 장기-매주
    final String week3 = "first"; //장기-매달
    final List<String> days1 = new ArrayList<>(); //단기
    final List<String> days2 = List.of("mon","tues"); //장기-매주, 장기-매달
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
        final String name = "name";
        final String nickname = "nickname";
        final String email = "email@gmail.com";
        final Gender gender = Gender.M;
        final LocalDate birth = LocalDate.now();
        final String picture = "picture";
        final Boolean alarm = true;
        userRepository.save(User.builder().name(name).nickName(nickname)
                .email(email).gender(gender).birthDay(birth).picture(picture)
                .joinAlarmYn(alarm).beforeAlarmYn(alarm).noticeAlarmYn(alarm).build());

        SecurityContext emptyContext = SecurityContextHolder.createEmptyContext();
        emptyContext.setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        new org.springframework.security.core.userdetails.User(
                                email,"temp",new ArrayList<>())
                        , null
                )
        );
        SecurityContextHolder.setContext(emptyContext);
        clear();
    }

    @Test
    public void 모집글_단기_정적이미지_등록_성공() throws Exception {
        //given
        MultiValueMap info = createRecruitmentForm_common();
        info.add("volunteeringType", volunteeringType1); //단기
        info.add("period", period1); //단기
        info.add("week", week1and2); //단기
        info.add("days", String.valueOf(days1)); //단기
        info.add("picture.type", String.valueOf(type1)); //정적 이미지
        info.add("picture.staticImage", staticImage1); //정적이미지

        //when & then
        mockMvc.perform(
                multipart(WRITE_URL)
                        .file(getFakeMockMultipartFile()) //정적이미지
                        .params(info)
                )
                .andExpect(status().isOk())
                .andDo(print());
    }
    @Test
    public void 모집글_장기_매주_업로드이미지_등록_성공() throws Exception {
        //given
        MultiValueMap info = createRecruitmentForm_common();
        info.add("volunteeringType", volunteeringType2); //장기
        info.add("period", period2); //장기-매주
        info.add("week", week1and2); //장기-매주
        info.add("days[0]", days2.get(0)); //장기-매주
        info.add("days[1]", days2.get(1)); //장기-매주
        info.add("picture.type", String.valueOf(type2)); //업로드 이미지
        info.add("picture.staticImage", staticImage2); //업로드 이미지

        //when & then
        mockMvc.perform(
                multipart(WRITE_URL)
                        .file(getRealMockMultipartFile()) //업로드 이미지
                        .params(info)
                )
                .andExpect(status().isOk());

        //finally(s3 업로드 이미지 삭제)
        //테스트 코드에서 저장한 모집글 게시물은 하나니깐 전체 조회해도 반드시 하나만 나올것이다. (좋지 않은 코드 같은데...)
        Recruitment recruitment = recruitmentRepository.findAll().get(0);
        Image image = imageRepository.findByRealWorkCodeAndNo(RealWorkCode.RECRUITMENT, recruitment.getRecruitmentNo()).get();
        fileService.deleteFile(image.getStorage().getFakeImageName());
    }

    @Test
    public void 모집글_장기_매달_업로드이미지_등록_성공() throws Exception {
        //given
        MultiValueMap info = createRecruitmentForm_common();
        info.add("volunteeringType", volunteeringType2); //장기
        info.add("period", period3); //장기-매달
        info.add("week", week3); //장기-매달
        info.add("days[0]", days2.get(0)); //장기-매달
        info.add("days[1]", days2.get(1)); //장기-매달
        info.add("picture.type", String.valueOf(type2)); //업로드 이미지
        info.add("picture.staticImage", staticImage2); //업로드 이미지

        //when & then
        mockMvc.perform(
                        multipart(WRITE_URL)
                                .file(getRealMockMultipartFile()) //업로드 이미지
                                .params(info)
                )
                .andExpect(status().isOk());

        //finally(s3 업로드 이미지 삭제)
        //테스트 코드에서 저장한 모집글 게시물은 하나니깐 전체 조회해도 반드시 하나만 나올것이다. (좋지 않은 코드 같은데...)
        Recruitment recruitment = recruitmentRepository.findAll().get(0);
        Image image = imageRepository.findByRealWorkCodeAndNo(RealWorkCode.RECRUITMENT, recruitment.getRecruitmentNo()).get();
        fileService.deleteFile(image.getStorage().getFakeImageName());
    }

    @Test
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
        info.add("week", week1and2); //단기
        info.add("days", String.valueOf(days1)); //단기
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
    public void 모집글폼_유효성검사_NotEmpty() throws Exception {
        //given
        MultiValueMap info = createRecruitmentForm_common();
        info.add("volunteeringType", ""); // -> null & 빈 값 허용안함
        info.add("period", period1); //단기
        info.add("week", week1and2); //단기
        info.add("days", String.valueOf(days1)); //단기
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
    public void 모집글폼_유효성검사_Length() throws Exception {
        //given
        MultiValueMap info = createRecruitmentForm_common();
        info.add("volunteeringType", "testtesttest"); // -> length 초과
        info.add("period", period1); //단기
        info.add("week", week1and2); //단기
        info.add("days", String.valueOf(days1)); //단기
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
    public void 모집글폼_유효성검사_NotNull() throws Exception {
        //given
        MultiValueMap info = createRecruitmentForm_common();
        info.add("volunteeringType",volunteeringType1);
        info.add("period", null); // -> null 허용하지 않음
        info.add("week", week1and2); //단기
        info.add("days", String.valueOf(days1)); //단기
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
        info.add("week", week1and2); //단기
        info.add("days", String.valueOf(days1)); //단기
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