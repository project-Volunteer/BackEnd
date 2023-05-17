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
    List<Participant> findEGParticipantByRecruitment_RecruitmentNoAndStateIn(Long recruitmentNo, List<State> states);

    Optional<Participant> findByRecruitment_RecruitmentNoAndParticipant_UserNo(Long recruitmentNo, Long userId);

    List<Participant> findByRecruitment_RecruitmentNoAndParticipant_UserNoIn(Long recruitmentNo, List<Long> userNos);

    @Query("select p from Participant p " +
            "where p.recruitment.recruitmentNo = :recruitmentNo " +
            "and p.participant.userNo = :userNo " +
            "and p.state = :state")
    Optional<Participant> findByRecruitmentNoAndParticipantNoAndState(@Param("recruitmentNo")Long recruitmentNo, @Param("userNo")Long userNo,
                                                                      @Param("state")State state);

    //봉사 모집글 팀원 신청가능한 인원 반환 쿼리
    @Query("select count(p) from Participant p " +
            "join p.recruitment r " +
            "where p.recruitment.recruitmentNo=:no " +
            "and p.state=project.volunteer.global.common.component.State.JOIN_APPROVAL")
    Integer countAvailableParticipants(@Param("no") Long recruitmentNo);

}
