package project.volunteer.domain.scheduleParticipation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.repository.RecruitmentRepository;
import project.volunteer.domain.recruitmentParticipation.domain.RecruitmentParticipation;
import project.volunteer.domain.recruitmentParticipation.repository.RecruitmentParticipationRepository;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;
import project.volunteer.domain.scheduleParticipation.repository.ScheduleParticipationRepository;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.domain.sehedule.repository.ScheduleRepository;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.Coordinate;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.common.component.Timetable;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;
import project.volunteer.support.DatabaseCleaner;

@SpringBootTest
@ActiveProfiles("test")
public class ConcurrentScheduleParticipationTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RecruitmentRepository recruitmentRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private RecruitmentParticipationRepository recruitmentParticipationRepository;
    @Autowired
    private ScheduleParticipationRepository scheduleParticipationRepository;
    @Autowired
    private ScheduleParticipationCommandFacade scheduleParticipationCommandFacade;
    @SpyBean
    private Clock clock;
    @Autowired
    private DatabaseCleaner databaseCleaner;

    @AfterEach
    void tearDown() {
        databaseCleaner.execute();
    }

    @DisplayName("여러 회원이 동시에 일정 참여를 시도해도 참여 가능 인원 임계값을 넘지 못한다.")
    @Test
    void participateWithConcurrent() throws InterruptedException {
        // given
        int numberOfThreads = 5;
        CountDownLatch countDownLatch = new CountDownLatch(numberOfThreads);
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        given(clock.instant()).willReturn(Instant.parse("2024-01-29T10:00:00Z"));

        User writer = userRepository.save(new User("test", "test", "test", "test@email.com", Gender.M, LocalDate.now(),
                "http://...", true, true, true, Role.USER, "kakao", "1234", null));

        Recruitment recruitment = recruitmentRepository.save(
                new Recruitment("title", "content", VolunteeringCategory.EDUCATION, VolunteeringType.IRREG,
                        VolunteerType.ADULT, 999, 0, true, "organization",
                        Address.builder().sido("1111").sigungu("11").details("삼성 아파트").fullName("대구광역시 북구 삼성 아파트").build(),
                        Coordinate.builder().longitude(3.2F).latitude(3.2F).build(),
                        Timetable.builder().startDay(LocalDate.of(2024, 1, 10)).endDay(LocalDate.of(2024, 2, 10))
                                .hourFormat(HourFormat.AM).startTime(LocalTime.now()).progressTime(10).build(),
                        0, 0, true, IsDeleted.N, writer)
        );

        Schedule schedule = scheduleRepository.save(
                new Schedule(
                        Timetable.builder().startDay(LocalDate.of(2024, 2, 1)).endDay(LocalDate.of(2024, 2, 10))
                                .hourFormat(HourFormat.AM).startTime(LocalTime.now()).progressTime(10).build(),
                        "test", "unicef",
                        Address.builder().sido("1111").sigungu("11").details("삼성 아파트").fullName("대구광역시 북구 삼성 아파트").build(),
                        2, IsDeleted.N, 0, recruitment)
        );

        List<User> users = createAndSaveUser(numberOfThreads);
        users.forEach(user -> recruitmentParticipationRepository.save(
                new RecruitmentParticipation(recruitment, user, ParticipantState.JOIN_APPROVAL)));

        // when
        for (int i = 0; i < numberOfThreads; i++) {
            User user = users.get(i);

            executorService.execute(() -> {
                try {
                    scheduleParticipationCommandFacade.participateSchedule(user.getUserNo(),
                            recruitment.getRecruitmentNo(), schedule.getScheduleNo());
                } catch (BusinessException e) {
                    exceptions.add(e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();

        //then
        List<ScheduleParticipation> findScheduleParticipation = scheduleParticipationRepository.findAll();
        Schedule findSchedule = scheduleRepository.findById(schedule.getScheduleNo())
                .orElseThrow(() -> new IllegalArgumentException("일정 정보가 존재하지 않습니다."));
        assertAll(
                () -> assertThat(findScheduleParticipation).hasSize(2),
                () -> assertThat(exceptions).hasSize(3)
                        .extracting("errorCode")
                        .containsExactlyInAnyOrder(ErrorCode.INSUFFICIENT_CAPACITY, ErrorCode.INSUFFICIENT_CAPACITY,
                                ErrorCode.INSUFFICIENT_CAPACITY),
                () -> assertThat(findSchedule.getCurrentVolunteerNum()).isEqualTo(2)
        );
    }

    private List<User> createAndSaveUser(int size) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            User user = userRepository.save(
                    new User("test" + i, "test" + i, "test", "test@email.com", Gender.M, LocalDate.now(),
                            "http://...", true, true, true, Role.USER, "kakao", "1234", null)
            );
            users.add(user);
        }
        return users;
    }

}
