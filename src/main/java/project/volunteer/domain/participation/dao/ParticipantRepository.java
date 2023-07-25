package project.volunteer.domain.participation.dao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.volunteer.domain.participation.dao.dto.ParticipantStateDetails;
import project.volunteer.domain.participation.dao.dto.UserRecruitmentDetails;
import project.volunteer.domain.participation.domain.Participant;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;
import project.volunteer.global.common.component.ParticipantState;

import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    //참여자 매핑 정보, 참여자 정보 쿼리 한번에 가져오기(left join)
    @EntityGraph(attributePaths = {"participant"})
    List<Participant> findEGParticipantByRecruitment_RecruitmentNoAndStateIn(Long recruitmentNo, List<ParticipantState> states);

    @Query("select new project.volunteer.domain.participation.dao.dto.ParticipantStateDetails" +
            "(p.state, u.userNo, u.nickName, coalesce(s.imagePath, u.picture)) " +
            "from Participant p " +
            "join p.participant as u " +
            "left join Image i " +
            "on u.userNo = i.no " +
            "and i.realWorkCode=project.volunteer.global.common.component.RealWorkCode.USER " +
            "and i.isDeleted=project.volunteer.global.common.component.IsDeleted.N " +
            "left join i.storage as s " +
            "where p.recruitment.recruitmentNo=:no " +
            "and p.state in :states ")
    List<ParticipantStateDetails> findParticipantsByOptimization(@Param("no") Long recruitmentNo, @Param("states") List<ParticipantState> states);

    Optional<Participant> findByRecruitment_RecruitmentNoAndParticipant_UserNo(Long recruitmentNo, Long userId);

    List<Participant> findByRecruitment_RecruitmentNoAndParticipant_UserNoIn(Long recruitmentNo, List<Long> userNos);

    @Query("select p from Participant p " +
            "where p.recruitment.recruitmentNo = :recruitmentNo " +
            "and p.participant.userNo = :userNo " +
            "and p.state = :state")
    Optional<Participant> findByRecruitmentNoAndParticipantNoAndState(@Param("recruitmentNo")Long recruitmentNo, @Param("userNo")Long userNo,
                                                                      @Param("state") ParticipantState state);

    //봉사 모집글 팀원 인원 반환 쿼리
    @Query("select count(p) from Participant p " +
            "join p.recruitment r " +
            "where p.recruitment.recruitmentNo=:no " +
            "and p.state=project.volunteer.global.common.component.ParticipantState.JOIN_APPROVAL")
    Integer countAvailableParticipants(@Param("no") Long recruitmentNo);

    /**
     * 봉사 모집글 팀원 확인 쿼리
     * JPA 에서 select 절 exist 지원하지 않음
     * "count" 성능 이슈 발생할 거 같은데??
     */
    //봉사 모집글 팀원 확인 쿼리
    @Query("select count(p.participantNo) > 0 " +
            "from Participant p " +
            "where p.recruitment.recruitmentNo=:recruitmentNo " +
            "and p.participant.userNo=:userNo " +
            "and p.state=project.volunteer.global.common.component.ParticipantState.JOIN_APPROVAL")
    Boolean existRecruitmentTeamMember(@Param("recruitmentNo") Long recruitmentNo, @Param("userNo") Long userNo);

    @Query("select p " +
           "from Participant p " +
           "where p.participant.userNo=:loginUserNo")
    List<Participant> findJoinStatusByTeamUserno(@Param("loginUserNo") Long loginUserNo);

    @Query("select new project.volunteer.domain.participation.dao.dto.UserRecruitmentDetails" +
                "(p.recruitment.recruitmentNo" +
                ", i.staticImageName" +
                ", s.imagePath" +
                ", p.recruitment.VolunteeringTimeTable.startDay" +
                ", p.recruitment.VolunteeringTimeTable.endDay" +
                ", p.recruitment.title" +
                ", p.recruitment.address.sido" +
                ", p.recruitment.address.sigungu" +
                ", p.recruitment.address.details" +
                ", p.recruitment.volunteeringCategory" +
                ", p.recruitment.volunteeringType"+
                ", p.recruitment.isIssued" +
                ", p.recruitment.volunteerType) "+
            "from Participant p " +
            "left join Image i " +
            "on p.recruitment.recruitmentNo=(i.no) " +
            "and i.realWorkCode=project.volunteer.global.common.component.RealWorkCode.RECRUITMENT " +
            "and i.isDeleted=project.volunteer.global.common.component.IsDeleted.N " +
            "left join i.storage as s " +
            "where p.participant.userNo=:loginUserNo " +
            "and p.state=:state " +
            "and p.recruitment.isDeleted=project.volunteer.global.common.component.IsDeleted.N ")
    List<UserRecruitmentDetails> findRecuitmentByUsernoAndStatus(@Param("loginUserNo")Long loginUserNo, @Param("state") ParticipantState state);

}
