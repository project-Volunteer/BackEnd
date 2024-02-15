package project.volunteer.domain.scheduleParticipation.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import project.volunteer.domain.recruitmentParticipation.repository.ParticipantRepository;
import project.volunteer.domain.recruitmentParticipation.domain.Participant;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.repository.RecruitmentRepository;
import project.volunteer.domain.scheduleParticipation.repository.ScheduleParticipationRepository;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;
import project.volunteer.domain.sehedule.repository.ScheduleRepository;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.*;
import project.volunteer.global.error.exception.BusinessException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Slf4j
public class ConcurrentTest {
    @Autowired UserRepository userRepository;
    @Autowired RecruitmentRepository recruitmentRepository;
    @Autowired ScheduleRepository scheduleRepository;
    @Autowired ParticipantRepository participantRepository;
    @Autowired
    ParticipationServiceConcurrent participateService;
    @Autowired ScheduleParticipationRepository scheduleParticipationRepository;

    User writer;
    Recruitment saveRecruitment;
    Schedule saveSchedule;
    @BeforeEach
    void init(){
        //작성자 저장
        User writerUser = User.createUser("1234", "1234", "1234", "1234", Gender.M, LocalDate.now(), "1234",
                true, true, true, Role.USER, "kakao", "1234", null);
        writer = userRepository.save(writerUser);

        //모집글 저장
        Recruitment createRecruitment = new Recruitment( "title", "content", VolunteeringCategory.EDUCATION, VolunteeringType.REG,
                VolunteerType.ADULT, 9999,0,true, "unicef",
                new Address("111", "11", "test", "test"),
                new Coordinate(1.2F, 2.2F),
                new Timetable(LocalDate.of(2024, 1, 10), LocalDate.of(2024, 3, 3), HourFormat.AM,
                        LocalTime.now(), 10),
                0, 0, true, IsDeleted.N, writerUser);
        saveRecruitment = recruitmentRepository.save(createRecruitment);

        //일정 저장
        Schedule createSchedule = Schedule.create(
                saveRecruitment,
                new Timetable(
                        LocalDate.now().plusMonths(1), LocalDate.now().plusMonths(1),
                        HourFormat.AM, LocalTime.now(), 3),
                "content", "organization",
                Address.createAddress("11", "1111", "details", "fullName"), 1);
        saveSchedule = scheduleRepository.save(createSchedule);
    }

    @Test
    @DisplayName("일정 참여 모집 인원은 1명인데, 동시성 이슈로 인해 3명의 사용자 모두 참여가 된다.")
    @Disabled
    public void concurrentParticipationWithoutLock() throws InterruptedException {
        //given
        final int numberOfThreads = 3;

        CountDownLatch countDownLatch = new CountDownLatch(numberOfThreads);
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        List<Participant> participants = addTeamMember(numberOfThreads);

        //when
        for(int i=0;i<numberOfThreads;i++){
            Participant participant = participants.get(i);

            executorService.execute(() -> {
                try{
                    participateService.participateWithoutLock(saveRecruitment.getRecruitmentNo(), saveSchedule.getScheduleNo(), participant.getParticipant().getUserNo());
                }finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();

        //then
        List<ScheduleParticipation> findSP = scheduleParticipationRepository.findBySchedule_ScheduleNo(saveSchedule.getScheduleNo());
        assertThat(findSP.size()).isEqualTo(3);
    }

    @Test
    @DisplayName("낙관적 락을 통해 동시성 문제를 해결해보자.")
    @Disabled
    public void concurrentParticipationWithOPTIMSTICLock() throws InterruptedException {
        //given
        final int numberOfThreads = 3;

        CountDownLatch countDownLatch = new CountDownLatch(numberOfThreads);
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        List<Participant> participants = addTeamMember(numberOfThreads);

        //when
        for(int i=0;i<numberOfThreads;i++){
            Participant participant = participants.get(i);

            executorService.execute(() -> {
                try{
                    participateService.participateWithOPTIMSTICLock(saveRecruitment.getRecruitmentNo(), saveSchedule.getScheduleNo(), participant.getParticipant().getUserNo());
                }catch (ObjectOptimisticLockingFailureException e){
                    log.info("동시성 문제 발견");
                    /**
                     * 예외가 밸생해야됨.
                     * 예외 발생 시 "인원 마감" message 를 사용자에게 보여준다. -> 동시성 해결됬나?!
                     * "데드락" 발생(s-lock, x-lock)!!!(외래키가 있는 테이블에서는 낙관적 락으로 동시성을 해결 불가능!!)
                     * 만약 데드락이 발생하지 않더라도,여유자리가 많은 남은 상황에서 동시에 신청할 때는 예외가 발생하면 안됨.
                     * 또한 데드락이 발생하지 않고 마지막 인원의 신청자가 update 해주기 위해서 상태 Enum을 Schedule에 추가해 업데이트해주더라도, 남은 자리가 2자리고 동시 신청자가 3명일 경우 여전히 동시성 문제 해결 안됨.
                     * => 즉 동시성 이슈를 해결할 수 없음!!
                     */
                }
                finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();

        //then
        Schedule findSchedule = scheduleRepository.findById(saveSchedule.getScheduleNo()).get();
        List<ScheduleParticipation> findSP = scheduleParticipationRepository.findBySchedule_ScheduleNo(saveSchedule.getScheduleNo());
        assertThat(findSP.size()).isEqualTo(1);
        assertThat(findSchedule.getCurrentVolunteerNum()).isEqualTo(1);
    }

    @Test
    @DisplayName("비관적 락을 통해 동시성 문제를 해결해보자.")
    @Disabled
    public void concurrentParticipationWithPERSSIMITIC_WRITE_Lock() throws InterruptedException {
        //given
        final int numberOfThreads = 3;

        CountDownLatch countDownLatch = new CountDownLatch(numberOfThreads);
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        List<Participant> participants = addTeamMember(numberOfThreads);

        //when
        for(int i=0;i<numberOfThreads;i++){
            Participant participant = participants.get(i);

            executorService.execute(() -> {
                try{
                    participateService.participateWithPERSSIMITIC_WRITE_Lock(saveRecruitment.getRecruitmentNo(), saveSchedule.getScheduleNo(), participant.getParticipant().getUserNo());
                }catch (BusinessException e){
                    log.info("인원 마감 : {}", e.getMessage());
                    //인원 마감 "INSUFFICIENT_CAPACITY" 예외가 발생해야 함.
                }
                finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();

        //then
        Schedule findSchedule = scheduleRepository.findById(saveSchedule.getScheduleNo()).get();
        List<ScheduleParticipation> findSP = scheduleParticipationRepository.findBySchedule_ScheduleNo(saveSchedule.getScheduleNo());
        assertThat(findSP.size()).isEqualTo(1);
        assertThat(findSchedule.getCurrentVolunteerNum()).isEqualTo(1);
    }


    private List<Participant> addTeamMember(int num){
        List<Participant> participants = new ArrayList<>();
        for(int i=0;i<num;i++){
            User createUser = User.createUser("test" + i, "test" + i, "test" + i, "test" + i, Gender.M, LocalDate.now(), "picture",
                    true, true, true, Role.USER, "kakao", "test" + i, null);
            User saveUser = userRepository.save(createUser);

            Participant createParticipant = Participant.createParticipant(saveRecruitment, saveUser, ParticipantState.JOIN_APPROVAL);
            Participant saveParticipant = participantRepository.save(createParticipant);

            participants.add(saveParticipant);
        }
        return participants;
    }
}
