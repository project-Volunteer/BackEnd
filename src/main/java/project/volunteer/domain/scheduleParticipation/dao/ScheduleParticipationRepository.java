package project.volunteer.domain.scheduleParticipation.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;
import project.volunteer.global.common.component.ParticipantState;

import java.util.List;
import java.util.Optional;

public interface ScheduleParticipationRepository extends JpaRepository<ScheduleParticipation, Long> {

    @Query("select sp " +
            "from ScheduleParticipation sp " +
            "join sp.participant p on p.participant.userNo=:userNo " +
            "and sp.schedule.scheduleNo=:scheduleNo")
    Optional<ScheduleParticipation> findByUserNoAndScheduleNo(@Param("userNo")Long userNo, @Param("scheduleNo")Long scheduleNo);

    //일정 참여 중인 인원 수 반환 쿼리
    @Query("select count(sp) " +
            "from ScheduleParticipation sp " +
            "where sp.schedule.scheduleNo=:scheduleNo " +
            "and sp.state=project.volunteer.global.common.component.ParticipantState.PARTICIPATING")
    Integer countActiveParticipant(@Param("scheduleNo")Long scheduleNo);

    List<ScheduleParticipation> findBySchedule_ScheduleNo(Long scheduleNo);

    @Query("select sp " +
            "from ScheduleParticipation sp " +
            "join sp.participant p on p.participant.userNo=:userNo " +
            "where sp.schedule.scheduleNo=:scheduleNo " +
            "and sp.state=:state ")
    Optional<ScheduleParticipation> findByUserNoAndScheduleNoAndState(@Param("userNo")Long userNo, @Param("scheduleNo")Long scheduleNo,
                                                                      @Param("state") ParticipantState state);

    Optional<ScheduleParticipation> findByScheduleParticipationNoAndState(Long scheduleParticipationNo, ParticipantState state);
    List<ScheduleParticipation> findByScheduleParticipationNoIn(List<Long> spNos);
}
