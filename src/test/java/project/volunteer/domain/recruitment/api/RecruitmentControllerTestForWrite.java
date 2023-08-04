package project.volunteer.domain.recruitment.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import project.volunteer.domain.image.domain.ImageType;
import project.volunteer.domain.repeatPeriod.domain.Period;
import project.volunteer.domain.storage.dao.StorageRepository;
import project.volunteer.domain.storage.domain.Storage;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.repeatPeriod.domain.Day;
import project.volunteer.domain.repeatPeriod.domain.Week;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.infra.s3.FileService;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@AutoConfigureRestDocs
class RecruitmentControllerTestForWrite {
    @Autowired private UserRepository userRepository;
    @Autowired private StorageRepository storageRepository;
    @Autowired private FileService fileService;
    @Autowired private MockMvc mockMvc;

    final String WRITE_URL = "/recruitment";
    final String AUTHORIZATION_HEADER = "accessToken";

    @BeforeEach
    private void initUser() {
        userRepository.save(User.builder()
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
    }
    @AfterEach
    void deleteUploadImage(){
        List<Storage> storages = storageRepository.findAll();
        storages.stream()
                .forEach(s -> fileService.deleteFile(s.getFakeImageName()));
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 모집글_비정기_정적이미지_등록_성공() throws Exception {
        //given
        final String volunteeringType = VolunteeringType.IRREG.getId();
        final String type = ImageType.STATIC.getId();
        final String staticImage = "3";

        MultiValueMap info = createCommontRecruitmentForm();
        info.add("volunteeringType", volunteeringType); //비정기
        info.add("picture.type", type); //정적 이미지
        info.add("picture.staticImage", staticImage); //정적이미지

        //when & then
        mockMvc.perform(
                multipart(WRITE_URL)
                        .params(info)
                )
                .andExpect(status().isCreated())
                .andDo(print());
    }
    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 모집글_정기_매주_업로드이미지_등록_성공() throws Exception {
        //given
        final String volunteeringType = VolunteeringType.REG.getId();
        final String period = Period.WEEK.getId();
        final List<String> days = List.of(Day.MON.getId());
        final String type = ImageType.UPLOAD.getId();

        MultiValueMap info = createCommontRecruitmentForm();
        info.add("volunteeringType", volunteeringType); //정기
        info.add("period", period); //정기-매주
        info.add("days[0]", days.get(0)); //정기-매주
        info.add("picture.type", type); //업로드 이미지

        //when & then
        mockMvc.perform(
                multipart(WRITE_URL)
                        .file(getRealMockMultipartFile()) //업로드 이미지
                        .params(info)
                )
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 모집글_정기_매달_업로드이미지_등록_성공() throws Exception {
        //given
        final String volunteeringType = VolunteeringType.REG.getId();
        final String period = Period.MONTH.getId();
        final String week = Week.FIRST.getId();
        final List<String> days = List.of(Day.MON.getId(), Day.TUES.getId());
        final String type = ImageType.UPLOAD.getId();

        MultiValueMap info = createCommontRecruitmentForm();
        info.add("volunteeringType", volunteeringType);
        info.add("period", period);
        info.add("week", week); //정기-매달
        info.add("days", days.get(0));
        info.add("days", days.get(1));
        info.add("picture.type", String.valueOf(type));

        //when
        ResultActions result = mockMvc.perform(
                multipart(WRITE_URL)
                        .file(getRealMockMultipartFile()) //업로드 이미지
                        .header(AUTHORIZATION_HEADER, "access Token")
                        .params(info)
        );

        //then
        result.andExpect(status().isCreated())
                .andDo(print())
                .andDo(
                        document("APIs/volunteering/recruitment/POST",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName(AUTHORIZATION_HEADER).description("JWT Access Token")
                                ),
                                requestParts(
                                        partWithName("picture.uploadImage").optional().attributes(key("constraints").value("정적 이미지일 경우 NULL 허용")).description("첨부 이미지")
                                ),
                                requestParameters(
                                        parameterWithName("volunteeringCategory").description("Code VolunteeringCategory 참조바람."),
                                        parameterWithName("organizationName").attributes(key("constraints").value("1이상 50이하")).description("기관 이름"),
                                        parameterWithName("address.sido").description("시/구 코드"),
                                        parameterWithName("address.sigungu").description("시/군/구 코드"),
                                        parameterWithName("address.details").description("상세주소"),
                                        parameterWithName("address.latitude").description("위도"),
                                        parameterWithName("address.longitude").description("경도"),
                                        parameterWithName("isIssued").description("봉사 시간 인증 가능 여부"),
                                        parameterWithName("volunteerType").description("Code VolunteerType 참고바람."),
                                        parameterWithName("volunteerNum").attributes(key("constraints").value("1이상 9999이하")).description("봉사 모집 인원"),
                                        parameterWithName("volunteeringType").description("Code VolunteeringType 참고바람"),
                                        parameterWithName("startDay").attributes(key("format").value("MM-dd-yyyy")).description("봉사 모집 시작 날짜"),
                                        parameterWithName("endDay").attributes(key("format").value("MM-dd-yyyy")).description("봉사 모집 종료 날짜"),
                                        parameterWithName("hourFormat").description("Code HourFormat 참고바람."),
                                        parameterWithName("startTime").attributes(key("format").value("HH:mm")).description("정기 봉사 일정 시작 시간"),
                                        parameterWithName("progressTime").attributes(key("constraints").value("1이상 24이하")).description("정기 봉사 일정 진행 시간"),
                                        parameterWithName("period").optional().attributes(key("constraints").value("비정기일 경우 NULL 허용")).description("Code Period 참고바람."),
                                        parameterWithName("week").optional().attributes(key("constraints").value("비정기 혹은 Period가 매주일 경우 NULL 허용")).description("Code Week 참고바람."),
                                        parameterWithName("days").optional().attributes(key("constraints").value("비정기일 경우 NULL 허용")).description("Code Day 참고바람, 다중 값 허용(배열)"),
                                        parameterWithName("picture.type").description("Code ImageType 참고바람."),
                                        parameterWithName("picture.staticImage").optional().attributes(key("constraints").value("업로드 이미지일 경우 NULL 허용")).description("정적 이미지 코드"),
                                        parameterWithName("title").attributes(key("constraints").value("1이상 255이하")).description("봉사 모집글 제목"),
                                        parameterWithName("content").attributes(key("constraints").value("1이상 255이하")).description("봉사 모집글 본문"),
                                        parameterWithName("isPublished").description("임시 저장 유무")
                                 ),
                                responseFields(
                                        fieldWithPath("no").type(JsonFieldType.NUMBER).description("봉사 모집글 고유키 PK")
                                )
                        )
                );
    }

    //TODO: Validation 테스트 그냥 없애는게 나을까?
//    @Test
//    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
//    public void 모집글_저장_실패_없은봉사자유형코드() throws Exception {
//        //given
//        MultiValueMap<String,String> info  = new LinkedMultiValueMap<>();
//        info.add("volunteeringCategory", "001");
//        info.add("organizationName", organizationName);
//        info.add("isIssued", String.valueOf(isIssued));
//        info.add("volunteerType", "99999999"); // -> 없는 봉사자 유형코드!!!
//        info.add("volunteerNum", String.valueOf(volunteerNum));
//        info.add("startDay", startDay);
//        info.add("endDay", endDay);
//        info.add("startTime", startTime);
//        info.add("progressTime", String.valueOf(23));
//        info.add("hourFormat", hourFormat);
//        info.add("title", title);
//        info.add("content", content);
//        info.add("isPublished", String.valueOf(isPublished));
//        info.add("address.sido", sido);
//        info.add("address.sigungu", sigungu);
//        info.add("address.details", details);
//        info.add("address.latitude", String.valueOf(latitude));
//        info.add("address.longitude", String.valueOf(longitude));
//        info.add("volunteeringType",volunteeringType1);
//        info.add("period", period1);
//        info.add("week", String.valueOf(week1and2)); //비정기
//        info.add("days", ""); //비정기
//        info.add("picture.type", String.valueOf(type1)); //정적 이미지
//        info.add("picture.staticImage", staticImage1); //정적이미지
//
//        //when & then
//        mockMvc.perform(
//                        multipart(WRITE_URL)
//                                .file(getFakeMockMultipartFile()) //정적이미지
//                                .params(info)
//                )
//                .andExpect(status().isBadRequest())
//                .andDo(print());
//    }
//
//    @Test
//    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
//    public void 모집글_저장_실패_없은봉사카테고리코드() throws Exception {
//        //given
//        MultiValueMap<String,String> info  = new LinkedMultiValueMap<>();
//        info.add("volunteeringCategory", "1000"); // -> 존재하지 않는 카테코리 코드 !!
//        info.add("organizationName", organizationName);
//        info.add("isIssued", String.valueOf(isIssued));
//        info.add("volunteerType", volunteerType);
//        info.add("volunteerNum", String.valueOf(volunteerNum));
//        info.add("startDay", startDay);
//        info.add("endDay", endDay);
//        info.add("startTime", startTime);
//        info.add("progressTime", String.valueOf(23));
//        info.add("hourFormat", hourFormat);
//        info.add("title", title);
//        info.add("content", content);
//        info.add("isPublished", String.valueOf(isPublished));
//        info.add("address.sido", sido);
//        info.add("address.sigungu", sigungu);
//        info.add("address.details", details);
//        info.add("address.latitude", String.valueOf(latitude));
//        info.add("address.longitude", String.valueOf(longitude));
//        info.add("volunteeringType",volunteeringType1);
//        info.add("period", period1);
//        info.add("week", String.valueOf(week1and2)); //비정기
//        info.add("days", ""); //비정기
//        info.add("picture.type", String.valueOf(type1)); //정적 이미지
//        info.add("picture.staticImage", staticImage1); //정적이미지
//
//        //when & then
//        mockMvc.perform(
//                        multipart(WRITE_URL)
//                                .file(getFakeMockMultipartFile()) //정적이미지
//                                .params(info)
//                )
//                .andExpect(status().isBadRequest())
//                .andDo(print());
//    }
//    @Test
//    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
//    public void 모집글_저장_실패_없은봉사유형값() throws Exception {
//        //given
//        MultiValueMap<String,String> info  = new LinkedMultiValueMap<>();
//        info.add("volunteeringCategory", "001");
//        info.add("organizationName", organizationName);
//        info.add("isIssued", String.valueOf(isIssued));
//        info.add("volunteerType", volunteerType);
//        info.add("volunteerNum", String.valueOf(volunteerNum));
//        info.add("startDay", startDay);
//        info.add("endDay", endDay);
//        info.add("startTime", startTime);
//        info.add("progressTime", String.valueOf(23));
//        info.add("hourFormat", hourFormat);
//        info.add("title", title);
//        info.add("content", content);
//        info.add("isPublished", String.valueOf(isPublished));
//        info.add("address.sido", sido);
//        info.add("address.sigungu", sigungu);
//        info.add("address.details", details);
//        info.add("address.latitude", String.valueOf(latitude));
//        info.add("address.longitude", String.valueOf(longitude));
//        info.add("volunteeringType", "fail"); // -> 존재하지않는 봉사유형!!!!
//        info.add("period", period1);
//        info.add("week", String.valueOf(week1and2)); //비정기
//        info.add("days", ""); //비정기
//        info.add("picture.type", String.valueOf(type1)); //정적 이미지
//        info.add("picture.staticImage", staticImage1); //정적이미지
//
//        //when & then
//        mockMvc.perform(
//                        multipart(WRITE_URL)
//                                .file(getFakeMockMultipartFile()) //정적이미지
//                                .params(info)
//                )
//                .andExpect(status().isBadRequest())
//                .andDo(print());
//    }
//    @Test
//    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
//    public void 모집글폼_유효성검사_NotEmpty() throws Exception {
//        //given
//        MultiValueMap info = createCommontRecruitmentForm();
//        //info.add("volunteeringType", ""); // -> null & 빈 값 허용안함
//        info.add("period", period1); //비정기
//        info.add("week", String.valueOf(week1and2)); //비정기
//        info.add("days", ""); //비정기
//        info.add("picture.type", String.valueOf(type1)); //정적 이미지
//        info.add("picture.staticImage", staticImage1); //정적이미지
//
//        //when & then
//        mockMvc.perform(
//                        multipart(WRITE_URL)
//                                .file(getFakeMockMultipartFile()) //정적이미지
//                                .params(info)
//                )
//                .andExpect(status().isBadRequest())
//                .andDo(print());
//    }
//    @Test
//    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
//    public void 모집글폼_유효성검사_Length() throws Exception {
//        //given
//        MultiValueMap<String,String> info  = new LinkedMultiValueMap<>();
//        info.add("volunteeringCategory", "001");
//        info.add("organizationName", ""); // -> Length 미만
//        info.add("isIssued", String.valueOf(isIssued));
//        info.add("volunteerType", volunteerType);
//        info.add("volunteerNum", String.valueOf(volunteerNum));
//        info.add("startDay", startDay);
//        info.add("endDay", endDay);
//        info.add("startTime", startTime);
//        info.add("progressTime", String.valueOf(23));
//        info.add("hourFormat", hourFormat);
//        info.add("title", title);
//        info.add("content", content);
//        info.add("isPublished", String.valueOf(isPublished));
//        info.add("address.sido", sido);
//        info.add("address.sigungu", sigungu);
//        info.add("address.details", details);
//        info.add("address.latitude", String.valueOf(latitude));
//        info.add("address.longitude", String.valueOf(longitude));
//        info.add("volunteeringType", volunteeringType1);
//        info.add("period", period1);
//        info.add("week", String.valueOf(week1and2)); //비정기
//        info.add("days", ""); //비정기
//        info.add("picture.type", String.valueOf(type1)); //정적 이미지
//        info.add("picture.staticImage", staticImage1); //정적이미지
//
//        //when & then
//        mockMvc.perform(
//                        multipart(WRITE_URL)
//                                .file(getFakeMockMultipartFile()) //정적이미지
//                                .params(info)
//                )
//                .andExpect(status().isBadRequest())
//                .andDo(print());
//    }
//    @Test
//    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
//    public void 모집글폼_유효성검사_Range() throws Exception {
//        //given
//        MultiValueMap<String,String> info  = new LinkedMultiValueMap<>();
//        info.add("volunteeringCategory", "001");
//        info.add("organizationName", organizationName);
//        info.add("isIssued", String.valueOf(isIssued));
//        info.add("volunteerType", volunteerType);
//        info.add("volunteerNum", String.valueOf(Integer.MAX_VALUE)); // -> Range 초과
//        info.add("startDay", startDay);
//        info.add("endDay", endDay);
//        info.add("startTime", startTime);
//        info.add("progressTime", String.valueOf(23));
//        info.add("hourFormat", hourFormat);
//        info.add("title", title);
//        info.add("content", content);
//        info.add("isPublished", String.valueOf(isPublished));
//        info.add("address.sido", sido);
//        info.add("address.sigungu", sigungu);
//        info.add("address.details", details);
//        info.add("address.latitude", String.valueOf(latitude));
//        info.add("address.longitude", String.valueOf(longitude));
//        info.add("volunteeringType", volunteeringType1);
//        info.add("period", period1);
//        info.add("week", String.valueOf(week1and2)); //비정기
//        info.add("days", ""); //비정기
//        info.add("picture.type", String.valueOf(type1)); //정적 이미지
//        info.add("picture.staticImage", staticImage1); //정적이미지
//
//        //when & then
//        mockMvc.perform(
//                        multipart(WRITE_URL)
//                                .file(getFakeMockMultipartFile()) //정적이미지
//                                .params(info)
//                )
//                .andExpect(status().isBadRequest())
//                .andDo(print());
//    }
//    @Test
//    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
//    public void 모집글폼_유효성검사_NotNull() throws Exception {
//        //given
//        MultiValueMap info = createCommontRecruitmentForm();
//        info.add("volunteeringType",volunteeringType1);
//        info.add("period", null); // -> null 허용하지 않음
//        info.add("week", String.valueOf(week1and2)); //비정기
//        info.add("days", ""); //비정기
//        info.add("picture.type", String.valueOf(type1)); //정적 이미지
//        info.add("picture.staticImage", staticImage1); //정적이미지
//
//        //when & then
//        mockMvc.perform(
//                        multipart(WRITE_URL)
//                                .file(getFakeMockMultipartFile()) //정적이미지
//                                .params(info)
//                )
//                .andExpect(status().isBadRequest());
//    }
//    @Test
//    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
//    public void 모집글폼_유효성검사_Pattern_날짜() throws Exception {
//        //given
//        MultiValueMap<String,String> info  = new LinkedMultiValueMap<>();
//        info.add("volunteeringCategory", "001");
//        info.add("organizationName", organizationName);
//        info.add("isIssued", String.valueOf(isIssued));
//        info.add("volunteerType", volunteerType);
//        info.add("volunteerNum", String.valueOf(volunteerNum));
//        info.add("startDay", "12:25-2222"); // -> patten: MM-dd-yyyy
//        info.add("endDay", "09-302222"); // -> patten: MM-dd-yyyy
//        info.add("startTime", startTime);
//        info.add("progressTime", String.valueOf(23));
//        info.add("hourFormat", hourFormat);
//        info.add("title", title);
//        info.add("content", content);
//        info.add("isPublished", String.valueOf(isPublished));
//        info.add("address.sido", sido);
//        info.add("address.sigungu", sigungu);
//        info.add("address.details", details);
//        info.add("address.latitude", String.valueOf(latitude));
//        info.add("address.longitude", String.valueOf(longitude));
//        info.add("volunteeringType", volunteeringType1);
//        info.add("period", period1);
//        info.add("week", String.valueOf(week1and2)); //비정기
//        info.add("days", ""); //비정기
//        info.add("picture.type", String.valueOf(type1)); //정적 이미지
//        info.add("picture.staticImage", staticImage1); //정적이미지
//
//        //when & then
//        mockMvc.perform(
//                        multipart(WRITE_URL)
//                                .file(getFakeMockMultipartFile()) //정적이미지
//                                .params(info)
//                )
//                .andExpect(status().isBadRequest())
//                .andDo(print());
//    }
//    @Test
//    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
//    public void 모집글폼_유효성검사_Pattern_시간() throws Exception {
//        //given
//        MultiValueMap<String,String> info  = new LinkedMultiValueMap<>();
//        info.add("volunteeringCategory", "001");
//        info.add("organizationName", organizationName);
//        info.add("isIssued", String.valueOf(isIssued));
//        info.add("volunteerType", volunteerType);
//        info.add("volunteerNum", String.valueOf(volunteerNum));
//        info.add("startDay", startDay);
//        info.add("endDay", endDay);
//        info.add("startTime", "01/59"); // -> patten: HH:MM
//        info.add("progressTime", String.valueOf(23));
//        info.add("hourFormat", hourFormat);
//        info.add("title", title);
//        info.add("content", content);
//        info.add("isPublished", String.valueOf(isPublished));
//        info.add("address.sido", sido);
//        info.add("address.sigungu", sigungu);
//        info.add("address.details", details);
//        info.add("address.latitude", String.valueOf(latitude));
//        info.add("address.longitude", String.valueOf(longitude));
//        info.add("volunteeringType", volunteeringType1);
//        info.add("period", period1);
//        info.add("week", String.valueOf(week1and2)); //비정기
//        info.add("days", ""); //비정기
//        info.add("picture.type", String.valueOf(type1)); //정적 이미지
//        info.add("picture.staticImage", staticImage1); //정적이미지
//
//        //when & then
//        mockMvc.perform(
//                        multipart(WRITE_URL)
//                                .file(getFakeMockMultipartFile()) //정적이미지
//                                .params(info)
//                )
//                .andExpect(status().isBadRequest())
//                .andDo(print());
//    }

    private MultiValueMap createCommontRecruitmentForm() {
        final String volunteeringCategory = "001";
        final String organizationName = "organization";
        final String sido = "11";
        final String sigungu = "11011";
        final String details = "details";
        final float latitude = 3.2F;
        final float longitude = 3.2F;
        final Boolean isIssued = true;
        final Boolean isPublished = true;
        final String volunteerType = "1"; //all
        final int volunteerNum = 5;
        final String startDay = "05-01-2023";
        final String endDay = "05-09-2023";
        final String hourFormat = HourFormat.AM.name();
        final String startTime = "10:00";
        final int progressTime = 10;
        final String title = "title";
        final String content = "content";

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
    private MockMultipartFile getRealMockMultipartFile() throws IOException {
        return new MockMultipartFile(
                "picture.uploadImage", "file.PNG", "image/jpg", new FileInputStream("src/main/resources/static/test/file.PNG"));
    }
}