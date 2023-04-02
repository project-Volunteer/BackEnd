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
import project.volunteer.domain.image.domain.QImage;
import project.volunteer.domain.image.domain.RealWorkCode;
import project.volunteer.domain.participation.domain.QParticipant;
import project.volunteer.domain.recruitment.dao.queryDto.dto.QRecruitmentQueryDto;
import project.volunteer.domain.recruitment.dao.queryDto.dto.QRepeatPeriodQueryDto;
import project.volunteer.domain.recruitment.dao.queryDto.dto.RecruitmentQueryDto;
import project.volunteer.domain.recruitment.dao.queryDto.dto.RepeatPeriodQueryDto;
import project.volunteer.domain.recruitment.domain.*;
import project.volunteer.domain.recruitment.dao.queryDto.dto.SearchType;
import project.volunteer.domain.repeatPeriod.domain.QRepeatPeriod;
import project.volunteer.domain.storage.domain.QStorage;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RecruitmentQueryDtoRepositoryImpl implements RecruitmentQueryDtoRepository {

    private final JPAQueryFactory jpaQueryFactory;

    //따로 static 으로 분리하기??
    private final QRecruitment qRecruitment = new QRecruitment("recruitment");
    private final QImage qImage = new QImage("image");
    private final QStorage qStorage = new QStorage("storage");
    private final QRepeatPeriod qRepeatPeriod = new QRepeatPeriod("repeatPeriod");
    private final QParticipant qParticipant = new QParticipant("participant");

    @Override
    public Slice<RecruitmentQueryDto> findRecruitmentDtos(Pageable pageable, SearchType searchType) {

        //전체 모집글,이미지,저장소 join 조회(Slice 처리)
        Slice<RecruitmentQueryDto> result = findRecruitmentJoinImageBySearchType(pageable, searchType);

        result.getContent().stream()
                .forEach(dto -> {
                    //각 모집글에 해당 하는 반복주기 엔티티 리스트 조회(반복주기 엔티티는 N 이므로 별도 조회)
                    List<RepeatPeriodQueryDto> repeatPeriodDto = findRepeatPeriodDto(dto.getNo());
                    dto.setRepeatPeriodList(repeatPeriodDto);

                    //각 모집글에 참여자 리스트 count(참여자 엔티티는 N 이므로 별도 조회)
                    Long currentParticipantNum = countParticipants(dto.getNo());
                    dto.setCurrentVolunteerNum(currentParticipantNum);
                } );

        /**
         * 현재 root 쿼리 1번에 컬렉션 쿼리 N번( 반복주기:N + 참여자 수:1) 발생
         * 추후 최적화가 필요한 부분
         */

        return result;
    }

    //offset 기반 Slice -> 추후 no offset 으로 성능 최적화 가능
    @Override
    public Slice<RecruitmentQueryDto> findRecruitmentJoinImageBySearchType(Pageable pageable, SearchType searchType) {

        //모집글&이미지&저장소 모두 1:1이니 한번에 받아오기
        //모집글에 반드시 이미지가 존재?(static, upload) : innerJoin, leftJoin
        //이미지에 저장소가 없을수 있으니 : leftJoin
        List<RecruitmentQueryDto> content = jpaQueryFactory
                .select(
                        new QRecruitmentQueryDto(qRecruitment.recruitmentNo, qRecruitment.title, qRecruitment.sido, qRecruitment.sigungu,
                                qRecruitment.VolunteeringTimeTable.startDay, qRecruitment.VolunteeringTimeTable.endDay, qRecruitment.volunteeringType,
                                qRecruitment.volunteerType, qRecruitment.isIssued, qRecruitment.volunteerNum, qRecruitment.VolunteeringTimeTable.progressTime,
                                qImage.staticImageName, qStorage.imagePath))
                .from(qRecruitment)
                .leftJoin(qImage).on(qRecruitment.recruitmentNo.eq(qImage.no)) //recruitment, image left join
                .leftJoin(qImage.storage, qStorage).on(qImage.realWorkCode.eq(RealWorkCode.RECRUITMENT)) //image, storage left join & 사진 타입이 모집글인거만
                .where(
                        containCategory(searchType.getCategory()),
                        eqSidoCode(searchType.getSido()),
                        eqSigunguCode(searchType.getSigungu()),
                        eqVolunteeringType(searchType.getVolunteeringType()),
                        eqVolunteerType(searchType.getVolunteerType()),
                        eqIsIssued(searchType.getIsIssued()),
                        qRecruitment.isPublished.eq(Boolean.TRUE) //임시저장된 모집글은 제외
                        //추후 삭제된 게시물도 제외 필요.
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1) //limit 보다 한개더 가져온다.
                .orderBy(setSort(pageable))
                .fetch(); //counting 쿼리 X

        return checkEndPage(pageable, content);
    }

    private List<RepeatPeriodQueryDto> findRepeatPeriodDto(Long recruitmentNo){
        return jpaQueryFactory
                .select(new QRepeatPeriodQueryDto(qRepeatPeriod.period, qRepeatPeriod.week, qRepeatPeriod.day))
                .from(qRepeatPeriod)
                .where(qRepeatPeriod.recruitment.recruitmentNo.eq(recruitmentNo))
                .fetch();
    }

    private Long countParticipants(Long recruitmentNo){
        return jpaQueryFactory
                .select(qParticipant.count())
                .from(qParticipant)
                .where(
                        qParticipant.recruitment.recruitmentNo.eq(recruitmentNo),
                        qParticipant.isApproved.eq(Boolean.TRUE)) //참여 승인자만
                .fetchOne();
    }

    private Slice<RecruitmentQueryDto> checkEndPage(Pageable pageable, List<RecruitmentQueryDto> content) {
        boolean hasNext = false;

        if(content.size() > pageable.getPageSize()){ //다음 페이지가 존재하는 경우
            content.remove(pageable.getPageSize()); //한개더 가져온 엔티티를 삭제
            hasNext = true;
        }
        return new SliceImpl<>(content, pageable, hasNext);
    }

    //정렬 확장 가능성 열어둠.(해당 로직 정렬 기준이 하나만 가능)
    private OrderSpecifier setSort(Pageable pageable) {

        if(!pageable.getSort().isEmpty()) {
            for(Sort.Order order : pageable.getSort()){
                Order direction = order.getDirection().isAscending() ? Order.ASC:Order.DESC;
                switch (order.getProperty()){
                    case "likeCount":
                        return new OrderSpecifier(direction, qRecruitment.likeCount);
                    case "viewCount":
                        return new OrderSpecifier(direction, qRecruitment.viewCount);
                }
            }
        }
        return new OrderSpecifier(Order.ASC, qRecruitment.createdDate); //default: 최신순
    }

    private BooleanExpression containCategory(List<VolunteeringCategory> categories) {
        return (!categories.isEmpty())?(qRecruitment.volunteeringCategory.in(categories)):null;
    }
    private BooleanExpression eqSidoCode(String sido) {
        return (StringUtils.hasText(sido))?(qRecruitment.sido.eq(sido)):null;
    }
    private BooleanExpression eqSigunguCode(String sigungu){
        return (StringUtils.hasText(sigungu))?(qRecruitment.sigungu.eq(sigungu)):null;
    }
    private BooleanExpression eqVolunteeringType(VolunteeringType volunteeringType) {
        return (!ObjectUtils.isEmpty(volunteeringType))?
                (qRecruitment.volunteeringType.eq(volunteeringType)):null;
    }
    private BooleanExpression eqVolunteerType(VolunteerType volunteerType) {
        return (!ObjectUtils.isEmpty(volunteerType))?
                (qRecruitment.volunteerType.eq(volunteerType)):null;
    }
    private BooleanExpression eqIsIssued(Boolean isIssued){
        return (!ObjectUtils.isEmpty(isIssued))?(qRecruitment.isIssued.eq(isIssued)):null;
    }

}
