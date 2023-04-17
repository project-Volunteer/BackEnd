package project.volunteer.domain.recruitment.application;

import org.assertj.core.api.Assertions;
import org.hibernate.validator.constraints.Range;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.image.application.ImageService;
import project.volunteer.domain.image.application.dto.SaveImageDto;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.image.domain.ImageType;
import project.volunteer.domain.image.domain.RealWorkCode;
import project.volunteer.domain.participation.dao.ParticipantRepository;
import project.volunteer.domain.participation.domain.Participant;
import project.volunteer.domain.recruitment.application.dto.ParticipantDto;
import project.volunteer.domain.recruitment.application.dto.RecruitmentDto;
import project.volunteer.domain.recruitment.application.dto.SaveRecruitDto;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.repeatPeriod.application.RepeatPeriodService;
import project.volunteer.domain.repeatPeriod.application.dto.SaveRepeatPeriodDto;
import project.volunteer.domain.storage.domain.Storage;
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
    private List<Long> deleteImageNo = new ArrayList<>();
    private void clear() {
        em.flush();
        em.clear();
    }
    @BeforeEach
    public void init() throws IOException {
        //작성자 임시 로그인
        Long userNo = initLogin();

        //모집글 저장 및 반복 주기 저장
        addRecruitment();

        //모집글 업로드 이미지 저장
        addImage(RealWorkCode.RECRUITMENT, saveRecruitment.getRecruitmentNo());
        //작성자 업로드 이미지 저장
        addImage(RealWorkCode.USER, userNo);

        //유저 임시 회원가입, 이미지 업로드, 참여자 등록
        initParticipant();

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
    private Long initLogin(){
        final String name = "name";
        final String nickname = "nickname";
        final String email = "email@gmail.com";
        final Gender gender = Gender.M;
        final LocalDate birth = LocalDate.now();
        final String picture = "picture";
        final Boolean alarm = true;
        User saveUser = userRepository.save(User.builder().nickName(nickname)
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
        return saveUser.getUserNo();
    }
    private void addRecruitment(){
        //모집글 저장
        String category = "001";
        String volunteeringType = "long";
        String volunteerType = "1"; //all
        Boolean isIssued = true;
        String sido = "11";
        String sigungu = "1111";
        String organizationName ="name";
        String details = "details";
        Float latitude = 3.2F , longitude = 3.2F;
        Integer volunteerNum = 5;
        String startDay = "01-01-2000";
        String endDay = "01-01-2000";
        String startTime = "01:01:00";
        Integer progressTime = 3;
        String title = "title", content = "content";
        Boolean isPublished = true;
        SaveRecruitDto saveRecruitDto = new SaveRecruitDto(category, organizationName, sido, sigungu, details, latitude, longitude,
                isIssued, volunteerType, volunteerNum, volunteeringType, startDay, endDay, startTime, progressTime, title, content, isPublished);
        Long no = recruitmentService.addRecruitment(saveRecruitDto);

        //모집글 반복주기 저장(장기-매달)
        String period = "month";
        String week = "first";
        List<String> days = List.of("mon","tues");
        SaveRepeatPeriodDto savePeriodDto = new SaveRepeatPeriodDto(period, week, days);
        repeatPeriodService.addRepeatPeriod(no, savePeriodDto);

        saveRecruitment = recruitmentRepository.findById(no).get();
}
    private void addImage(RealWorkCode realWorkCode, Long no) throws IOException {
        SaveImageDto staticImageDto = SaveImageDto.builder()
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
        String name = "name";
        String email = "email@naver.com";
        Gender gender = Gender.M;
        LocalDate birth = LocalDate.now();
        String picture = "picture";
        Boolean alarm = true;

        for (int i=0;i<5;i++){
            //임시 사용자 회원가입
            String nickname = "nickname"+i;
            User saveUser = userRepository.save(User.builder().nickName(nickname)
                    .email(email).gender(gender).birthDay(birth).picture(picture)
                    .joinAlarmYn(alarm).beforeAlarmYn(alarm).noticeAlarmYn(alarm).build());

            //임시 사용자 이미지 업로드
            if(i%2==0)
                addImage(RealWorkCode.USER, saveUser.getUserNo());

            //참여자로 등록
            Participant participant = Participant.builder().participant(saveUser).recruitment(saveRecruitment).build();
            participantRepository.save(participant);
        }
    }

    @Test
    public void 모집글_상세조회_성공(){
        //given & when
        RecruitmentDto recruitment = recruitmentDtoService.findRecruitment(saveRecruitment.getRecruitmentNo());

        //then
        Assertions.assertThat(recruitment.getCurrentVolunteer().size()).isEqualTo(5); //참여자 5명
        Assertions.assertThat(recruitment.getPicture().getType()).isEqualTo(ImageType.UPLOAD.getValue()); //모집글 이미지=업로드
        for(ParticipantDto dto : recruitment.getCurrentVolunteer()){
            Assertions.assertThat(dto.getIsApproved()).isFalse();
        }
        System.out.println(recruitment);
    }

}