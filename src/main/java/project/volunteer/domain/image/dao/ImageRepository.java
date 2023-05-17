package project.volunteer.domain.image.dao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.image.domain.RealWorkCode;

import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {

    //image, storage 한번에 쿼리로 가져오기(left join)
    @EntityGraph(attributePaths = {"storage"})
    @Query("select im from Image im " +
            "where im.realWorkCode = :code and im.no = :no and im.isDeleted=project.volunteer.global.common.component.IsDeleted.N")
    public Optional<Image> findEGStorageByCodeAndNo(@Param("code") RealWorkCode realWorkCode, @Param("no") Long no);

    @Query("select img from Image img " +
            "where img.realWorkCode=:code and img.no=:no and img.isDeleted=project.volunteer.global.common.component.IsDeleted.N")
    public List<Image> findImagesByCodeAndNo(@Param("code") RealWorkCode code, @Param("no") Long no);

    @Query("select img from Image img " +
            "where img.realWorkCode= :code and img.no= :no and img.isDeleted=project.volunteer.global.common.component.IsDeleted.N")
    public Optional<Image> findByCodeAndNo(@Param("code") RealWorkCode code, @Param("no") Long no);

}
