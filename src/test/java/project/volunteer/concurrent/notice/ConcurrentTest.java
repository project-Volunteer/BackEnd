package project.volunteer.concurrent.notice;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import project.volunteer.domain.confirmation.dao.ConfirmationRepository;
import project.volunteer.domain.confirmation.domain.Confirmation;
import project.volunteer.domain.notice.dao.NoticeRepository;
import project.volunteer.domain.notice.domain.Notice;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.repository.RecruitmentRepository;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.*;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
@SpringBootTest
@Slf4j
public class ConcurrentTest {

    @Autowired EntityManager em;
    @Autowired UserRepository userRepository;
    @Autowired RecruitmentRepository recruitmentRepository;
    @Autowired NoticeRepository noticeRepository;
    @Autowired ConfirmationRepository confirmationRepository;
    @Autowired NoticeConcurrentTestService noticeService;
    User writer;
    Long saveRecruitmentNo;
    Long saveNoticeNo;
    @BeforeEach
    void init(){
        //작성자 저장
        User writerUser = User.createUser("1234", "1234", "1234", "1234", Gender.M, LocalDate.now(), "picture",
                true, true, true, Role.USER, "kakao", "1234", null);
        writer = userRepository.save(writerUser);

        //모집글 저장
        Recruitment createRecruitment = new Recruitment(0L, "title", "content", VolunteeringCategory.EDUCATION, VolunteeringType.REG,
                VolunteerType.ADULT, 9999,0,true, "unicef",
                new Address("111", "11", "test", "test"),
                new Coordinate(1.2F, 2.2F),
                new Timetable(LocalDate.of(2024, 1, 10), LocalDate.of(2024, 3, 3), HourFormat.AM,
                        LocalTime.now(), 10),
                0, 0, true, IsDeleted.N, writer, null);
        saveRecruitmentNo = recruitmentRepository.save(createRecruitment).getRecruitmentNo();

        //공지사항 저장
        Notice createNotice = Notice.createNotice("test");
        createNotice.setRecruitment(createRecruitment);
        saveNoticeNo = noticeRepository.save(createNotice).getNoticeNo();
    }

    @Test
    @DisplayName("같은 사용자가 공지사항 읽음을 광클한다.")
    public void concurrent_read_same_user() throws InterruptedException {
        //given
        final User loginUser = 사용자_추가("test");
        final int numberOfThreads = 2;
        CountDownLatch countDownLatch = new CountDownLatch(numberOfThreads);
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        //when
        for(int i=0;i<numberOfThreads;i++){
            executorService.execute(() -> {
                try{
                    noticeService.readNoticeBasic(saveRecruitmentNo, saveNoticeNo, loginUser.getUserNo());
                }finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();

        //then
        /**
         * 공지사항에 대한 사용자의 읽음 정보 2개가 insert 됨.
         * but, 공지사항 총 읽음 개수는 1개로 update(두번의 갱신 이상문제-마지막 commit 반영)
         * 같은 공시사항에 대해서 하나의 읽음 정보만 insert 가능
         */
        Notice findNotice = noticeRepository.findById(saveNoticeNo).get();
        List<Confirmation> findConfirmations = confirmationRepository.findConfirmations(loginUser.getUserNo(), RealWorkCode.NOTICE, saveNoticeNo);
        assertThat(findNotice.getCheckedNum()).isEqualTo(1);
        assertThat(findConfirmations.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("낙관적락 이용 테스트")
    public void concurrent_OPTIMSTIC_LOCK() throws InterruptedException {
        //given
        final int numberOfThreads = 2;
        final User loginUser = 사용자_추가("test");
        CountDownLatch countDownLatch = new CountDownLatch(numberOfThreads);
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        //when
        for(int i=0;i<numberOfThreads;i++){
            executorService.execute(() -> {
                try{
                    noticeService.readNoticeOPTIMISTIC_LOCK(saveRecruitmentNo, saveNoticeNo, loginUser.getUserNo());
                }catch (ObjectOptimisticLockingFailureException e){
                    log.info("동시성 문제 발생");
                }finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();

        //then
        Notice findNotice = noticeRepository.findById(saveNoticeNo).get();
        List<Confirmation> findConfirmations = confirmationRepository.findConfirmations(loginUser.getUserNo(), RealWorkCode.NOTICE, saveNoticeNo);
        assertThat(findNotice.getCheckedNum()).isEqualTo(1);
        assertThat(findConfirmations.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("복합 UNIQUE KEY 이용 테스트")
    public void concurrent_UNIQUE_KEY() throws InterruptedException {
        //given
        final int numberOfThreads = 2;
        final User loginUser = 사용자_추가("test");
        CountDownLatch countDownLatch = new CountDownLatch(numberOfThreads);
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        //when
        for(int i=0;i<numberOfThreads;i++){
            executorService.execute(() -> {
                try{
                    noticeService.readNotice_UNIQUE_KEY(saveRecruitmentNo, saveNoticeNo, loginUser.getUserNo());
                }catch(DataIntegrityViolationException e){
                    /**
                     * Spring Unique Key 문제시 해당 예외가 터지는데 Unique Key, Foreign Key, Not NULL 인지 알수 없음.
                     * 비지니스 로직에서 Unique key 검사 로직(이미 존재, exists~)을 넣어도 여전히 동시성 문제 남아있음.
                     *
                     * 김영한님 의견:
                     * 애플리케이션 전체를 볼때 PK나 index에서 단건을 조회하는 경우 성능에 미치는 영향은 거의 미미합니다.
                     * 그리고 대부분의 애플리케이션은 조회가 많지, 이렇게 저장하는 비즈니스 로직의 호출은 상대적으로 매우 적습니다.
                     * 그래서 이 경우 성능은 크게 고려대상이 안됩니다. => 하지만 동시성 문제 남아있다.
                     */
                    log.info("DB KEY 예외 발생");
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();

        //then
        Notice findNotice = noticeRepository.findById(saveNoticeNo).get();
        List<Confirmation> findConfirmations = confirmationRepository.findConfirmations(loginUser.getUserNo(), RealWorkCode.NOTICE, saveNoticeNo);
        assertThat(findNotice.getCheckedNum()).isEqualTo(1);
        assertThat(findConfirmations.size()).isEqualTo(1);
    }


    private User 사용자_추가(String value){
        User loginUser = User.createUser(value, value, value, value, Gender.M, LocalDate.now(), "picture",
                true, true, true, Role.USER, "kakao", value, null);
        return userRepository.save(loginUser);
    }
}
