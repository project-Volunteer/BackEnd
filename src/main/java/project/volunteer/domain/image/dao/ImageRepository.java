package project.volunteer.domain.image.dao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.image.domain.RealWorkCode;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {

    //image, storage 한번에 쿼리로 가져오기(left join)
    @EntityGraph(attributePaths = {"storage"})
    public Optional<Image> findByRealWorkCodeAndNo(RealWorkCode realWorkCode, Long no);
}
