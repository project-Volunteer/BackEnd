package project.volunteer.domain.recruitment.repository.queryDto;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.domain.recruitment.repository.queryDto.dto.QRecruitmentListQuery;
import project.volunteer.domain.recruitment.repository.queryDto.dto.RecruitmentListQuery;
import project.volunteer.domain.recruitment.domain.*;
import project.volunteer.domain.recruitment.repository.queryDto.dto.RecruitmentCond;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.global.common.component.ParticipantState;

import static project.volunteer.domain.image.domain.QStorage.storage;
import static project.volunteer.domain.recruitment.domain.QRecruitment.recruitment;
import static project.volunteer.domain.image.domain.QImage.image;
import static project.volunteer.domain.participation.domain.QParticipant.participant1;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RecruitmentQueryDtoRepositoryImpl implements RecruitmentQueryDtoRepository {

    private final JPAQueryFactory jpaQueryFactory;

   //TODO: 추후 no offset 으로 성능 최적화 고려해보기
    @Override
    public Slice<RecruitmentListQuery> findRecruitmentJoinImageBySearchType(Pageable pageable, RecruitmentCond searchType) {
        //모집글 이미지 같이 조회(최적화)
        List<RecruitmentListQuery> content = jpaQueryFactory
                .select(
                        new QRecruitmentListQuery(recruitment.recruitmentNo, recruitment.volunteeringCategory,recruitment.title,
                                recruitment.address.sido, recruitment.address.sigungu, recruitment.address.fullName,
                                recruitment.timetable.startDay, recruitment.timetable.endDay, recruitment.volunteeringType,
                                recruitment.volunteerType, recruitment.isIssued, recruitment.maxParticipationNum, storage.imagePath))
                .from(recruitment)
                .leftJoin(image)
                .on(
                        recruitment.recruitmentNo.eq(image.no),
                        image.realWorkCode.eq(RealWorkCode.RECRUITMENT)
                )
                .leftJoin(image.storage, storage)
                .where(
                        containCategory(searchType.getCategory()),
                        eqSidoCode(searchType.getSido()),
                        eqSigunguCode(searchType.getSigungu()),
                        eqVolunteeringType(searchType.getVolunteeringType()),
                        eqVolunteerType(searchType.getVolunteerType()),
                        eqIsIssued(searchType.getIsIssued()),

                        recruitment.isPublished.eq(Boolean.TRUE), //임시저장된 모집글은 제외
                        recruitment.isDeleted.eq(IsDeleted.N)     //삭제 게시물 제외
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1) //limit 보다 한개더 가져온다.
                .orderBy(setSort(pageable))
                .fetch(); //counting 쿼리 X

        return checkEndPage(pageable, content);
    }

    @Override
    public Slice<RecruitmentListQuery> findRecruitmentJoinImageByTitle(Pageable pageable, String title) {
        List<RecruitmentListQuery> content = jpaQueryFactory
                .select(
                        new QRecruitmentListQuery(recruitment.recruitmentNo, recruitment.volunteeringCategory,recruitment.title,
                                recruitment.address.sido, recruitment.address.sigungu, recruitment.address.fullName,
                                recruitment.timetable.startDay, recruitment.timetable.endDay, recruitment.volunteeringType,
                                recruitment.volunteerType, recruitment.isIssued, recruitment.maxParticipationNum, storage.imagePath))
                .from(recruitment)
                .leftJoin(image)
                .on(
                        recruitment.recruitmentNo.eq(image.no),
                        image.realWorkCode.eq(RealWorkCode.RECRUITMENT)
                )
                .leftJoin(image.storage, storage)
                .where(
                        //TODO: ElasticSearch or FullText search 고려 해보기
                        likeTitle(title),
                        recruitment.isPublished.eq(Boolean.TRUE), //임시저장된 모집글은 제외
                        recruitment.isDeleted.eq(IsDeleted.N)     //삭제 게시물 제외
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1) //limit 보다 한개더 가져온다.
                .orderBy(setSort(pageable))
                .fetch(); //counting 쿼리 X

        return checkEndPage(pageable, content);
    }

    @Override
    public Long findRecruitmentCountBySearchType(RecruitmentCond searchType) {
        return jpaQueryFactory
                .select(recruitment.count())
                .from(recruitment)
                .where(
                        containCategory(searchType.getCategory()),
                        eqSidoCode(searchType.getSido()),
                        eqSigunguCode(searchType.getSigungu()),
                        eqVolunteeringType(searchType.getVolunteeringType()),
                        eqVolunteerType(searchType.getVolunteerType()),
                        eqIsIssued(searchType.getIsIssued()),

                        recruitment.isPublished.eq(Boolean.TRUE), //임시저장된 모집글은 제외
                        recruitment.isDeleted.eq(IsDeleted.N)     //삭제 게시물 제외
                )
                .fetchOne();
    }

    @Override
    public Long countParticipants(Long recruitmentNo){
        return jpaQueryFactory
                .select(participant1.count())
                .from(participant1)
                .where(
                        participant1.recruitment.recruitmentNo.eq(recruitmentNo),
                        participant1.state.eq(ParticipantState.JOIN_APPROVAL)) //참여 승인자만
                .fetchOne();
    }

    private Slice<RecruitmentListQuery> checkEndPage(Pageable pageable, List<RecruitmentListQuery> content) {
        boolean hasNext = false;

        if(content.size() > pageable.getPageSize()){ //다음 페이지가 존재하는 경우
            content.remove(pageable.getPageSize()); //한개더 가져온 엔티티를 삭제
            hasNext = true;
        }
        return new SliceImpl<>(content, pageable, hasNext);
    }

    //정렬 확장 가능성 열어둠.(해당 로직은 정렬 기준이 하나만 가능)
    private OrderSpecifier setSort(Pageable pageable) {

        if(!pageable.getSort().isEmpty()) {
            for(Sort.Order order : pageable.getSort()){
                Order direction = order.getDirection().isAscending() ? Order.ASC:Order.DESC;
                switch (order.getProperty()){
                    case "likeCount":
                        return new OrderSpecifier(direction, recruitment.likeCount);
                    case "viewCount":
                        return new OrderSpecifier(direction, recruitment.viewCount);
                }
            }
        }
        return new OrderSpecifier(Order.ASC, recruitment.recruitmentNo); //default: 생성 오름차순
    }

    private BooleanExpression containCategory(List<VolunteeringCategory> categories) {
        return (!categories.isEmpty())?(recruitment.volunteeringCategory.in(categories)):null;
    }
    private BooleanExpression eqSidoCode(String sido) {
        return (StringUtils.hasText(sido))?(recruitment.address.sido.eq(sido)):null;
    }
    private BooleanExpression eqSigunguCode(String sigungu){
        return (StringUtils.hasText(sigungu))?(recruitment.address.sigungu.eq(sigungu)):null;
    }
    private BooleanExpression eqVolunteeringType(VolunteeringType volunteeringType) {
        return (!ObjectUtils.isEmpty(volunteeringType))?
                (recruitment.volunteeringType.eq(volunteeringType)):null;
    }
    private BooleanExpression eqVolunteerType(VolunteerType volunteerType) {
        return (!ObjectUtils.isEmpty(volunteerType))?
                (recruitment.volunteerType.eq(volunteerType)):null;
    }
    private BooleanExpression eqIsIssued(Boolean isIssued){
        return (!ObjectUtils.isEmpty(isIssued))?(recruitment.isIssued.eq(isIssued)):null;
    }
    private BooleanExpression likeTitle(String title){
        return (StringUtils.hasText(title))?(recruitment.title.contains(title)):null;
        //return (StringUtils.hasText(title))?(recruitment.title.like("%" + title + "%")):null;
    }
}
