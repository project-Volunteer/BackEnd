package project.volunteer.domain.recruitment.application;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import project.volunteer.domain.recruitment.application.dto.ParticipantDetails;
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
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.infra.s3.FileService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
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
    private Long userNo;
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
        addImage(RealWorkCode.USER, userNo);

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
    @BeforeEach
    private void initUser() {
        User save = userRepository.save(User.builder()
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
        userNo = save.getUserNo();
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
        RecruitmentDetails recruitment = recruitmentDtoService.findRecruitment(saveRecruitment.getRecruitmentNo());

        //then
        Assertions.assertThat(recruitment.getCurrentVolunteer().size()).isEqualTo(5); //참여자 5명
        Assertions.assertThat(recruitment.getPicture().getType()).isEqualTo(ImageType.UPLOAD.getValue()); //모집글 이미지=업로드
        for(ParticipantDetails dto : recruitment.getCurrentVolunteer()){
            Assertions.assertThat(dto.getIsApproved()).isTrue();
        }
        System.out.println(recruitment);
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