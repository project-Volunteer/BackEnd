package project.volunteer.domain.participation.dao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.volunteer.domain.participation.domain.Participant;
import project.volunteer.global.common.component.State;

import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    //참여자 매핑 정보, 참여자 정보 쿼리 한번에 가져오기(left join)
    @EntityGraph(attributePaths = {"participant"})
    List<Participant> findEGParticipantByRecruitment_RecruitmentNo(Long recruitmentNo);

    Optional<Participant> findByRecruitment_RecruitmentNoAndParticipant_UserNo(Long recruitmentNo, Long userId);

    @Query("select p from Participant p " +
            "where p.recruitment.recruitmentNo = :recruitmentNo " +
            "and p.participant.userNo = :userNo " +
            "and p.state = :state")
    Optional<Participant> findByState(@Param("recruitmentNo")Long recruitmentNo, @Param("userNo")Long userNo, @Param("state")State state);

    List<Participant> findByRecruitment_RecruitmentNoAndParticipant_UserNoIn(Long recruitmentNo, List<Long> userNos);

}
