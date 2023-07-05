package project.volunteer.domain.confirmation.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.volunteer.domain.confirmation.domain.Confirmation;
import project.volunteer.global.common.component.RealWorkCode;

import java.util.Optional;

public interface ConfirmationRepository extends JpaRepository<Confirmation, Long>, ConfirmationRepositoryCustom {

    @Query("select c " +
            "from Confirmation c " +
            "where c.user.userNo=:userNo " +
            "and c.realWorkCode=:code " +
            "and c.no=:no ")
    Optional<Confirmation> findConfirmation(@Param("userNo")Long userNo, @Param("code")RealWorkCode code, @Param("no")Long no);
}
