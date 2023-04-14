package project.volunteer.domain.recruitment.api;

import org.junit.jupiter.api.AfterEach;
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
import project.volunteer.domain.image.application.ImageService;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.image.domain.ImageType;
import project.volunteer.domain.image.domain.RealWorkCode;
import project.volunteer.domain.image.application.dto.ImageParam;
import project.volunteer.domain.participation.dao.ParticipantRepository;
import project.volunteer.domain.participation.domain.Participant;
import project.volunteer.domain.recruitment.application.RecruitmentService;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;

import project.volunteer.domain.recruitment.application.dto.RecruitmentParam;
import project.volunteer.domain.repeatPeriod.application.RepeatPeriodService;
import project.volunteer.domain.repeatPeriod.application.dto.RepeatPeriodParam;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RecruitmentControllerTestForFindAll {

    @Autowired UserRepository userRepository;
    @Autowired ParticipantRepository participantRepository;
    @Autowired RecruitmentRepository recruitmentRepository;
    @Autowired ImageRepository imageRepository;
    @Autowired RecruitmentService recruitmentService;
    @Autowired RepeatPeriodService repeatPeriodService;
    @Autowired ImageService imageService;
    @Autowired FileService fileService;
    @PersistenceContext EntityManager em;
    @Autowired MockMvc mockMvc;

    private static final String FINDALL_URL = "/recruitment";
    private List<Long> deletePlanS3ImageNo = new ArrayList<>();
    private void clear() {
        em.flush();
        em.clear();
    }
    private MockMultipartFile getMockMultipartFile() throws IOException {
        return new MockMultipartFile(
                "file", "file.PNG", "image/jpg", new FileInputStream("src/main/resources/static/test/file.PNG"));
    }
    @BeforeEach
    public void init() throws IOException {
        //유저 임시 로그인
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

        //모집글 데이터
        String category1 = "001";
        String category2 = "002";
        String volunteeringType1 = "short";
        String volunteeringType2 = "long";
        String volunteerType1 = "1"; //all
        String volunteerType2 = "3"; //teenager
        Boolean isIssued1 = true;
        Boolean isIssued2 = false;
        String sido1 = "11";
        String sido2 = "22";
        String sigungu1 = "1111";
        String sigungu2 = "2222";
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

        //반복 주기 데이터
        String period = "week";
        String week = "";
        List<String> days = List.of("mon","tues");

        for(int i=0;i<5;i++){

            //모집글 저장
            //no1: 단기 + static 이미지 + 참여자 1명(승인)   -> 총 5개 저장
            //no2: 장기 + upload 이미지 + 참여자 1명(미승인)  -> 총 5개 저장
            RecruitmentParam saveRecruitDto1 = new RecruitmentParam(category1, organizationName, sido1, sigungu1, details, latitude, longitude,
                    isIssued1, volunteerType1, volunteerNum, volunteeringType1, startDay, endDay, startTime, progressTime, title, content, isPublished);
            RecruitmentParam saveRecruitDto2 = new RecruitmentParam(category2, organizationName, sido2, sigungu2, details, latitude, longitude,
                    isIssued2, volunteerType2, volunteerNum, volunteeringType2, startDay, endDay, startTime, progressTime, title, content, isPublished);
            Long no1 = recruitmentService.addRecruitment(saveRecruitDto1);
            Long no2 = recruitmentService.addRecruitment(saveRecruitDto2);

            //반복 주기 저장
            RepeatPeriodParam savePeriodDto = new RepeatPeriodParam(period, week, days);
            repeatPeriodService.addRepeatPeriod(no2, savePeriodDto);

            //이미지 저장
            ImageParam staticImageDto = ImageParam.builder()
                    .code(RealWorkCode.RECRUITMENT)
                    .imageType(ImageType.STATIC)
                    .no(no1)
                    .staticImageCode(String.valueOf(i))
                    .uploadImage(null)
                    .build();
            Long saveId1 = imageService.addImage(staticImageDto);

            ImageParam uploadImageDto = ImageParam.builder()
                    .code(RealWorkCode.RECRUITMENT)
                    .imageType(ImageType.UPLOAD)
                    .no(no2)
                    .staticImageCode(null)
                    .uploadImage(getMockMultipartFile())
                    .build();
            Long saveId2 = imageService.addImage(uploadImageDto);
            deletePlanS3ImageNo.add(saveId2); //S3에 저장된 이미지 추후 삭제 예정

            //참여자 저장
            Recruitment recruitment1 = recruitmentRepository.findById(no1).get();
            Participant participant1 = Participant.builder()
                    .participant(saveUser)
                    .recruitment(recruitment1)
                    .build();
            participant1.approve(); //참여 승인
            participantRepository.save(participant1);

            Recruitment recruitment2 = recruitmentRepository.findById(no2).get();
            Participant participant2 = Participant.builder()
                    .participant(saveUser)
                    .recruitment(recruitment2)
                    .build();
            participantRepository.save(participant2); //참여 미승인
        }
        clear();
    }
    @AfterEach
    public void deleteS3Image() { //S3에 테스트를 위해 저장한 이미지 삭제
        for(Long id : deletePlanS3ImageNo){
            Image image = imageRepository.findById(id).get();
            Storage storage = image.getStorage();
            fileService.deleteFile(storage.getFakeImageName());
        }
    }

    @Test
    public void 모집글_전체조회_모든필터링_성공() throws Exception {

        //given: 페이지 & 필터링 조건
        MultiValueMap<String,String> info = new LinkedMultiValueMap();
        info.add("page", "0");
        info.add("volunteering_category", "001");
        info.add("sido", "11");
        info.add("sigungu","1111");
        info.add("volunteering_type", "short");
        info.add("volunteer_type", "1"); //all
        info.add("is_issued", "true");

        //when & then
        mockMvc.perform(
                get(FINDALL_URL).params(info))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void 모집글_전체조회_다중카테고리필터링_성공() throws Exception {

        //given: 페이지 & 필터링 조건
        MultiValueMap<String,String> info = new LinkedMultiValueMap();
        info.add("page", "0");
        info.add("volunteering_category", "001,002");

        //when & then
        mockMvc.perform(
                        get(FINDALL_URL).params(info))
                .andExpect(status().isOk())
                .andDo(print());
    }

}