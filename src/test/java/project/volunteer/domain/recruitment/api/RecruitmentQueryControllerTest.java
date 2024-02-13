//package project.volunteer.domain.recruitment.api;
//
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.restdocs.RestDocumentationContextProvider;
//import org.springframework.restdocs.RestDocumentationExtension;
//import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
//import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
//import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
//import org.springframework.restdocs.payload.JsonFieldType;
//import org.springframework.security.test.context.support.TestExecutionEvent;
//import org.springframework.security.test.context.support.WithUserDetails;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.ResultActions;
//import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.context.WebApplicationContext;
//import org.springframework.web.filter.CharacterEncodingFilter;
//import project.volunteer.domain.image.application.ImageService;
//import project.volunteer.domain.image.dao.ImageRepository;
//import project.volunteer.domain.image.domain.Image;
//import project.volunteer.domain.recruitment.domain.VolunteerType;
//import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
//import project.volunteer.domain.recruitment.dao.RepeatPeriodRepository;
//import project.volunteer.domain.recruitment.domain.repeatPeriod.Day;
//import project.volunteer.domain.recruitment.domain.repeatPeriod.Period;
//import project.volunteer.domain.recruitment.domain.repeatPeriod.RepeatPeriod;
//import project.volunteer.domain.recruitment.domain.repeatPeriod.Week;
//import project.volunteer.global.common.component.*;
//import project.volunteer.domain.image.application.dto.ImageParam;
//import project.volunteer.domain.participation.dao.ParticipantRepository;
//import project.volunteer.domain.participation.domain.Participant;
//import project.volunteer.domain.recruitment.application.RecruitmentCommandUseCase;
//import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
//import project.volunteer.domain.recruitment.domain.Recruitment;
//import project.volunteer.domain.recruitment.domain.VolunteeringType;
//import project.volunteer.domain.image.dao.StorageRepository;
//import project.volunteer.domain.image.domain.Storage;
//import project.volunteer.domain.user.dao.UserRepository;
//import project.volunteer.domain.user.domain.Gender;
//import project.volunteer.domain.user.domain.Role;
//import project.volunteer.domain.user.domain.User;
//import project.volunteer.global.common.dto.StateResponse;
//import project.volunteer.global.infra.s3.FileService;
//import project.volunteer.document.restdocs.config.RestDocsConfiguration;
//
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.time.LocalDate;
//import java.time.LocalTime;
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
//import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
//import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
//import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
//import static org.springframework.restdocs.request.RequestDocumentation.*;
//import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import static project.volunteer.document.restdocs.util.DocumentFormatGenerator.getDateFormat;
//import static project.volunteer.document.restdocs.util.DocumentFormatGenerator.getTimeFormat;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@Transactional
//@AutoConfigureRestDocs //Rest docs 에 필요한 정보 자동 주입
//@ExtendWith(RestDocumentationExtension.class) //커스텀을 위한 RestDocumentationContextProvider 주입 받기 위해
//@Import(RestDocsConfiguration.class) //커스텀한 Rest docs 관련 bean 사용
//class RecruitmentQueryControllerTest {
//    @Autowired UserRepository userRepository;
//    @Autowired ParticipantRepository participantRepository;
//    @Autowired RecruitmentRepository recruitmentRepository;
//    @Autowired ImageRepository imageRepository;
//    @Autowired StorageRepository storageRepository;
//    @Autowired
//    RecruitmentCommandUseCase recruitmentService;
//    @Autowired RepeatPeriodRepository repeatPeriodRepository;
//    @Autowired ImageService imageService;
//    @Autowired FileService fileService;
//    @Autowired MockMvc mockMvc;
//    @Autowired RestDocumentationResultHandler restDocs;
//
//    final String AUTHORIZATION_HEADER = "accessToken";
//    private User writer;
//    private List<Recruitment> saveRecruitmentList = new ArrayList<>();
//    private List<Image> saveRecruitmentUploadImageList = new ArrayList<>();
//    private List<RepeatPeriod> saveRepeatPeriodList = new ArrayList<>();
//    private List<Participant> saveJoinApprovalnParticipantList = new ArrayList<>();
//    private List<Image> saveJoinApprovalnParticipantUploadImageList = new ArrayList<>();
//    private List<Participant> saveJoinRequestParticipantList = new ArrayList<>();
//
//    @BeforeEach
//    void setUp(final WebApplicationContext context,
//               final RestDocumentationContextProvider provider) throws IOException {
//        //커스텀 Rest docs
//        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
//                .apply(MockMvcRestDocumentation.documentationConfiguration(provider))
//                .alwaysDo(MockMvcResultHandlers.print()) //andDo(print()) 코드 포함
//                .alwaysDo(restDocs)
//                .addFilter(new CharacterEncodingFilter("UTF-8", true)) //한글 깨짐 방지
//                .build();
//
//        //작성자 저장
//        writer = User.createUser("rctfq1234", "rctfq1234", "rctfq1234", "rctfq1234", Gender.M, LocalDate.now(), "picture",
//                true, true, true, Role.USER, "kakao", "rctfq1234", null);
//        userRepository.save(writer);
//
//        //봉사 모집글 10개 저장
//        Address recruitmentAddress = Address.createAddress("11", "1111", "details", "fullName");
//        Coordinate coordinate = Coordinate.createCoordinate(3.2F, 3.2F);
//
//        Recruitment saveRecruitment1 = Recruitment.create("연탄 봉사", "test", VolunteeringCategory.ADMINSTRATION_ASSISTANCE, VolunteeringType.REG,
//                VolunteerType.TEENAGER, 10, true, "test", recruitmentAddress, coordinate,
//                new Timetable(LocalDate.now(), LocalDate.now().plusMonths(1), HourFormat.AM, LocalTime.now(), 10), true);
//        saveRecruitment1.setWriter(writer);
//        saveRecruitmentList.add(recruitmentRepository.save(saveRecruitment1));
//
//        Recruitment saveRecruitment2 = Recruitment.create("청소년 봉사", "test", VolunteeringCategory.ADMINSTRATION_ASSISTANCE, VolunteeringType.REG,
//                VolunteerType.TEENAGER, 10, true, "test", recruitmentAddress, coordinate,
//                new Timetable(LocalDate.now(), LocalDate.now().plusMonths(2), HourFormat.AM, LocalTime.now(), 10), true);
//        saveRecruitment2.setWriter(writer);
//        saveRecruitmentList.add(recruitmentRepository.save(saveRecruitment2));
//
//        Recruitment saveRecruitment3 = Recruitment.create("정기적인 봉사", "test", VolunteeringCategory.ADMINSTRATION_ASSISTANCE, VolunteeringType.REG,
//                VolunteerType.TEENAGER, 10, true, "test", recruitmentAddress, coordinate,
//                new Timetable(LocalDate.now(), LocalDate.now().plusMonths(3), HourFormat.AM, LocalTime.now(), 10), true);
//        saveRecruitment3.setWriter(writer);
//        saveRecruitmentList.add(recruitmentRepository.save(saveRecruitment3));
//
//        Recruitment saveRecruitment4 = Recruitment.create("재미있는 봉사", "test", VolunteeringCategory.ADMINSTRATION_ASSISTANCE, VolunteeringType.REG,
//                VolunteerType.TEENAGER, 10, true, "test", recruitmentAddress, coordinate,
//                new Timetable(LocalDate.now(), LocalDate.now().plusMonths(4), HourFormat.AM, LocalTime.now(), 10), true);
//        saveRecruitment4.setWriter(writer);
//        saveRecruitmentList.add(recruitmentRepository.save(saveRecruitment4));
//
//        Recruitment saveRecruitment5 = Recruitment.create("마포구 봉사활동", "test", VolunteeringCategory.ADMINSTRATION_ASSISTANCE, VolunteeringType.REG,
//                VolunteerType.TEENAGER, 10, true, "test", recruitmentAddress, coordinate,
//                new Timetable(LocalDate.now(), LocalDate.now().plusMonths(5), HourFormat.AM, LocalTime.now(), 10), true);
//        saveRecruitment5.setWriter(writer);
//        saveRecruitmentList.add(recruitmentRepository.save(saveRecruitment5));
//
//        Recruitment saveRecruitment6 = Recruitment.create("봉사 모집합니다.", "test", VolunteeringCategory.CULTURAL_EVENT, VolunteeringType.REG,
//                VolunteerType.TEENAGER, 10, true, "test", recruitmentAddress, coordinate,
//                new Timetable(LocalDate.now(), LocalDate.now().plusMonths(6), HourFormat.AM, LocalTime.now(), 10), true);
//        saveRecruitment6.setWriter(writer);
//        saveRecruitmentList.add(recruitmentRepository.save(saveRecruitment6));
//
//        Recruitment saveRecruitment7 = Recruitment.create("아무나 오세요.", "test", VolunteeringCategory.RESIDENTIAL_ENV, VolunteeringType.IRREG,
//                VolunteerType.TEENAGER, 10, true, "test", recruitmentAddress, coordinate,
//                new Timetable(LocalDate.now(), LocalDate.now().plusMonths(7), HourFormat.AM, LocalTime.now(), 10), true);
//        saveRecruitment7.setWriter(writer);
//        saveRecruitmentList.add(recruitmentRepository.save(saveRecruitment7));
//
//        Recruitment saveRecruitment8 = Recruitment.create("청소년을 도와줍시다.", "test", VolunteeringCategory.HOMELESS_DOG, VolunteeringType.IRREG,
//                VolunteerType.TEENAGER, 10, true, "test", recruitmentAddress, coordinate,
//                new Timetable(LocalDate.now(), LocalDate.now().plusMonths(8), HourFormat.AM, LocalTime.now(), 10), true);
//        saveRecruitment8.setWriter(writer);
//        saveRecruitmentList.add(recruitmentRepository.save(saveRecruitment8));
//
//        Recruitment saveRecruitment9 = Recruitment.create("아무나 모집", "test", VolunteeringCategory.FRAM_VILLAGE, VolunteeringType.IRREG,
//                VolunteerType.TEENAGER, 10, true, "test", recruitmentAddress, coordinate,
//                new Timetable(LocalDate.now(), LocalDate.now().plusMonths(9), HourFormat.AM, LocalTime.now(), 10), true);
//        saveRecruitment9.setWriter(writer);
//        saveRecruitmentList.add(recruitmentRepository.save(saveRecruitment9));
//
//        Recruitment saveRecruitment10 = Recruitment.create("모집합니다.", "test", VolunteeringCategory.HEALTH_MEDICAL, VolunteeringType.IRREG,
//                VolunteerType.TEENAGER, 10, true, "test", recruitmentAddress, coordinate,
//                new Timetable(LocalDate.now(), LocalDate.now().plusMonths(10), HourFormat.AM, LocalTime.now(), 10), true);
//        saveRecruitment10.setWriter(writer);
//        saveRecruitmentList.add(recruitmentRepository.save(saveRecruitment10));
//
//        //static & upload 이미지 저장
//        ImageParam staticImageDto1 = ImageParam.builder()
//                .code(RealWorkCode.RECRUITMENT)
//                .no(saveRecruitment1.getRecruitmentNo())
//                .uploadImage(getMockMultipartFile())
//                .build();
//        Long imageNo1 = imageService.addImage(staticImageDto1);
//        saveRecruitmentUploadImageList.add(imageRepository.findById(imageNo1).get());
//        ImageParam staticImageDto2 = ImageParam.builder()
//                .code(RealWorkCode.RECRUITMENT)
//                .no(saveRecruitment2.getRecruitmentNo())
//                .uploadImage(getMockMultipartFile())
//                .build();
//        Long imageNo2 = imageService.addImage(staticImageDto2);
//        saveRecruitmentUploadImageList.add(imageRepository.findById(imageNo2).get());
//
//        //반복 주기 저장 -> 봉사 모집글 1
//        RepeatPeriod period1 = RepeatPeriod.builder()
//                .period(Period.MONTH)
//                .week(Week.FIRST)
//                .day(Day.MON)
//                .build();
//        period1.assignRecruitment(saveRecruitment1);
//        saveRepeatPeriodList.add(repeatPeriodRepository.save(period1));
//        RepeatPeriod period2 = RepeatPeriod.builder()
//                .period(Period.MONTH)
//                .week(Week.FIRST)
//                .day(Day.TUES)
//                .build();
//        period2.assignRecruitment(saveRecruitment1);
//        saveRepeatPeriodList.add(repeatPeriodRepository.save(period2));
//
//        //팀원 & 신청자 저장 -> 봉사 모집글 1
//        User user1 = User.createUser("rctfqt", "rctfqt", "rctfqt", "rctfqt", Gender.M, LocalDate.now(), "picture",
//                true, true, true, Role.USER, "kakao", "rctfqt", null);
//        userRepository.save(user1);
//        ImageParam staticImageDto = ImageParam.builder()
//                .code(RealWorkCode.USER)
//                .no(user1.getUserNo())
//                .uploadImage(getMockMultipartFile())
//                .build();
//        Long imageNo11 = imageService.addImage(staticImageDto);
//        saveJoinApprovalnParticipantUploadImageList.add(imageRepository.findById(imageNo11).get());
//        Participant participant1 = Participant.builder()
//                .recruitment(saveRecruitment1)
//                .participant(user1)
//                .state(ParticipantState.JOIN_APPROVAL)
//                .build();
//        saveJoinApprovalnParticipantList.add(participantRepository.save(participant1));
//
//        User user2 = User.createUser("rctfqtt", "rctfqtt", "rctfqtt", "rctfqtt", Gender.M, LocalDate.now(), "picture",
//                true, true, true, Role.USER, "kakao", "rctfqtt", null);
//        userRepository.save(user2);
//        Participant participant2 = Participant.builder()
//                .recruitment(saveRecruitment1)
//                .participant(user2)
//                .state(ParticipantState.JOIN_APPROVAL)
//                .build();
//        saveJoinApprovalnParticipantList.add(participantRepository.save(participant2));
//
//        User user3 = User.createUser("rctfqttt", "rctfqttt", "rctfqttt", "rctfqttt", Gender.M, LocalDate.now(), "picture",
//                true, true, true, Role.USER, "kakao", "rctfqttt", null);
//        userRepository.save(user3);
//        Participant participant3 = Participant.builder()
//                .recruitment(saveRecruitment1)
//                .participant(user3)
//                .state(ParticipantState.JOIN_REQUEST)
//                .build();
//        saveJoinRequestParticipantList.add(participantRepository.save(participant3));
//    }
//
//    @AfterEach
//    void deleteUploadImage(){
//        List<Storage> storages = storageRepository.findAll();
//        storages.stream()
//                .forEach(s -> fileService.deleteFile(s.getFakeImageName()));
//
//        saveRecruitmentList = new ArrayList<>();
//    }
//
//    @Test
//    public void findKeywordListRecruitment() throws Exception {
//        //given
//        MultiValueMap<String,String> info = new LinkedMultiValueMap();
//        info.add("page", "0");
//        info.add("keyword", "봉사");
//
//        //when
//        ResultActions result = mockMvc.perform(get("/recruitment/search")
//                .header(AUTHORIZATION_HEADER, "access Token")
//                .params(info)
//        );
//
//        //then
//        result.andExpect(status().isOk())
//                .andExpect(jsonPath("$.recruitmentList[0].no").value(saveRecruitmentList.get(0).getRecruitmentNo()))
//                .andExpect(jsonPath("$.recruitmentList[0].volunteeringCategory").value(saveRecruitmentList.get(0).getVolunteeringCategory().getId()))
//                .andExpect(jsonPath("$.recruitmentList[0].title").value(saveRecruitmentList.get(0).getTitle()))
//                .andExpect(jsonPath("$.recruitmentList[0].sido").value(saveRecruitmentList.get(0).getAddress().getSido()))
//                .andExpect(jsonPath("$.recruitmentList[0].sigungu").value(saveRecruitmentList.get(0).getAddress().getSigungu()))
//                .andExpect(jsonPath("$.recruitmentList[0].volunteeringType").value(saveRecruitmentList.get(0).getVolunteeringType().getId()))
//                .andExpect(jsonPath("$.recruitmentList[0].volunteerType").value(saveRecruitmentList.get(0).getVolunteerType().getId()))
//                .andExpect(jsonPath("$.recruitmentList[0].isIssued").value(saveRecruitmentList.get(0).getIsIssued()))
//                .andExpect(jsonPath("$.recruitmentList[0].volunteerNum").value(saveRecruitmentList.get(0).getMaxParticipationNum()))
//                .andExpect(jsonPath("$.recruitmentList[0].currentVolunteerNum").value(2))
//                .andExpect(jsonPath("$.recruitmentList[0].picture.isStaticImage").value(false))
//                .andExpect(jsonPath("$.recruitmentList[0].picture.uploadImage").value(saveRecruitmentUploadImageList.get(0).getStorage().getImagePath()))
//                .andExpect(jsonPath("$.recruitmentList[1].no").value(saveRecruitmentList.get(1).getRecruitmentNo()))
//                .andExpect(jsonPath("$.recruitmentList[1].currentVolunteerNum").value(0))
//                .andExpect(jsonPath("$.recruitmentList[1].picture.isStaticImage").value(false))
//                .andExpect(jsonPath("$.recruitmentList[1].picture.uploadImage").value(saveRecruitmentUploadImageList.get(1).getStorage().getImagePath()))
//                .andExpect(jsonPath("$.recruitmentList[2].no").value(saveRecruitmentList.get(2).getRecruitmentNo()))
//                .andExpect(jsonPath("$.recruitmentList[2].currentVolunteerNum").value(0))
//                .andExpect(jsonPath("$.recruitmentList[2].picture.isStaticImage").value(true))
//                .andExpect(jsonPath("$.recruitmentList[3].no").value(saveRecruitmentList.get(3).getRecruitmentNo()))
//                .andExpect(jsonPath("$.recruitmentList[3].currentVolunteerNum").value(0))
//                .andExpect(jsonPath("$.recruitmentList[3].picture.isStaticImage").value(true))
//                .andExpect(jsonPath("$.recruitmentList[4].no").value(saveRecruitmentList.get(4).getRecruitmentNo()))
//                .andExpect(jsonPath("$.recruitmentList[4].currentVolunteerNum").value(0))
//                .andExpect(jsonPath("$.recruitmentList[4].picture.isStaticImage").value(true))
//                .andExpect(jsonPath("$.recruitmentList[5].no").value(saveRecruitmentList.get(5).getRecruitmentNo()))
//                .andExpect(jsonPath("$.recruitmentList[5].currentVolunteerNum").value(0))
//                .andExpect(jsonPath("$.recruitmentList[5].picture.isStaticImage").value(true))
//                .andExpect(jsonPath("$.isLast").value(true))
//                .andExpect(jsonPath("$.lastId").value(saveRecruitmentList.get(5).getRecruitmentNo()))
//                .andDo(
//                        restDocs.document(
//                                requestHeaders(
//                                        headerWithName(AUTHORIZATION_HEADER).optional().description("JWT Access Token")
//                                ),
//                                requestParameters(
//                                        parameterWithName("page").optional().description("페이지 번호"),
//                                        parameterWithName("keyword").description("검색 키워드")
//                                ),
//                                responseFields(
//                                        fieldWithPath("isLast").type(JsonFieldType.BOOLEAN).description("마지막 봉사 모집글 유무"),
//                                        fieldWithPath("lastId").type(JsonFieldType.NUMBER).description("응답 봉사 모집글 리스트 중 마지막 모집글 고유키 PK"),
//                                        fieldWithPath("recruitmentList").type(JsonFieldType.ARRAY).description("봉사 모집글 리스트")
//                                ).andWithPrefix("recruitmentList.[].",
//                                        fieldWithPath("no").type(JsonFieldType.NUMBER).description("봉사 모집글 고유키 PK"),
//                                        fieldWithPath("volunteeringCategory").type(JsonFieldType.STRING).description("Code VolunteeringCategory 참고바람"),
//                                        fieldWithPath("picture.isStaticImage").type(JsonFieldType.BOOLEAN).description("정적/동적 이미지 구분"),
//                                        fieldWithPath("picture.uploadImage").type(JsonFieldType.STRING).optional().description("업로드 이미지 URL, isStaticImage True 일 경우 NULL"),
//                                        fieldWithPath("title").type(JsonFieldType.STRING).description("봉사 모집글 제목"),
//                                        fieldWithPath("sido").type(JsonFieldType.STRING).description("시/구 코드"),
//                                        fieldWithPath("sigungu").type(JsonFieldType.STRING).description("시/군/구 코드"),
//                                        fieldWithPath("startDay").type(JsonFieldType.STRING).attributes(getDateFormat()).description("봉사 모집 시작 날짜"),
//                                        fieldWithPath("endDay").type(JsonFieldType.STRING).attributes(getDateFormat()).description("봉사 모집 종료 날짜"),
//                                        fieldWithPath("volunteeringType").type(JsonFieldType.STRING).description("Code VolunteeringType 참고바람"),
//                                        fieldWithPath("isIssued").type(JsonFieldType.BOOLEAN).description("봉사 시간 인증 가능 여부"),
//                                        fieldWithPath("volunteerNum").type(JsonFieldType.NUMBER).description("봉사 모집 인원"),
//                                        fieldWithPath("currentVolunteerNum").type(JsonFieldType.NUMBER).description("현재 봉사 모집글 참여(승인된) 인원"),
//                                        fieldWithPath("volunteerType").type(JsonFieldType.STRING).description("Code VolunteerType 참고바람."))
//                        )
//                );
//    }
//
//    @Test
//    @Disabled
//    public void 모집글_전체카운트_빈필터링_성공() throws Exception {
//        //init
//        mockMvc.perform(get("/recruitment/count"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("totalCnt").value(10));
//    }
//
//    private MockMultipartFile getMockMultipartFile() throws IOException {
//        return new MockMultipartFile(
//                "file", "file.PNG", "image/jpg", new FileInputStream("src/main/resources/static/test/file.PNG"));
//    }
//}