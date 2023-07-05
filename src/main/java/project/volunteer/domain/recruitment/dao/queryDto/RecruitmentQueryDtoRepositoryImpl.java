package project.volunteer.domain.recruitment.dao.queryDto;

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
import project.volunteer.domain.recruitment.dao.queryDto.dto.QRecruitmentListQuery;
import project.volunteer.domain.recruitment.dao.queryDto.dto.RecruitmentListQuery;
import project.volunteer.domain.recruitment.domain.*;
import project.volunteer.domain.recruitment.dao.queryDto.dto.RecruitmentCond;
import project.volunteer.domain.repeatPeriod.domain.Day;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.global.common.component.ParticipantState;

import static project.volunteer.domain.recruitment.domain.QRecruitment.recruitment;
import static project.volunteer.domain.image.domain.QImage.image;
import static project.volunteer.domain.storage.domain.QStorage.storage;
import static project.volunteer.domain.repeatPeriod.domain.QRepeatPeriod.repeatPeriod;
import static project.volunteer.domain.participation.domain.QParticipant.participant1;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RecruitmentQueryDtoRepositoryImpl implements RecruitmentQueryDtoRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Slice<RecruitmentListQuery> findRecruitmentDtos(Pageable pageable, RecruitmentCond searchType) {

        //전체 모집글,이미지,저장소 join 조회(Slice 처리)
        Slice<RecruitmentListQuery> result = findRecruitmentJoinImageBySearchType(pageable, searchType);

        result.getContent().stream()
                .forEach(dto -> {
                    //각 모집글에 해당 하는 반복주기 엔티티 리스트 조회(반복주기 엔티티는 N 이므로 별도 조회)
                    if(dto.getVolunteeringType().equals(VolunteeringType.REG)) {
                        List<Day> days = findDays(dto.getNo());
                        dto.setDays(days);
                    }

                    //각 모집글에 참여자 리스트 count(참여자 엔티티는 N 이므로 별도 조회)
                    Long currentParticipantNum = countParticipants(dto.getNo());
                    dto.setCurrentVolunteerNum(currentParticipantNum);
                } );

        /**
         * 현재 root 쿼리 1번 결과만큼(모집글 개수) 쿼리 N번(참여자 수 count 쿼리 + 장기일경우 반복주기 쿼리) 발생
         * 추후 최적화가 필요한 부분
         */

        return result;
    }

    //offset 기반 Slice -> 추후 no offset 으로 성능 최적화 가능
    @Override
    public Slice<RecruitmentListQuery> findRecruitmentJoinImageBySearchType(Pageable pageable, RecruitmentCond searchType) {

        //모집글&이미지&저장소 모두 1:1이니 한번에 받아오기
        //모집글에 반드시 이미지가 존재?(static, upload) : innerJoin, leftJoin
        //이미지에 저장소가 없을수 있으니 : leftJoin
        List<RecruitmentListQuery> content = jpaQueryFactory
                .select(
                        new QRecruitmentListQuery(recruitment.recruitmentNo, recruitment.title,
                                recruitment.address.sido, recruitment.address.sigungu,
                                recruitment.VolunteeringTimeTable.startDay, recruitment.VolunteeringTimeTable.endDay, recruitment.volunteeringType,
                                recruitment.volunteerType, recruitment.isIssued, recruitment.volunteerNum, recruitment.VolunteeringTimeTable.progressTime,
                                image.staticImageName, storage.imagePath))
                .from(recruitment)
                .leftJoin(image).on(recruitment.recruitmentNo.eq(image.no)) //recruitment, image left join
                .leftJoin(image.storage, storage).on(image.realWorkCode.eq(RealWorkCode.RECRUITMENT)) //image, storage left join & 사진 타입이 모집글인거만
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

    private List<Day> findDays(Long recruitmentNo){
        return jpaQueryFactory
                .select(repeatPeriod.day)
                .from(repeatPeriod)
                .where(repeatPeriod.recruitment.recruitmentNo.eq(recruitmentNo))
                .fetch();
    }

    private Long countParticipants(Long recruitmentNo){
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
        return new OrderSpecifier(Order.ASC, recruitment.createdDate); //default: 최신순
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

}
