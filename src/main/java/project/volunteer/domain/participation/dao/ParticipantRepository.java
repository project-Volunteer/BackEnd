package project.volunteer.domain.participation.dao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import project.volunteer.domain.participation.domain.Participant;

import javax.persistence.Entity;
import java.util.List;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    //참여자 매핑 정보, 참여자 정보 쿼리 한번에 가져오기(left join)
    @EntityGraph(attributePaths = {"participant"})
    List<Participant> findEGParticipantByRecruitment_RecruitmentNo(Long recruitmentNo);
}
