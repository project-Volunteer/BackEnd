package project.volunteer.domain.recruitment.repository;

import static project.volunteer.domain.recruitment.domain.QRecruitment.recruitment;
import static project.volunteer.domain.user.domain.QUser.user;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import project.volunteer.domain.image.domain.QImage;
import project.volunteer.domain.image.domain.QStorage;
import project.volunteer.domain.recruitment.application.dto.query.list.RecruitmentSearchCond;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.repository.dto.RecruitmentAndUserDetail;
import project.volunteer.domain.recruitment.application.dto.query.list.RecruitmentList;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.global.common.component.RealWorkCode;

@Repository
@RequiredArgsConstructor
public class RecruitmentQueryDSLRepositoryImpl implements RecruitmentQueryDSLRepository {
    private final static QImage recruitmentImage = new QImage("recruitmentImage");
    private final static QImage userImage = new QImage("userImage");
    private final static QStorage recruitmentImageStorage = new QStorage("recruitmentImageStorage");
    private final static QStorage userImageStorage = new QStorage("userImageStorage");

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<RecruitmentAndUserDetail> findRecruitmentAndUserDetailBy(Long recruitmentNo) {
        return Optional.ofNullable(queryFactory
                .select(Projections.constructor(RecruitmentAndUserDetail.class, recruitment.recruitmentNo,
                        recruitment.volunteeringCategory, recruitment.organizationName, recruitment.isIssued,
                        recruitment.volunteeringType, recruitment.volunteerType, recruitment.maxParticipationNum,
                        recruitment.timetable, recruitment.title, recruitment.content, recruitment.address,
                        recruitment.coordinate,
                        recruitmentImageStorage.imagePath, user.nickName, user.picture, userImageStorage.imagePath))
                .from(recruitment)
                .innerJoin(recruitment.writer, user)
                .leftJoin(recruitmentImage)
                .on(recruitment.recruitmentNo.eq(recruitmentImage.no),
                        recruitmentImage.realWorkCode.eq(RealWorkCode.RECRUITMENT),
                        recruitmentImage.isDeleted.eq(IsDeleted.N))
                .leftJoin(recruitmentImage.storage, recruitmentImageStorage)
                .leftJoin(userImage)
                .on(user.userNo.eq(userImage.no),
                        userImage.realWorkCode.eq(RealWorkCode.USER),
                        userImage.isDeleted.eq(IsDeleted.N))
                .leftJoin(userImage.storage, userImageStorage)
                .where(recruitment.recruitmentNo.eq(recruitmentNo),
                        recruitment.isDeleted.eq(IsDeleted.N))
                .fetchOne());
    }

    @Override
    public Slice<RecruitmentList> findRecruitmentListBy(Pageable pageable, RecruitmentSearchCond searchCond) {
        List<RecruitmentList> result = queryFactory.select(getRecruitmentListConstructor())
                .from(recruitment)
                .leftJoin(recruitmentImage)
                .on(recruitment.recruitmentNo.eq(recruitmentImage.no),
                        recruitmentImage.realWorkCode.eq(RealWorkCode.RECRUITMENT),
                        recruitmentImage.isDeleted.eq(IsDeleted.N))
                .leftJoin(recruitmentImage.storage, recruitmentImageStorage)
                .where(
                        inCategory(searchCond.getCategory()),
                        eqSidoCode(searchCond.getSido()),
                        eqSigunguCode(searchCond.getSigungu()),
                        eqVolunteeringType(searchCond.getVolunteeringType()),
                        eqVolunteerType(searchCond.getVolunteerType()),
                        eqIsIssued(searchCond.getIsIssued()),
                        recruitment.isPublished.eq(Boolean.TRUE),
                        recruitment.isDeleted.eq(IsDeleted.N)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1) //limit 보다 한개더 가져온다.
                .orderBy(recruitment.recruitmentNo.desc())
                .fetch();

        return checkEndPage(pageable, result);
    }

    @Override
    public Slice<RecruitmentList> findRecruitmentListByTitle(Pageable pageable, String keyWard) {
        List<RecruitmentList> result = queryFactory.select(getRecruitmentListConstructor())
                .from(recruitment)
                .leftJoin(recruitmentImage)
                .on(recruitment.recruitmentNo.eq(recruitmentImage.no),
                        recruitmentImage.realWorkCode.eq(RealWorkCode.RECRUITMENT),
                        recruitmentImage.isDeleted.eq(IsDeleted.N))
                .leftJoin(recruitmentImage.storage, recruitmentImageStorage)
                .where(
                        likeTitle(keyWard),
                        recruitment.isPublished.eq(Boolean.TRUE),
                        recruitment.isDeleted.eq(IsDeleted.N)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .orderBy(recruitment.recruitmentNo.desc())
                .fetch();

        return checkEndPage(pageable, result);
    }

    @Override
    public Long findRecruitmentCountBy(RecruitmentSearchCond searchCond) {
        return queryFactory
                .select(recruitment.count())
                .from(recruitment)
                .where(
                        inCategory(searchCond.getCategory()),
                        eqSidoCode(searchCond.getSido()),
                        eqSigunguCode(searchCond.getSigungu()),
                        eqVolunteeringType(searchCond.getVolunteeringType()),
                        eqVolunteerType(searchCond.getVolunteerType()),
                        eqIsIssued(searchCond.getIsIssued()),
                        recruitment.isPublished.eq(Boolean.TRUE),
                        recruitment.isDeleted.eq(IsDeleted.N)
                )
                .fetchOne();
    }

    private ConstructorExpression<RecruitmentList> getRecruitmentListConstructor() {
        return Projections.constructor(RecruitmentList.class, recruitment.recruitmentNo,
                recruitment.volunteeringCategory, recruitment.volunteeringType, recruitment.volunteerType,
                recruitment.title, recruitment.isIssued, recruitment.maxParticipationNum,
                recruitment.currentVolunteerNum, recruitment.address, recruitment.timetable,
                recruitmentImageStorage.imagePath);
    }

    private Slice<RecruitmentList> checkEndPage(Pageable pageable, List<RecruitmentList> result) {
        boolean hasNext = false;

        if (result.size() > pageable.getPageSize()) { //다음 페이지가 존재하는 경우
            result.remove(pageable.getPageSize()); //한개더 가져온 엔티티를 삭제
            hasNext = true;
        }
        return new SliceImpl<>(result, pageable, hasNext);
    }

    private BooleanExpression inCategory(List<VolunteeringCategory> categories) {
        if (Objects.isNull(categories) || categories.isEmpty()) {
            return null;
        }
        return recruitment.volunteeringCategory.in(categories);
    }

    private BooleanExpression eqVolunteeringType(VolunteeringType volunteeringType) {
        if (Objects.isNull(volunteeringType)) {
            return null;
        }
        return recruitment.volunteeringType.eq(volunteeringType);
    }

    private BooleanExpression eqVolunteerType(VolunteerType volunteerType) {
        if (Objects.isNull(volunteerType)) {
            return null;
        }
        return recruitment.volunteerType.eq(volunteerType);
    }

    private BooleanExpression eqIsIssued(Boolean isIssued) {
        if (Objects.isNull(isIssued)) {
            return null;
        }
        return recruitment.isIssued.eq(isIssued);
    }

    private BooleanExpression eqSidoCode(String sido) {
        if (!StringUtils.hasText(sido)) {
            return null;
        }
        return recruitment.address.sido.eq(sido);
    }

    private BooleanExpression eqSigunguCode(String sigungu) {
        if (!StringUtils.hasText(sigungu)) {
            return null;
        }
        return recruitment.address.sigungu.eq(sigungu);
    }

    private BooleanExpression likeTitle(String keyWard) {
        if (!StringUtils.hasText(keyWard)) {
            return null;
        }
        return recruitment.title.contains(keyWard);
    }

}
