package project.volunteer.domain.scheduleParticipation.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import project.volunteer.domain.scheduleParticipation.dao.dto.CompletedScheduleDetail;
import project.volunteer.domain.scheduleParticipation.dao.dto.ParticipantDetails;
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

    List<ScheduleParticipation> findBySchedule_ScheduleNoAndState(Long scheduleNo, ParticipantState state);

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

    //N+1 문제를 막기 위해서 Projection + Join 방식 사용
    @Query("select new project.volunteer.domain.scheduleParticipation.dao.dto.ParticipantDetails(u.userNo,u.nickName,u.email,coalesce(s.imagePath,u.picture),sp.state) " +
            "from ScheduleParticipation sp " +
            "join sp.participant p " +
            "join p.participant u " +
            "left join Image i " +
            "on i.no=u.userNo " +
            "and i.realWorkCode=project.volunteer.global.common.component.RealWorkCode.USER " +
            "and i.isDeleted=project.volunteer.global.common.component.IsDeleted.N " +
            "left join i.storage s " +
            "where sp.schedule.scheduleNo=:scheduleNo " +
            "and sp.state in :states ")
    List<ParticipantDetails> findParticipantsByOptimization(@Param("scheduleNo") Long scheduleNo, @Param("states") List<ParticipantState> states);

    @Query("select new project.volunteer.domain.scheduleParticipation.dao.dto.CompletedScheduleDetail(" +
            "sp.schedule.scheduleNo, " +
            "sp.participant.recruitment.title, " +
            "sp.schedule.scheduleTimeTable.endDay) " +
            "from ScheduleParticipation sp " +
            "where sp.participant.participant.userNo=:loginUserNo " +
            "and sp.state=:state")
    List<CompletedScheduleDetail> findCompletedSchedules(@Param("loginUserNo") Long loginUserNo, @Param("state") ParticipantState state);


    @Query("select sp " +
            "from ScheduleParticipation sp " +
            "where sp.participant.participant.userNo=:loginUserNo " +
            "and sp.state=project.volunteer.global.common.component.ParticipantState.PARTICIPATION_COMPLETE_APPROVAL ")
    List<ScheduleParticipation> findScheduleJoinHistoryByUserno(@Param("loginUserNo")Long loginUserNo);

}
