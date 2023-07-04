package project.volunteer.domain.recruitment.performance;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.domain.participation.dao.ParticipantRepository;
import project.volunteer.domain.participation.domain.Participant;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.repeatPeriod.dao.RepeatPeriodRepository;
import project.volunteer.domain.repeatPeriod.domain.Day;
import project.volunteer.domain.repeatPeriod.domain.Period;
import project.volunteer.domain.repeatPeriod.domain.RepeatPeriod;
import project.volunteer.domain.repeatPeriod.domain.Week;
import project.volunteer.domain.storage.dao.StorageRepository;
import project.volunteer.domain.storage.domain.Storage;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


@Transactional
@SpringBootTest
public class MockData {

    @Autowired RecruitmentRepository recruitmentRepository;
    @Autowired RepeatPeriodRepository repeatPeriodRepository;
    @Autowired ImageRepository imageRepository;
    @Autowired StorageRepository storageRepository;
    @Autowired UserRepository userRepository;
    @Autowired ParticipantRepository participantRepository;
    User ownerUser;
    List<Recruitment> saveRecruitmentNoList = new ArrayList<>();
    @BeforeEach
    public void init() {
        ownerUser = userRepository.save(User.builder()
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

        for (int i = 0; i < 10; i++) {
            Recruitment saveRecruitment = recruitmentRepository.save(
                    Recruitment.builder()
                            .title("1234" + i)
                            .content("1234" + i)
                            .volunteeringCategory(VolunteeringCategory.EDUCATION)
                            .volunteeringType(VolunteeringType.REG)
                            .volunteerType(VolunteerType.ALL)
                            .volunteerNum(9999)
                            .isIssued(true)
                            .organizationName("1234" + i)
                            .address(
                                    Address.builder()
                                            .sido("1" + i)
                                            .sigungu("1234" + i)
                                            .details("1234" + i)
                                            .build()
                            )
                            .coordinate(
                                    Coordinate.builder()
                                            .longitude(3.2F)
                                            .latitude(3.2F)
                                            .build()
                            )
                            .timetable(
                                    Timetable.builder()
                                            .progressTime(3)
                                            .startDay(LocalDate.now())
                                            .endDay(LocalDate.now())
                                            .startTime(LocalTime.now())
                                            .hourFormat(HourFormat.AM)
                                            .build()
                            )
                            .isPublished(true)
                            .build());
            saveRecruitment.setWriter(ownerUser);

            RepeatPeriod savePeriod = repeatPeriodRepository.save(
                    RepeatPeriod.builder()
                            .period(Period.MONTH)
                            .week(Week.FIRST)
                            .day(Day.SUN)
                            .build());
            savePeriod.setRecruitment(saveRecruitment);

            //현재 사용중인 이미지
            Storage saveStorage = storageRepository.save(
                    Storage.builder()
                            .imagePath("1234" + i)
                            .fakeImageName("1234" + i)
                            .realImageName("1234" + i)
                            .extName(".jpg")
                            .build());
            Image saveImage = imageRepository.save(
                    Image.builder()
                            .realWorkCode(RealWorkCode.RECRUITMENT)
                            .no(saveRecruitment.getRecruitmentNo())
                            .build());
            saveImage.setStorage(saveStorage);

            saveRecruitmentNoList.add(saveRecruitment);
        }
    }

//    @Rollback(value = false)
//    @Test
//    public void 성능테스트를위한_목데이터_setup() throws Exception {
//        //만건 insert
//        for(int i=0;i<10;i++){
//            addParticipant(5000, State.JOIN_APPROVAL, saveRecruitmentNoList.get(i).getRecruitmentNo());
//            addParticipant(5000, State.JOIN_REQUEST, saveRecruitmentNoList.get(i).getRecruitmentNo());
//        }
//    }

    private void addParticipant(int count, ParticipantState state, Long recruitmentNo){
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

            Storage storage = storageRepository.save(
                    Storage.builder()
                            .imagePath("1234" + i)
                            .fakeImageName("1234" + i)
                            .realImageName("1234" + i)
                            .extName(".jpg")
                            .build());

            Image save = imageRepository.save(
                    Image.builder()
                            .realWorkCode(RealWorkCode.USER)
                            .no(joinUser.getUserNo())
                            .build());
            save.setStorage(storage);

            //이전에 사용하던 이미지(삭제된 이미지)
            Storage deletedStorage = storageRepository.save(
                    Storage.builder()
                            .imagePath("deleted" + i)
                            .fakeImageName("deleted" + i)
                            .realImageName("deleted" + i)
                            .extName(".jpg")
                            .build());

            deletedStorage.setDeleted();
            Image deletedImage = imageRepository.save(
                    Image.builder()
                            .realWorkCode(RealWorkCode.USER)
                            .no(joinUser.getUserNo())
                            .build());
            deletedImage.setStorage(deletedStorage);
            deletedImage.setDeleted();


            participantRepository.save(Participant.builder()
                    .participant(joinUser)
                    .recruitment(recruitmentRepository.findById(recruitmentNo).get())
                    .state(state)
                    .build());
        }
    }
}
