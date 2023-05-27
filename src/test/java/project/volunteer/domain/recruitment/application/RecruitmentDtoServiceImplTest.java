package project.volunteer.domain.recruitment.application;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.image.application.ImageService;
import project.volunteer.domain.image.application.dto.ImageParam;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.image.domain.ImageType;
import project.volunteer.domain.image.domain.RealWorkCode;
import project.volunteer.domain.participation.dao.ParticipantRepository;
import project.volunteer.domain.participation.domain.Participant;
import project.volunteer.global.common.response.ParticipantState;
import project.volunteer.domain.recruitment.application.dto.RecruitmentDetails;
import project.volunteer.domain.recruitment.application.dto.RecruitmentParam;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
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
import project.volunteer.global.common.component.State;
import project.volunteer.global.common.component.Timetable;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.infra.s3.FileService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Transactional
class RecruitmentDtoServiceImplTest {

    @PersistenceContext EntityManager em;
    @Autowired UserRepository userRepository;
    @Autowired RecruitmentDtoService recruitmentDtoService;
    @Autowired RecruitmentService recruitmentService;
    @Autowired RepeatPeriodService repeatPeriodService;
    @Autowired ImageService imageService;
    @Autowired FileService fileService;
    @Autowired ImageRepository imageRepository;
    @Autowired ParticipantRepository participantRepository;
    @Autowired RecruitmentRepository recruitmentRepository;

    private Recruitment saveRecruitment;
    private User loginUser;
    private List<Long> deleteImageNo = new ArrayList<>();
    private void clear() {
        em.flush();
        em.clear();
    }
    private void setMockUpData() throws IOException {
        //모집글 저장 및 반복 주기 저장
        addRecruitment();

        //모집글 업로드 이미지 저장
        addImage(RealWorkCode.RECRUITMENT, saveRecruitment.getRecruitmentNo());

        //작성자 업로드 이미지 저장
        addImage(RealWorkCode.USER, loginUser.getUserNo());

        //유저 임시 회원가입, 이미지 업로드, 참여자 등록
        initParticipant();
    }
    private void addRecruitment() {
        //모집글 저장
        String category = "001";
        String volunteeringType = VolunteeringType.REG.name();
        String volunteerType = "1"; //all
        Boolean isIssued = true;
        String sido = "11";
        String sigungu = "1111";
        String organizationName = "name";
        String details = "details";
        Float latitude = 3.2F, longitude = 3.2F;
        Integer volunteerNum = 7;
        String startDay = LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        String endDay = LocalDate.now().plusMonths(3).format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        String hourFormat = HourFormat.AM.name();
        String startTime = "01:01";
        Integer progressTime = 3;
        String title = "title", content = "content";
        Boolean isPublished = true;
        RecruitmentParam saveRecruitDto = new RecruitmentParam(category, organizationName, sido, sigungu, details, latitude, longitude,
                isIssued, volunteerType, volunteerNum, volunteeringType, startDay, endDay, hourFormat, startTime, progressTime, title, content, isPublished);
        Long no = recruitmentService.addRecruitment(saveRecruitDto);

        //모집글 반복주기 저장(장기-매달)
        String period = "month";
        int week = Week.FIRST.getValue();
        List<Integer> days = List.of(Day.MON.getValue(), Day.TUES.getValue());
        RepeatPeriodParam savePeriodDto = new RepeatPeriodParam(period, week, days);
        repeatPeriodService.addRepeatPeriod(no, savePeriodDto);

        saveRecruitment = recruitmentRepository.findById(no).get();
    }
    private void addImage(RealWorkCode realWorkCode, Long no) throws IOException {
        ImageParam staticImageDto = ImageParam.builder()
                .code(realWorkCode)
                .imageType(ImageType.UPLOAD)
                .no(no)
                .staticImageCode(null)
                .uploadImage(getMockMultipartFile())
                .build();
        Long imageNo = imageService.addImage(staticImageDto);
        deleteImageNo.add(imageNo);
    }
    private MockMultipartFile getMockMultipartFile() throws IOException {
        return new MockMultipartFile(
                "file", "file.PNG", "image/jpg", new FileInputStream("src/main/resources/static/test/file.PNG"));
    }
    private void initParticipant() throws IOException {
        for (int i=0;i<5;i++){
            //임시 사용자 회원가입
            String rand = "test"+i;
            User saveUser = userRepository.save(User.builder()
                    .id(rand)
                    .password(rand)
                    .nickName(rand)
                    .email("email@naver.com")
                    .gender(Gender.M)
                    .birthDay(LocalDate.now())
                    .picture("picture")
                    .joinAlarmYn(true).beforeAlarmYn(true).noticeAlarmYn(true)
                    .role(Role.USER)
                    .provider("kakao").providerId("1234")
                    .build());

            //임시 사용자 이미지 업로드
            if(i%2==0)
                addImage(RealWorkCode.USER, saveUser.getUserNo());

            //참여자로 등록
            Participant participant = Participant.builder()
                    .participant(saveUser)
                    .recruitment(saveRecruitment)
                    .state(State.JOIN_APPROVAL)
                    .build();
            participantRepository.save(participant);
        }
    }
    private List<Long> addParticipant(int count, State state, Long recruitmentNo){
        List<Long> participantNoList = new ArrayList<>();

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

            participantRepository.save(Participant.builder()
                    .participant(joinUser)
                    .recruitment(recruitmentRepository.findById(recruitmentNo).get())
                    .state(state)
                    .build());

            participantNoList.add(joinUser.getUserNo());
        }
        return participantNoList;
    }

    @BeforeEach
    private void initUser() {
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
        clear();
    }
    @AfterEach
    public void deleteS3Image() { //S3에 테스트를 위해 저장한 이미지 삭제
        for(Long id : deleteImageNo){
            Image image = imageRepository.findById(id).get();
            Storage storage = image.getStorage();
            fileService.deleteFile(storage.getFakeImageName());
        }
    }
    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 모집글_상세조회_성공() throws IOException {
        //init
        setMockUpData();

        //given & when
        RecruitmentDetails recruitmentDto = recruitmentDtoService.findRecruitment(saveRecruitment.getRecruitmentNo());

        //then
        Assertions.assertThat(recruitmentDto.getApprovalVolunteer().size()).isEqualTo(5); //승인 참여자 5명
        Assertions.assertThat(recruitmentDto.getRequiredVolunteer().size()).isEqualTo(0);
        Assertions.assertThat(recruitmentDto.getPicture().getType()).isEqualTo(ImageType.UPLOAD.getValue()); //모집글 이미지=업로드
    }

    @DisplayName("모집글 참가인원 상태 테스트")
    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void searchParticipantState() throws IOException {
        //given
        setMockUpData(); //기본 approval 사용자 5명, 모집글 최대 참가인원 7명
        addParticipant(5, State.JOIN_REQUEST, saveRecruitment.getRecruitmentNo()); //팀 신청 5명
        clear();

        //when
        RecruitmentDetails details = recruitmentDtoService.findRecruitment(saveRecruitment.getRecruitmentNo());

        //then
        Assertions.assertThat(details.getApprovalVolunteer().size()).isEqualTo(5);
        Assertions.assertThat(details.getRequiredVolunteer().size()).isEqualTo(5);
    }

    @DisplayName("모집글 첫 참가 신청으로 로그인 사용자 상태가 신청 가능 상태가 된다.")
    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void loginUserAvailableStateByFirst() throws IOException {
        //given
        setMockUpData(); //기본 approval 사용자 5명, 모집글 최대 참가인원 7명
        clear();

        //when
        RecruitmentDetails details = recruitmentDtoService.findRecruitment(saveRecruitment.getRecruitmentNo());

        //then
        Assertions.assertThat(details.getStatus()).isEqualTo(ParticipantState.AVAILABLE.name());

    }

    @DisplayName("모집글 팀 탈퇴로 인해 로그인 사용자 상태가 신청 가능 상태가 된다.")
    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void loginUserAvailableStateByQuit() throws IOException {
        //given
        setMockUpData(); //기본 approval 사용자 5명, 모집글 최대 참가인원 7명
        participantRepository.save(
                Participant.builder()
                        .recruitment(saveRecruitment)
                        .participant(loginUser)
                        .state(State.QUIT)
                        .build()
        );
        clear();

        //when
        RecruitmentDetails details = recruitmentDtoService.findRecruitment(saveRecruitment.getRecruitmentNo());

        //then
        Assertions.assertThat(details.getStatus()).isEqualTo(ParticipantState.AVAILABLE.name());
    }

    @DisplayName("모집글 팀 신청으로 인해 로그인 사용자 상태가 승인 대기 상태가 된다.")
    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void loginUserPendingState() throws IOException {
        //given
        setMockUpData(); //기본 approval 사용자 5명, 모집글 최대 참가인원 7명
        participantRepository.save(
                Participant.builder()
                        .recruitment(saveRecruitment)
                        .participant(loginUser)
                        .state(State.JOIN_REQUEST)
                        .build()
        );
        clear();

        //when
        RecruitmentDetails details = recruitmentDtoService.findRecruitment(saveRecruitment.getRecruitmentNo());

        //then
        Assertions.assertThat(details.getStatus()).isEqualTo(ParticipantState.PENDING.name());
    }

    @DisplayName("모집글 팀 승인으로 인해 로그인 사용자 상태가 승인 완료 상태가 된다.")
    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void loginUserApprovedState() throws IOException {
        //given
        setMockUpData(); //기본 approval 사용자 5명, 모집글 최대 참가인원 7명
        participantRepository.save(
                Participant.builder()
                        .recruitment(saveRecruitment)
                        .participant(loginUser)
                        .state(State.JOIN_APPROVAL)
                        .build()
        );
        clear();

        //when
        RecruitmentDetails details = recruitmentDtoService.findRecruitment(saveRecruitment.getRecruitmentNo());

        //then
        Assertions.assertThat(details.getStatus()).isEqualTo(ParticipantState.APPROVED.name());
    }

    @DisplayName("모집 기간 만료로 인해 로그인 사용자 상태가 모집 마감 상태가 된다.")
    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void loginUserDoneStateByFinishEndDay() throws IOException {
        //given
        setMockUpData(); //기본 approval 사용자 5명, 모집글 최대 참가인원 7명
        saveRecruitment.setVolunteeringTimeTable(
                Timetable.builder()
                        .progressTime(3)
                        .startDay(LocalDate.now().minusMonths(3))
                        .endDay(LocalDate.now().minusMonths(2))
                        .startTime(LocalTime.now())
                        .hourFormat(HourFormat.AM)
                        .build()
        );
        clear();

        //when
        RecruitmentDetails details = recruitmentDtoService.findRecruitment(saveRecruitment.getRecruitmentNo());

        //then
        Assertions.assertThat(details.getStatus()).isEqualTo(ParticipantState.DONE.name());
    }

    @DisplayName("팀원 모집인원 초과로 인해 로그인 사용자 상태가 모집 마감 상태가 된다.")
    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void loginUserDoneStateByVolunteerNum() throws IOException {
        //given
        setMockUpData(); //기본 approval 사용자 5명, 모집글 최대 참가인원 7명
        addParticipant(2, State.JOIN_APPROVAL, saveRecruitment.getRecruitmentNo()); //모집인원 마감
        clear();

        //when
        RecruitmentDetails details = recruitmentDtoService.findRecruitment(saveRecruitment.getRecruitmentNo());

        //then
        Assertions.assertThat(details.getStatus()).isEqualTo(ParticipantState.DONE.name());
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 모집글_상세조회_실패_삭제된게시물() throws IOException {
        //given
        setMockUpData();
        saveRecruitment.setDeleted(); //삭제 게시물로 만들기

        //when & then
        Assertions.assertThatThrownBy(() -> recruitmentDtoService.findRecruitment(saveRecruitment.getRecruitmentNo()))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 모집글_상세조회_실패_임시게시물() throws IOException {
        //given
        setMockUpData();
        saveRecruitment.setIsPublished(false); //임시 게시물로 만들기

        //when & then
        Assertions.assertThatThrownBy(() -> recruitmentDtoService.findRecruitment(saveRecruitment.getRecruitmentNo()))
                .isInstanceOf(BusinessException.class);
    }

}