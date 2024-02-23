package project.volunteer.domain.scheduleParticipation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import project.volunteer.domain.recruitmentParticipation.domain.RecruitmentParticipation;
import project.volunteer.domain.scheduleParticipation.repository.dto.CompletedScheduleDetail;
import project.volunteer.domain.scheduleParticipation.repository.dto.ParticipantDetails;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.global.common.component.ParticipantState;

import java.util.List;
import java.util.Optional;

public interface ScheduleParticipationRepository extends JpaRepository<ScheduleParticipation, Long>,
        ScheduleParticipationQueryDSLRepository {

    Boolean existsByScheduleAndRecruitmentParticipation(Schedule schedule, RecruitmentParticipation recruitmentParticipation);

    Optional<ScheduleParticipation> findByScheduleAndRecruitmentParticipation(Schedule schedule, RecruitmentParticipation participant);

    List<ScheduleParticipation> findByIdIn(List<Long> ids);









    @Query("select sp " +
            "from ScheduleParticipation sp " +
            "join sp.recruitmentParticipation p on p.user.userNo=:userNo " +
            "and sp.schedule.scheduleNo=:scheduleNo")
    Optional<ScheduleParticipation> findByUserNoAndScheduleNo(@Param("userNo") Long userNo,
                                                              @Param("scheduleNo") Long scheduleNo);

    @Query("select sp.state " +
            "from ScheduleParticipation sp " +
            "join sp.recruitmentParticipation p on p.user.userNo=:userNo " +
            "and sp.schedule.scheduleNo=:scheduleNo")
    Optional<ParticipantState> findStateBy(@Param("userNo") Long userNo, @Param("scheduleNo") Long scheduleNo);

    //일정 참여 중인 인원 수 반환 쿼리
    @Query("select count(sp) " +
            "from ScheduleParticipation sp " +
            "where sp.schedule.scheduleNo=:scheduleNo " +
            "and sp.state=project.volunteer.global.common.component.ParticipantState.PARTICIPATING")
    Integer countActiveParticipant(@Param("scheduleNo") Long scheduleNo);

    List<ScheduleParticipation> findBySchedule_ScheduleNo(Long scheduleNo);

    @Query("select sp " +
            "from ScheduleParticipation sp " +
            "join sp.schedule s " +
            "join s.recruitment r " +
            "where r.recruitmentNo =:recruitmentNo")
    List<ScheduleParticipation> findByRecruitmentNo(@Param("recruitmentNo") Long recruitmentNo);

    @Query("select sp " +
            "from ScheduleParticipation sp " +
            "join sp.recruitmentParticipation p on p.user.userNo=:userNo " +
            "where sp.schedule.scheduleNo=:scheduleNo " +
            "and sp.state=:state ")
    Optional<ScheduleParticipation> findByUserNoAndScheduleNoAndState(@Param("userNo") Long userNo,
                                                                      @Param("scheduleNo") Long scheduleNo,
                                                                      @Param("state") ParticipantState state);

    Optional<ScheduleParticipation> findByScheduleAndRecruitmentParticipationAndState(Schedule schedule, RecruitmentParticipation participant,
                                                                                      ParticipantState state);

    Optional<ScheduleParticipation> findByIdAndState(Long scheduleParticipationNo,
                                                     ParticipantState state);

    //N+1 문제를 막기 위해서 Projection + Join 방식 사용
    @Query("select new project.volunteer.domain.scheduleParticipation.repository.dto.ParticipantDetails(sp.id,u.nickName,u.email,coalesce(s.imagePath,u.picture),sp.state) "
            +
            "from ScheduleParticipation sp " +
            "join sp.recruitmentParticipation p " +
            "join p.user u " +
            "left join Image i " +
            "on i.no=u.userNo " +
            "and i.realWorkCode=project.volunteer.global.common.component.RealWorkCode.USER " +
            "and i.isDeleted=project.volunteer.global.common.component.IsDeleted.N " +
            "left join i.storage s " +
            "where sp.schedule.scheduleNo=:scheduleNo " +
            "and sp.state in :states ")
    List<ParticipantDetails> findOptimizationParticipantByScheduleAndState(@Param("scheduleNo") Long scheduleNo,
                                                                           @Param("states") List<ParticipantState> states);

    @Query("select new project.volunteer.domain.scheduleParticipation.repository.dto.CompletedScheduleDetail" +
            "(sp.schedule.scheduleNo " +
            ",sp.recruitmentParticipation.recruitment.title " +
            ",sp.schedule.scheduleTimeTable.endDay) " +
            "from ScheduleParticipation sp " +
            "where sp.recruitmentParticipation.user.userNo=:loginUserNo " +
            "and sp.state=:state")
    List<CompletedScheduleDetail> findCompletedSchedules(@Param("loginUserNo") Long loginUserNo,
                                                         @Param("state") ParticipantState state);

    @Query("select sp " +
            "from ScheduleParticipation sp " +
            "where sp.recruitmentParticipation.user.userNo=:loginUserNo " +
            "and sp.state=:state")
    List<ScheduleParticipation> findScheduleListByUsernoAndStatus(@Param("loginUserNo") Long loginUserNo,
                                                                  @Param("state") ParticipantState state);

}
