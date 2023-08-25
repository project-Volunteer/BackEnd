package project.volunteer.domain.recruitment.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import project.volunteer.domain.image.application.ImageService;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.dao.RepeatPeriodRepository;
import project.volunteer.domain.recruitment.domain.Day;
import project.volunteer.domain.recruitment.domain.Period;
import project.volunteer.domain.recruitment.domain.RepeatPeriod;
import project.volunteer.domain.recruitment.domain.Week;
import project.volunteer.global.common.component.*;
import project.volunteer.domain.image.application.dto.ImageParam;
import project.volunteer.domain.participation.dao.ParticipantRepository;
import project.volunteer.domain.participation.domain.Participant;
import project.volunteer.domain.recruitment.application.RecruitmentService;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.image.dao.StorageRepository;
import project.volunteer.domain.image.domain.Storage;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.dto.StateResponse;
import project.volunteer.global.infra.s3.FileService;
import project.volunteer.restdocs.document.config.RestDocsConfiguration;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static project.volunteer.restdocs.document.util.DocumentFormatGenerator.getDateFormat;
import static project.volunteer.restdocs.document.util.DocumentFormatGenerator.getTimeFormat;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@AutoConfigureRestDocs //Rest docs 에 필요한 정보 자동 주입
@ExtendWith(RestDocumentationExtension.class) //커스텀을 위한 RestDocumentationContextProvider 주입 받기 위해
@Import(RestDocsConfiguration.class) //커스텀한 Rest docs 관련 bean 사용
class RecruitmentQueryControllerTest {
    @Autowired UserRepository userRepository;
    @Autowired ParticipantRepository participantRepository;
    @Autowired RecruitmentRepository recruitmentRepository;
    @Autowired ImageRepository imageRepository;
    @Autowired StorageRepository storageRepository;
    @Autowired RecruitmentService recruitmentService;
    @Autowired RepeatPeriodRepository repeatPeriodRepository;
    @Autowired ImageService imageService;
    @Autowired FileService fileService;
    @Autowired MockMvc mockMvc;
    @Autowired RestDocumentationResultHandler restDocs;

    final String AUTHORIZATION_HEADER = "accessToken";
    private User writer;
    private List<Recruitment> saveRecruitmentList = new ArrayList<>();
    private List<Image> saveRecruitmentUploadImageList = new ArrayList<>();
    private List<RepeatPeriod> saveRepeatPeriodList = new ArrayList<>();
    private List<Participant> saveJoinApprovalnParticipantList = new ArrayList<>();
    private List<Image> saveJoinApprovalnParticipantUploadImageList = new ArrayList<>();
    private List<Participant> saveJoinRequestParticipantList = new ArrayList<>();

    @BeforeEach
    void setUp(final WebApplicationContext context,
               final RestDocumentationContextProvider provider) throws IOException {
        //커스텀 Rest docs
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(MockMvcRestDocumentation.documentationConfiguration(provider))
                .alwaysDo(MockMvcResultHandlers.print()) //andDo(print()) 코드 포함
                .alwaysDo(restDocs)
                .addFilter(new CharacterEncodingFilter("UTF-8", true)) //한글 깨짐 방지
                .build();

        //작성자 저장
        writer = User.createUser("rctfq1234", "rctfq1234", "rctfq1234", "rctfq1234", Gender.M, LocalDate.now(), "picture",
                true, true, true, Role.USER, "kakao", "rctfq1234", null);
        userRepository.save(writer);

        //봉사 모집글 10개 저장
        Address recruitmentAddress = Address.createAddress("11", "1111", "details");
        Coordinate coordinate = Coordinate.createCoordinate(3.2F, 3.2F);

        Recruitment saveRecruitment1 = Recruitment.createRecruitment("연탄 봉사", "test", VolunteeringCategory.ADMINSTRATION_ASSISTANCE, VolunteeringType.REG,
                VolunteerType.TEENAGER, 10, true, "test", recruitmentAddress, coordinate,
                Timetable.createTimetable(LocalDate.now(), LocalDate.now().plusMonths(1), HourFormat.AM, LocalTime.now(), 10), true);
        saveRecruitment1.setWriter(writer);
        saveRecruitmentList.add(recruitmentRepository.save(saveRecruitment1));

        Recruitment saveRecruitment2 = Recruitment.createRecruitment("청소년 봉사", "test", VolunteeringCategory.ADMINSTRATION_ASSISTANCE, VolunteeringType.REG,
                VolunteerType.TEENAGER, 10, true, "test", recruitmentAddress, coordinate,
                Timetable.createTimetable(LocalDate.now(), LocalDate.now().plusMonths(2), HourFormat.AM, LocalTime.now(), 10), true);
        saveRecruitment2.setWriter(writer);
        saveRecruitmentList.add(recruitmentRepository.save(saveRecruitment2));

        Recruitment saveRecruitment3 = Recruitment.createRecruitment("정기적인 봉사", "test", VolunteeringCategory.ADMINSTRATION_ASSISTANCE, VolunteeringType.REG,
                VolunteerType.TEENAGER, 10, true, "test", recruitmentAddress, coordinate,
                Timetable.createTimetable(LocalDate.now(), LocalDate.now().plusMonths(3), HourFormat.AM, LocalTime.now(), 10), true);
        saveRecruitment3.setWriter(writer);
        saveRecruitmentList.add(recruitmentRepository.save(saveRecruitment3));

        Recruitment saveRecruitment4 = Recruitment.createRecruitment("재미있는 봉사", "test", VolunteeringCategory.ADMINSTRATION_ASSISTANCE, VolunteeringType.REG,
                VolunteerType.TEENAGER, 10, true, "test", recruitmentAddress, coordinate,
                Timetable.createTimetable(LocalDate.now(), LocalDate.now().plusMonths(4), HourFormat.AM, LocalTime.now(), 10), true);
        saveRecruitment4.setWriter(writer);
        saveRecruitmentList.add(recruitmentRepository.save(saveRecruitment4));

        Recruitment saveRecruitment5 = Recruitment.createRecruitment("마포구 봉사활동", "test", VolunteeringCategory.ADMINSTRATION_ASSISTANCE, VolunteeringType.REG,
                VolunteerType.TEENAGER, 10, true, "test", recruitmentAddress, coordinate,
                Timetable.createTimetable(LocalDate.now(), LocalDate.now().plusMonths(5), HourFormat.AM, LocalTime.now(), 10), true);
        saveRecruitment5.setWriter(writer);
        saveRecruitmentList.add(recruitmentRepository.save(saveRecruitment5));

        Recruitment saveRecruitment6 = Recruitment.createRecruitment("봉사 모집합니다.", "test", VolunteeringCategory.CULTURAL_EVENT, VolunteeringType.REG,
                VolunteerType.TEENAGER, 10, true, "test", recruitmentAddress, coordinate,
                Timetable.createTimetable(LocalDate.now(), LocalDate.now().plusMonths(6), HourFormat.AM, LocalTime.now(), 10), true);
        saveRecruitment6.setWriter(writer);
        saveRecruitmentList.add(recruitmentRepository.save(saveRecruitment6));

        Recruitment saveRecruitment7 = Recruitment.createRecruitment("아무나 오세요.", "test", VolunteeringCategory.RESIDENTIAL_ENV, VolunteeringType.IRREG,
                VolunteerType.TEENAGER, 10, true, "test", recruitmentAddress, coordinate,
                Timetable.createTimetable(LocalDate.now(), LocalDate.now().plusMonths(7), HourFormat.AM, LocalTime.now(), 10), true);
        saveRecruitment7.setWriter(writer);
        saveRecruitmentList.add(recruitmentRepository.save(saveRecruitment7));

        Recruitment saveRecruitment8 = Recruitment.createRecruitment("청소년을 도와줍시다.", "test", VolunteeringCategory.HOMELESS_DOG, VolunteeringType.IRREG,
                VolunteerType.TEENAGER, 10, true, "test", recruitmentAddress, coordinate,
                Timetable.createTimetable(LocalDate.now(), LocalDate.now().plusMonths(8), HourFormat.AM, LocalTime.now(), 10), true);
        saveRecruitment8.setWriter(writer);
        saveRecruitmentList.add(recruitmentRepository.save(saveRecruitment8));

        Recruitment saveRecruitment9 = Recruitment.createRecruitment("아무나 모집", "test", VolunteeringCategory.FRAM_VILLAGE, VolunteeringType.IRREG,
                VolunteerType.TEENAGER, 10, true, "test", recruitmentAddress, coordinate,
                Timetable.createTimetable(LocalDate.now(), LocalDate.now().plusMonths(9), HourFormat.AM, LocalTime.now(), 10), true);
        saveRecruitment9.setWriter(writer);
        saveRecruitmentList.add(recruitmentRepository.save(saveRecruitment9));

        Recruitment saveRecruitment10 = Recruitment.createRecruitment("모집합니다.", "test", VolunteeringCategory.HEALTH_MEDICAL, VolunteeringType.IRREG,
                VolunteerType.TEENAGER, 10, true, "test", recruitmentAddress, coordinate,
                Timetable.createTimetable(LocalDate.now(), LocalDate.now().plusMonths(10), HourFormat.AM, LocalTime.now(), 10), true);
        saveRecruitment10.setWriter(writer);
        saveRecruitmentList.add(recruitmentRepository.save(saveRecruitment10));

        //static & upload 이미지 저장
        ImageParam staticImageDto1 = ImageParam.builder()
                .code(RealWorkCode.RECRUITMENT)
                .no(saveRecruitment1.getRecruitmentNo())
                .uploadImage(getMockMultipartFile())
                .build();
        Long imageNo1 = imageService.addImage(staticImageDto1);
        saveRecruitmentUploadImageList.add(imageRepository.findById(imageNo1).get());
        ImageParam staticImageDto2 = ImageParam.builder()
                .code(RealWorkCode.RECRUITMENT)
                .no(saveRecruitment2.getRecruitmentNo())
                .uploadImage(getMockMultipartFile())
                .build();
        Long imageNo2 = imageService.addImage(staticImageDto2);
        saveRecruitmentUploadImageList.add(imageRepository.findById(imageNo2).get());

        //반복 주기 저장 -> 봉사 모집글 1
        RepeatPeriod period1 = RepeatPeriod.builder()
                .period(Period.MONTH)
                .week(Week.FIRST)
                .day(Day.MON)
                .build();
        period1.setRecruitment(saveRecruitment1);
        saveRepeatPeriodList.add(repeatPeriodRepository.save(period1));
        RepeatPeriod period2 = RepeatPeriod.builder()
                .period(Period.MONTH)
                .week(Week.FIRST)
                .day(Day.TUES)
                .build();
        period2.setRecruitment(saveRecruitment1);
        saveRepeatPeriodList.add(repeatPeriodRepository.save(period2));

        //팀원 & 신청자 저장 -> 봉사 모집글 1
        User user1 = User.createUser("rctfqt", "rctfqt", "rctfqt", "rctfqt", Gender.M, LocalDate.now(), "picture",
                true, true, true, Role.USER, "kakao", "rctfqt", null);
        userRepository.save(user1);
        ImageParam staticImageDto = ImageParam.builder()
                .code(RealWorkCode.USER)
                .no(user1.getUserNo())
                .uploadImage(getMockMultipartFile())
                .build();
        Long imageNo11 = imageService.addImage(staticImageDto);
        saveJoinApprovalnParticipantUploadImageList.add(imageRepository.findById(imageNo11).get());
        Participant participant1 = Participant.builder()
                .recruitment(saveRecruitment1)
                .participant(user1)
                .state(ParticipantState.JOIN_APPROVAL)
                .build();
        saveJoinApprovalnParticipantList.add(participantRepository.save(participant1));

        User user2 = User.createUser("rctfqtt", "rctfqtt", "rctfqtt", "rctfqtt", Gender.M, LocalDate.now(), "picture",
                true, true, true, Role.USER, "kakao", "rctfqtt", null);
        userRepository.save(user2);
        Participant participant2 = Participant.builder()
                .recruitment(saveRecruitment1)
                .participant(user2)
                .state(ParticipantState.JOIN_APPROVAL)
                .build();
        saveJoinApprovalnParticipantList.add(participantRepository.save(participant2));

        User user3 = User.createUser("rctfqttt", "rctfqttt", "rctfqttt", "rctfqttt", Gender.M, LocalDate.now(), "picture",
                true, true, true, Role.USER, "kakao", "rctfqttt", null);
        userRepository.save(user3);
        Participant participant3 = Participant.builder()
                .recruitment(saveRecruitment1)
                .participant(user3)
                .state(ParticipantState.JOIN_REQUEST)
                .build();
        saveJoinRequestParticipantList.add(participantRepository.save(participant3));
    }

    @AfterEach
    void deleteUploadImage(){
        List<Storage> storages = storageRepository.findAll();
        storages.stream()
                .forEach(s -> fileService.deleteFile(s.getFakeImageName()));

        saveRecruitmentList = new ArrayList<>();
    }

    @Test
    public void findListRecruitment() throws Exception {
        //given
        MultiValueMap<String,String> info = new LinkedMultiValueMap();
        info.add("page", "0");
        info.add("volunteering_category", "001");
        info.add("volunteering_category", "002");
        info.add("sido", "11");
        info.add("sigungu","1111");
        info.add("volunteering_type", VolunteeringType.REG.getId());
        info.add("volunteer_type", VolunteerType.TEENAGER.getId());
        info.add("is_issued", "true");

        //when
        ResultActions result = mockMvc.perform(get("/recruitment")
                .header(AUTHORIZATION_HEADER, "access Token")
                .params(info)
        );

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.recruitmentList[0].no").value(saveRecruitmentList.get(0).getRecruitmentNo()))
                .andExpect(jsonPath("$.recruitmentList[0].volunteeringCategory").value(saveRecruitmentList.get(0).getVolunteeringCategory().getId()))
                .andExpect(jsonPath("$.recruitmentList[0].title").value(saveRecruitmentList.get(0).getTitle()))
                .andExpect(jsonPath("$.recruitmentList[0].sido").value(saveRecruitmentList.get(0).getAddress().getSido()))
                .andExpect(jsonPath("$.recruitmentList[0].sigungu").value(saveRecruitmentList.get(0).getAddress().getSigungu()))
                .andExpect(jsonPath("$.recruitmentList[0].volunteeringType").value(saveRecruitmentList.get(0).getVolunteeringType().getId()))
                .andExpect(jsonPath("$.recruitmentList[0].volunteerType").value(saveRecruitmentList.get(0).getVolunteerType().getId()))
                .andExpect(jsonPath("$.recruitmentList[0].isIssued").value(saveRecruitmentList.get(0).getIsIssued()))
                .andExpect(jsonPath("$.recruitmentList[0].volunteerNum").value(saveRecruitmentList.get(0).getVolunteerNum()))
                .andExpect(jsonPath("$.recruitmentList[0].currentVolunteerNum").value(2))
                .andExpect(jsonPath("$.recruitmentList[0].picture.isStaticImage").value(false))
                .andExpect(jsonPath("$.recruitmentList[0].picture.uploadImage").value(saveRecruitmentUploadImageList.get(0).getStorage().getImagePath()))
                .andExpect(jsonPath("$.recruitmentList[1].no").value(saveRecruitmentList.get(1).getRecruitmentNo()))
                .andExpect(jsonPath("$.recruitmentList[1].currentVolunteerNum").value(0))
                .andExpect(jsonPath("$.recruitmentList[1].picture.isStaticImage").value(false))
                .andExpect(jsonPath("$.recruitmentList[1].picture.uploadImage").value(saveRecruitmentUploadImageList.get(1).getStorage().getImagePath()))
                .andExpect(jsonPath("$.recruitmentList[2].no").value(saveRecruitmentList.get(2).getRecruitmentNo()))
                .andExpect(jsonPath("$.recruitmentList[2].currentVolunteerNum").value(0))
                .andExpect(jsonPath("$.recruitmentList[2].picture.isStaticImage").value(true))
                .andExpect(jsonPath("$.recruitmentList[3].no").value(saveRecruitmentList.get(3).getRecruitmentNo()))
                .andExpect(jsonPath("$.recruitmentList[3].currentVolunteerNum").value(0))
                .andExpect(jsonPath("$.recruitmentList[3].picture.isStaticImage").value(true))
                .andExpect(jsonPath("$.recruitmentList[4].no").value(saveRecruitmentList.get(4).getRecruitmentNo()))
                .andExpect(jsonPath("$.recruitmentList[4].currentVolunteerNum").value(0))
                .andExpect(jsonPath("$.recruitmentList[4].picture.isStaticImage").value(true))
                .andExpect(jsonPath("$.recruitmentList[5].no").value(saveRecruitmentList.get(5).getRecruitmentNo()))
                .andExpect(jsonPath("$.recruitmentList[5].currentVolunteerNum").value(0))
                .andExpect(jsonPath("$.recruitmentList[5].picture.isStaticImage").value(true))
                .andExpect(jsonPath("$.isLast").value(true))
                .andExpect(jsonPath("$.lastId").value(saveRecruitmentList.get(5).getRecruitmentNo()))
                .andDo(
                        restDocs.document(
                                requestHeaders(
                                        headerWithName(AUTHORIZATION_HEADER).optional().description("JWT Access Token")
                                ),
                                requestParameters(
                                        parameterWithName("page").optional().description("페이지 번호"),
                                        parameterWithName("volunteering_category").optional().description("Code VolunteeringCategory 참고바람(다중 선택 가능)"),
                                        parameterWithName("sido").optional().description("시/도 코드"),
                                        parameterWithName("sigungu").optional().description("시/군/구 코드"),
                                        parameterWithName("volunteering_type").optional().description("Code VolunteeringType 참고바람."),
                                        parameterWithName("volunteer_type").optional().description("Code VolunteerType 참고바람."),
                                        parameterWithName("is_issued").optional().description("봉사 시간 인증 가능 여부")
                                ),
                                responseFields(
                                        fieldWithPath("isLast").type(JsonFieldType.BOOLEAN).description("마지막 봉사 모집글 유무"),
                                        fieldWithPath("lastId").type(JsonFieldType.NUMBER).description("응답 봉사 모집글 리스트 중 마지막 모집글 고유키 PK"),
                                        fieldWithPath("recruitmentList").type(JsonFieldType.ARRAY).description("봉사 모집글 리스트")
                                ).andWithPrefix("recruitmentList.[].",
                                        fieldWithPath("no").type(JsonFieldType.NUMBER).description("봉사 모집글 고유키 PK"),
                                        fieldWithPath("volunteeringCategory").type(JsonFieldType.STRING).description("Code VolunteeringCategory 참고바람"),
                                        fieldWithPath("picture.isStaticImage").type(JsonFieldType.BOOLEAN).description("정적/동적 이미지 구분"),
                                        fieldWithPath("picture.uploadImage").type(JsonFieldType.STRING).optional().description("업로드 이미지 URL, isStaticImage True 일 경우 NULL"),
                                        fieldWithPath("title").type(JsonFieldType.STRING).description("봉사 모집글 제목"),
                                        fieldWithPath("sido").type(JsonFieldType.STRING).description("시/구 코드"),
                                        fieldWithPath("sigungu").type(JsonFieldType.STRING).description("시/군/구 코드"),
                                        fieldWithPath("startDay").type(JsonFieldType.STRING).attributes(getDateFormat()).description("봉사 모집 시작 날짜"),
                                        fieldWithPath("endDay").type(JsonFieldType.STRING).attributes(getDateFormat()).description("봉사 모집 종료 날짜"),
                                        fieldWithPath("volunteeringType").type(JsonFieldType.STRING).description("Code VolunteeringType 참고바람"),
                                        fieldWithPath("isIssued").type(JsonFieldType.BOOLEAN).description("봉사 시간 인증 가능 여부"),
                                        fieldWithPath("volunteerNum").type(JsonFieldType.NUMBER).description("봉사 모집 인원"),
                                        fieldWithPath("currentVolunteerNum").type(JsonFieldType.NUMBER).description("현재 봉사 모집글 참여(승인된) 인원"),
                                        fieldWithPath("volunteerType").type(JsonFieldType.STRING).description("Code VolunteerType 참고바람."))
                        )
                );
    }

    @Test
    public void findKeywordListRecruitment() throws Exception {
        //given
        MultiValueMap<String,String> info = new LinkedMultiValueMap();
        info.add("page", "0");
        info.add("keyword", "봉사");

        //when
        ResultActions result = mockMvc.perform(get("/recruitment/search")
                .header(AUTHORIZATION_HEADER, "access Token")
                .params(info)
        );

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.recruitmentList[0].no").value(saveRecruitmentList.get(0).getRecruitmentNo()))
                .andExpect(jsonPath("$.recruitmentList[0].volunteeringCategory").value(saveRecruitmentList.get(0).getVolunteeringCategory().getId()))
                .andExpect(jsonPath("$.recruitmentList[0].title").value(saveRecruitmentList.get(0).getTitle()))
                .andExpect(jsonPath("$.recruitmentList[0].sido").value(saveRecruitmentList.get(0).getAddress().getSido()))
                .andExpect(jsonPath("$.recruitmentList[0].sigungu").value(saveRecruitmentList.get(0).getAddress().getSigungu()))
                .andExpect(jsonPath("$.recruitmentList[0].volunteeringType").value(saveRecruitmentList.get(0).getVolunteeringType().getId()))
                .andExpect(jsonPath("$.recruitmentList[0].volunteerType").value(saveRecruitmentList.get(0).getVolunteerType().getId()))
                .andExpect(jsonPath("$.recruitmentList[0].isIssued").value(saveRecruitmentList.get(0).getIsIssued()))
                .andExpect(jsonPath("$.recruitmentList[0].volunteerNum").value(saveRecruitmentList.get(0).getVolunteerNum()))
                .andExpect(jsonPath("$.recruitmentList[0].currentVolunteerNum").value(2))
                .andExpect(jsonPath("$.recruitmentList[0].picture.isStaticImage").value(false))
                .andExpect(jsonPath("$.recruitmentList[0].picture.uploadImage").value(saveRecruitmentUploadImageList.get(0).getStorage().getImagePath()))
                .andExpect(jsonPath("$.recruitmentList[1].no").value(saveRecruitmentList.get(1).getRecruitmentNo()))
                .andExpect(jsonPath("$.recruitmentList[1].currentVolunteerNum").value(0))
                .andExpect(jsonPath("$.recruitmentList[1].picture.isStaticImage").value(false))
                .andExpect(jsonPath("$.recruitmentList[1].picture.uploadImage").value(saveRecruitmentUploadImageList.get(1).getStorage().getImagePath()))
                .andExpect(jsonPath("$.recruitmentList[2].no").value(saveRecruitmentList.get(2).getRecruitmentNo()))
                .andExpect(jsonPath("$.recruitmentList[2].currentVolunteerNum").value(0))
                .andExpect(jsonPath("$.recruitmentList[2].picture.isStaticImage").value(true))
                .andExpect(jsonPath("$.recruitmentList[3].no").value(saveRecruitmentList.get(3).getRecruitmentNo()))
                .andExpect(jsonPath("$.recruitmentList[3].currentVolunteerNum").value(0))
                .andExpect(jsonPath("$.recruitmentList[3].picture.isStaticImage").value(true))
                .andExpect(jsonPath("$.recruitmentList[4].no").value(saveRecruitmentList.get(4).getRecruitmentNo()))
                .andExpect(jsonPath("$.recruitmentList[4].currentVolunteerNum").value(0))
                .andExpect(jsonPath("$.recruitmentList[4].picture.isStaticImage").value(true))
                .andExpect(jsonPath("$.recruitmentList[5].no").value(saveRecruitmentList.get(5).getRecruitmentNo()))
                .andExpect(jsonPath("$.recruitmentList[5].currentVolunteerNum").value(0))
                .andExpect(jsonPath("$.recruitmentList[5].picture.isStaticImage").value(true))
                .andExpect(jsonPath("$.isLast").value(true))
                .andExpect(jsonPath("$.lastId").value(saveRecruitmentList.get(5).getRecruitmentNo()))
                .andDo(
                        restDocs.document(
                                requestHeaders(
                                        headerWithName(AUTHORIZATION_HEADER).optional().description("JWT Access Token")
                                ),
                                requestParameters(
                                        parameterWithName("page").optional().description("페이지 번호"),
                                        parameterWithName("keyword").description("검색 키워드")
                                ),
                                responseFields(
                                        fieldWithPath("isLast").type(JsonFieldType.BOOLEAN).description("마지막 봉사 모집글 유무"),
                                        fieldWithPath("lastId").type(JsonFieldType.NUMBER).description("응답 봉사 모집글 리스트 중 마지막 모집글 고유키 PK"),
                                        fieldWithPath("recruitmentList").type(JsonFieldType.ARRAY).description("봉사 모집글 리스트")
                                ).andWithPrefix("recruitmentList.[].",
                                        fieldWithPath("no").type(JsonFieldType.NUMBER).description("봉사 모집글 고유키 PK"),
                                        fieldWithPath("volunteeringCategory").type(JsonFieldType.STRING).description("Code VolunteeringCategory 참고바람"),
                                        fieldWithPath("picture.isStaticImage").type(JsonFieldType.BOOLEAN).description("정적/동적 이미지 구분"),
                                        fieldWithPath("picture.uploadImage").type(JsonFieldType.STRING).optional().description("업로드 이미지 URL, isStaticImage True 일 경우 NULL"),
                                        fieldWithPath("title").type(JsonFieldType.STRING).description("봉사 모집글 제목"),
                                        fieldWithPath("sido").type(JsonFieldType.STRING).description("시/구 코드"),
                                        fieldWithPath("sigungu").type(JsonFieldType.STRING).description("시/군/구 코드"),
                                        fieldWithPath("startDay").type(JsonFieldType.STRING).attributes(getDateFormat()).description("봉사 모집 시작 날짜"),
                                        fieldWithPath("endDay").type(JsonFieldType.STRING).attributes(getDateFormat()).description("봉사 모집 종료 날짜"),
                                        fieldWithPath("volunteeringType").type(JsonFieldType.STRING).description("Code VolunteeringType 참고바람"),
                                        fieldWithPath("isIssued").type(JsonFieldType.BOOLEAN).description("봉사 시간 인증 가능 여부"),
                                        fieldWithPath("volunteerNum").type(JsonFieldType.NUMBER).description("봉사 모집 인원"),
                                        fieldWithPath("currentVolunteerNum").type(JsonFieldType.NUMBER).description("현재 봉사 모집글 참여(승인된) 인원"),
                                        fieldWithPath("volunteerType").type(JsonFieldType.STRING).description("Code VolunteerType 참고바람."))
                        )
                );
    }

    @Test
    public void findCountRecruitment() throws Exception {
        //given
        MultiValueMap<String,String> info = new LinkedMultiValueMap();
        info.add("volunteering_category", "001");
        info.add("volunteering_category", "002");
        info.add("sido", "11");
        info.add("sigungu","1111");
        info.add("volunteering_type", VolunteeringType.REG.getId());
        info.add("volunteer_type", VolunteerType.TEENAGER.getId());
        info.add("is_issued", "true");

        //when
        ResultActions result = mockMvc.perform(get("/recruitment/count")
                .header(AUTHORIZATION_HEADER, "access Token")
                .params(info)
        );

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("totalCnt").value(6))
                .andDo(
                        restDocs.document(
                                requestHeaders(
                                        headerWithName(AUTHORIZATION_HEADER).optional().description("JWT Access Token")
                                ),
                                requestParameters(
                                        parameterWithName("volunteering_category").optional().description("Code VolunteeringCategory 참고바람(다중 선택 가능)"),
                                        parameterWithName("sido").optional().description("시/도 코드"),
                                        parameterWithName("sigungu").optional().description("시/군/구 코드"),
                                        parameterWithName("volunteering_type").optional().description("Code VolunteeringType 참고바람."),
                                        parameterWithName("volunteer_type").optional().description("Code VolunteerType 참고바람."),
                                        parameterWithName("is_issued").optional().description("봉사 시간 인증 가능 여부")
                                ),
                                responseFields(
                                     fieldWithPath("totalCnt").type(JsonFieldType.NUMBER).description("필터링된 봉사 모집글 개수")
                                )
                        )
                );
    }

    @Test
    @Disabled
    public void 모집글_전체카운트_빈필터링_성공() throws Exception {
        //init
        mockMvc.perform(get("/recruitment/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("totalCnt").value(10));
    }

    @Test
    public void findDetailsRecruitment() throws Exception {
        //given & then
        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.get("/recruitment/{no}", saveRecruitmentList.get(0).getRecruitmentNo())
                .header(AUTHORIZATION_HEADER, "access Token")
        );

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.recruitment.no").value(saveRecruitmentList.get(0).getRecruitmentNo()))
                .andExpect(jsonPath("$.recruitment.volunteeringCategory").value(saveRecruitmentList.get(0).getVolunteeringCategory().getId()))
                .andExpect(jsonPath("$.recruitment.organizationName").value(saveRecruitmentList.get(0).getOrganizationName()))
                .andExpect(jsonPath("$.recruitment.address.sido").value(saveRecruitmentList.get(0).getAddress().getSido()))
                .andExpect(jsonPath("$.recruitment.address.sigungu").value(saveRecruitmentList.get(0).getAddress().getSigungu()))
                .andExpect(jsonPath("$.recruitment.address.details").value(saveRecruitmentList.get(0).getAddress().getDetails()))
                .andExpect(jsonPath("$.recruitment.address.latitude").value(saveRecruitmentList.get(0).getCoordinate().getLatitude()))
                .andExpect(jsonPath("$.recruitment.address.longitude").value(saveRecruitmentList.get(0).getCoordinate().getLongitude()))
                .andExpect(jsonPath("$.recruitment.isIssued").value(saveRecruitmentList.get(0).getIsIssued()))
                .andExpect(jsonPath("$.recruitment.volunteeringType").value(saveRecruitmentList.get(0).getVolunteeringType().getId()))
                .andExpect(jsonPath("$.recruitment.volunteerType").value(saveRecruitmentList.get(0).getVolunteerType().getId()))
                .andExpect(jsonPath("$.recruitment.volunteerNum").value(saveRecruitmentList.get(0).getVolunteerNum()))
                .andExpect(jsonPath("$.recruitment.title").value(saveRecruitmentList.get(0).getTitle()))
                .andExpect(jsonPath("$.recruitment.content").value(saveRecruitmentList.get(0).getContent()))
                .andExpect(jsonPath("$.approvalVolunteer[0].userNo").value(saveJoinApprovalnParticipantList.get(0).getParticipant().getUserNo()))
                .andExpect(jsonPath("$.approvalVolunteer[0].nickName").value(saveJoinApprovalnParticipantList.get(0).getParticipant().getNickName()))
                .andExpect(jsonPath("$.approvalVolunteer[0].imageUrl").value(saveJoinApprovalnParticipantUploadImageList.get(0).getStorage().getImagePath()))
                .andExpect(jsonPath("$.approvalVolunteer[1].userNo").value(saveJoinApprovalnParticipantList.get(1).getParticipant().getUserNo()))
                .andExpect(jsonPath("$.approvalVolunteer[1].nickName").value(saveJoinApprovalnParticipantList.get(1).getParticipant().getNickName()))
                .andExpect(jsonPath("$.approvalVolunteer[1].imageUrl").value(saveJoinApprovalnParticipantList.get(1).getParticipant().getPicture()))
                .andExpect(jsonPath("$.requiredVolunteer[0].userNo").value(saveJoinRequestParticipantList.get(0).getParticipant().getUserNo()))
                .andExpect(jsonPath("$.requiredVolunteer[0].nickName").value(saveJoinRequestParticipantList.get(0).getParticipant().getNickName()))
                .andExpect(jsonPath("$.requiredVolunteer[0].imageUrl").value(saveJoinRequestParticipantList.get(0).getParticipant().getPicture()))
                .andExpect(jsonPath("$.recruitment.author.nickName").value(writer.getNickName()))
                .andExpect(jsonPath("$.recruitment.author.imageUrl").value(writer.getPicture()))
                .andExpect(jsonPath("$.recruitment.repeatPeriod.period").value(saveRepeatPeriodList.get(0).getPeriod().getId()))
                .andExpect(jsonPath("$.recruitment.repeatPeriod.week").value(saveRepeatPeriodList.get(0).getWeek().getId()))
                .andExpect(jsonPath("$.recruitment.repeatPeriod.days[0]").value(saveRepeatPeriodList.get(0).getDay().getId()))
                .andExpect(jsonPath("$.recruitment.repeatPeriod.days[1]").value(saveRepeatPeriodList.get(1).getDay().getId()))
                .andExpect(jsonPath("$.recruitment.picture.isStaticImage").value(false))
                .andExpect(jsonPath("$.recruitment.picture.uploadImage").value(saveRecruitmentUploadImageList.get(0).getStorage().getImagePath()))
                .andDo(
                        restDocs.document(
                                requestHeaders(
                                        headerWithName(AUTHORIZATION_HEADER).optional().description("JWT Access Token")
                                ),
                                responseFields(
                                        fieldWithPath("recruitment.no").type(JsonFieldType.NUMBER).description("봉사 모집글 고유키 PK"),
                                        fieldWithPath("recruitment.volunteeringCategory").type(JsonFieldType.STRING).description("Code VolunteeringCategory 참고바람"),
                                        fieldWithPath("recruitment.organizationName").type(JsonFieldType.STRING).description("기관 이름"),
                                        fieldWithPath("recruitment.isIssued").type(JsonFieldType.BOOLEAN).description("봉사 시간 인증 가능 여부"),
                                        fieldWithPath("recruitment.volunteeringType").type(JsonFieldType.STRING).description("Code VolunteerType 참고바람."),
                                        fieldWithPath("recruitment.volunteerNum").type(JsonFieldType.NUMBER).description("봉사 모집 인원"),
                                        fieldWithPath("recruitment.volunteerType").type(JsonFieldType.STRING).description("Code VolunteerType 참고바람."),
                                        fieldWithPath("recruitment.startDay").type(JsonFieldType.STRING).attributes(getDateFormat()).description("봉사 모집글 고유키 PK"),
                                        fieldWithPath("recruitment.endDay").type(JsonFieldType.STRING).attributes(getDateFormat()).description("봉사 모집글 고유키 PK"),
                                        fieldWithPath("recruitment.startTime").type(JsonFieldType.STRING).attributes(getTimeFormat()).description("봉사 모집글 고유키 PK"),
                                        fieldWithPath("recruitment.hourFormat").type(JsonFieldType.STRING).description("Code HourFormat 참고바람."),
                                        fieldWithPath("recruitment.progressTime").type(JsonFieldType.NUMBER).description("정기 봉사 일정 진행 시간"),
                                        fieldWithPath("recruitment.title").type(JsonFieldType.STRING).description("봉사 모집글 제목"),
                                        fieldWithPath("recruitment.content").type(JsonFieldType.STRING).description("봉사 모집글 본문"),

                                        fieldWithPath("recruitment.address.sido").type(JsonFieldType.STRING).description("시/구 코드"),
                                        fieldWithPath("recruitment.address.sigungu").type(JsonFieldType.STRING).description("시/군/구 코드"),
                                        fieldWithPath("recruitment.address.details").type(JsonFieldType.STRING).description("상세주소"),
                                        fieldWithPath("recruitment.address.latitude").type(JsonFieldType.NUMBER).description("위도"),
                                        fieldWithPath("recruitment.address.longitude").type(JsonFieldType.NUMBER).description("경도"),

                                        fieldWithPath("approvalVolunteer[].userNo").type(JsonFieldType.NUMBER).description("봉사 참가 승인된 참가자(유저) 고유키 PK"),
                                        fieldWithPath("approvalVolunteer[].nickName").type(JsonFieldType.STRING).description("봉사 참가 승인된 참가자 닉네임"),
                                        fieldWithPath("approvalVolunteer[].imageUrl").type(JsonFieldType.STRING).description("봉사 참가 승인된 참가자 이미지 URL"),

                                        fieldWithPath("requiredVolunteer[].userNo").type(JsonFieldType.NUMBER).description("봉사 참가 미승인된 참가자(유저) 고유키 PK"),
                                        fieldWithPath("requiredVolunteer[].nickName").type(JsonFieldType.STRING).description("봉사 참가 미승인된 참가자 닉네임"),
                                        fieldWithPath("requiredVolunteer[].imageUrl").type(JsonFieldType.STRING).description("봉사 참가 미승인된 참가자 이미지 URL"),

                                        fieldWithPath("recruitment.author.nickName").type(JsonFieldType.STRING).description("봉사 모집글 작성자 닉네임"),
                                        fieldWithPath("recruitment.author.imageUrl").type(JsonFieldType.STRING).description("봉사 모집글 이미지 URL"),

                                        fieldWithPath("recruitment.repeatPeriod.period").type(JsonFieldType.STRING).optional().description("Code Period 참고바람. 비정기일 경우 NULL"),
                                        fieldWithPath("recruitment.repeatPeriod.week").type(JsonFieldType.STRING).optional().description("Code Week 참고바람. 비정기 혹은 Period가 매주일 경우 NULL"),
                                        fieldWithPath("recruitment.repeatPeriod.days").type(JsonFieldType.ARRAY).optional().description("Code Day 참고바람. 비정기일 경우 NULL"),

                                        fieldWithPath("recruitment.picture.isStaticImage").type(JsonFieldType.BOOLEAN).description("정적/동적 이미지 구분"),
                                        fieldWithPath("recruitment.picture.uploadImage").type(JsonFieldType.STRING).optional().description("업로드 이미지 URL, isStaticImage True 일 경우 NULL")
                                )
                        )
                );
    }

    @Test
    @WithUserDetails(value = "rctfqt", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void findParticipantState() throws Exception {
        //given & when
        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.get("/recruitment/{no}/status",saveRecruitmentList.get(0).getRecruitmentNo())
                .header(AUTHORIZATION_HEADER, "access Token")
        );

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("status").value(StateResponse.APPROVED.getId()))
                .andDo(
                        restDocs.document(
                                requestHeaders(
                                        headerWithName(AUTHORIZATION_HEADER).description("JWT Access Token")
                                ),
                                pathParameters(
                                        parameterWithName("no").description("봉사 모집글 고유키 PK")
                                ),
                                responseFields(
                                        fieldWithPath("status").type(JsonFieldType.STRING).description("Code ClientState 참고바람.")
                                )
                        )
                );
    }

    private MockMultipartFile getMockMultipartFile() throws IOException {
        return new MockMultipartFile(
                "file", "file.PNG", "image/jpg", new FileInputStream("src/main/resources/static/test/file.PNG"));
    }
}