package project.volunteer.domain.recruitmentParticipation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.volunteer.domain.recruitmentParticipation.domain.RecruitmentParticipation;
import project.volunteer.domain.recruitmentParticipation.repository.dto.RecruitmentParticipantDetail;
import project.volunteer.domain.recruitmentParticipation.repository.dto.UserRecruitmentDetails;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.ParticipantState;

import java.util.List;
import java.util.Optional;

public interface RecruitmentParticipationRepository extends JpaRepository<RecruitmentParticipation, Long> {
    Boolean existsByRecruitmentAndUser(Recruitment recruitment, User user);

    Optional<RecruitmentParticipation> findByRecruitmentAndUser(Recruitment recruitment, User participant);

    List<RecruitmentParticipation> findByIdIn(List<Long> ids);

    List<RecruitmentParticipation> findByRecruitment_RecruitmentNo(Long recruitmentNo);

    @Query("SELECT p.state FROM RecruitmentParticipation p "
            + "WHERE p.recruitment.recruitmentNo=:recruitmentNo AND p.user.userNo=:userNo")
    Optional<ParticipantState> findStateBy(@Param("recruitmentNo") Long recruitmentNo, @Param("userNo") Long userNo);

    @Query("select new project.volunteer.domain.recruitmentParticipation.repository.dto.RecruitmentParticipantDetail" +
            "(p.state, p.id, u.nickName, coalesce(s.imagePath, u.picture)) " +
            "from RecruitmentParticipation p " +
            "join p.user as u " +
            "left join Image i " +
            "on u.userNo = i.no " +
            "and i.realWorkCode=project.volunteer.global.common.component.RealWorkCode.USER " +
            "and i.isDeleted=project.volunteer.global.common.component.IsDeleted.N " +
            "left join i.storage as s " +
            "where p.recruitment.recruitmentNo=:no " +
            "and p.state in :states ")
    List<RecruitmentParticipantDetail> findParticipantsDetailBy(@Param("no") Long recruitmentNo, @Param("states") List<ParticipantState> states);















    Optional<RecruitmentParticipation> findByRecruitment_RecruitmentNoAndUser_UserNo(Long recruitmentNo, Long participantNo);

    /**
     * 봉사 모집글 팀원 확인 쿼리
     * JPA 에서 select 절 exist 지원하지 않음
     * "count" 성능 이슈 발생할 거 같은데??
     */
    //봉사 모집글 팀원 확인 쿼리
    @Query("select count(p.id) > 0 " +
            "from RecruitmentParticipation p " +
            "where p.recruitment.recruitmentNo=:recruitmentNo " +
            "and p.user.userNo=:userNo " +
            "and p.state=project.volunteer.global.common.component.ParticipantState.JOIN_APPROVAL")
    Boolean existRecruitmentTeamMember(@Param("recruitmentNo") Long recruitmentNo, @Param("userNo") Long userNo);

    @Query("select p " +
           "from RecruitmentParticipation p " +
           "where p.user.userNo=:loginUserNo")
    List<RecruitmentParticipation> findJoinStatusByTeamUserno(@Param("loginUserNo") Long loginUserNo);

    @Query("select new project.volunteer.domain.recruitmentParticipation.repository.dto.UserRecruitmentDetails" +
                "(p.recruitment.recruitmentNo" +
                ", s.imagePath" +
                ", p.recruitment.timetable.startDay" +
                ", p.recruitment.timetable.endDay" +
                ", p.recruitment.title" +
                ", p.recruitment.address.sido" +
                ", p.recruitment.address.sigungu" +
                ", p.recruitment.address.details" +
                ", p.recruitment.volunteeringCategory" +
                ", p.recruitment.volunteeringType"+
                ", p.recruitment.isIssued" +
                ", p.recruitment.volunteerType) "+
            "from RecruitmentParticipation p " +
            "left join Image i " +
            "on p.recruitment.recruitmentNo=(i.no) " +
            "and i.realWorkCode=project.volunteer.global.common.component.RealWorkCode.RECRUITMENT " +
            "and i.isDeleted=project.volunteer.global.common.component.IsDeleted.N " +
            "left join i.storage as s " +
            "where p.user.userNo=:loginUserNo " +
            "and p.state=:state " +
            "and p.recruitment.isDeleted=project.volunteer.global.common.component.IsDeleted.N ")
    List<UserRecruitmentDetails> findRecuitmentByUsernoAndStatus(@Param("loginUserNo")Long loginUserNo, @Param("state") ParticipantState state);

}
