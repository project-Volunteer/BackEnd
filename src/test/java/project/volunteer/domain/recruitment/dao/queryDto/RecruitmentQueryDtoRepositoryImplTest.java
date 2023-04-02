package project.volunteer.domain.recruitment.dao.queryDto;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.dao.queryDto.dto.RecruitmentQueryDto;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.dao.queryDto.dto.SearchType;
import project.volunteer.global.common.component.Timetable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
@Transactional
class RecruitmentQueryDtoRepositoryImplTest {

    @Autowired
    RecruitmentQueryDtoRepository recruitmentQueryDtoRepository;
    @Autowired
    RecruitmentRepository recruitmentRepository;
    @PersistenceContext EntityManager em;
    private void clear() {
        em.flush();
        em.clear();
    }
    @BeforeEach
    public void init() {
        //공통
        String title = "title";
        String content = "content";
        int volunteerNum = 10;
        String organizationName = "organization";
        float latitude = 3.2F;
        float longitude = 3.2F;
        Timetable timetable = new Timetable(LocalDate.now(), LocalDate.now(), LocalTime.now(), 10);
        Boolean isPublished = true;
        String details = "details";

        //필터링 조건
        VolunteeringCategory category1 = VolunteeringCategory.ADMINSTRATION_ASSISTANCE;
        VolunteeringCategory category2 = VolunteeringCategory.CULTURAL_EVENT;
        VolunteeringCategory category3 = VolunteeringCategory.DISASTER;

        VolunteeringType volunteeringType1 = VolunteeringType.SHORT;
        VolunteeringType volunteeringType2 = VolunteeringType.LONG;

        VolunteerType volunteerType1 = VolunteerType.ALL;
        VolunteerType volunteerType2 = VolunteerType.TEENAGER;
        VolunteerType volunteerType3 = VolunteerType.ADULT;

        Boolean isIssued1 = true;
        Boolean isIssued2 = false;

        String sido1 = "11";
        String sido2 = "22";
        String sido3 = "333";

        String sigungu1 = "1111";
        String sigungu2 = "2222";
        String sigungu3 = "3333";


        for(int i=0;i<5;i++){
            Recruitment create1 = Recruitment.builder()
                    .title(title) .content(content) .volunteeringCategory(category1) .volunteeringType(volunteeringType1) .volunteerType(volunteerType1)
                    .volunteerNum(volunteerNum) .isIssued(isIssued1) .organizationName(organizationName) .sido(sido1) .sigungu(sigungu1)
                    .details(details) .latitude(latitude) .longitude(longitude) .timetable(timetable) .isPublished(isPublished).build();
            recruitmentRepository.save(create1);

            Recruitment create2 = Recruitment.builder()
                    .title(title) .content(content) .volunteeringCategory(category2) .volunteeringType(volunteeringType2) .volunteerType(volunteerType2)
                    .volunteerNum(volunteerNum) .isIssued(isIssued2) .organizationName(organizationName) .sido(sido2) .sigungu(sigungu2)
                    .details(details) .latitude(latitude) .longitude(longitude) .timetable(timetable) .isPublished(isPublished).build();
            recruitmentRepository.save(create2);

            Recruitment create3 = Recruitment.builder()
                    .title(title) .content(content) .volunteeringCategory(category3) .volunteeringType(volunteeringType2) .volunteerType(volunteerType3)
                    .volunteerNum(volunteerNum) .isIssued(isIssued2) .organizationName(organizationName) .sido(sido3) .sigungu(sigungu3)
                    .details(details) .latitude(latitude) .longitude(longitude) .timetable(timetable) .isPublished(isPublished).build();
            recruitmentRepository.save(create3);
        }

        //임시 저장글(1개)
        Recruitment create4 = Recruitment.builder()
                .title(title) .content(content) .volunteeringCategory(category3) .volunteeringType(volunteeringType2) .volunteerType(volunteerType3)
                .volunteerNum(volunteerNum) .isIssued(isIssued2) .organizationName(organizationName) .sido(sido3) .sigungu(sigungu3)
                .details(details) .latitude(latitude) .longitude(longitude) .timetable(timetable) .isPublished(Boolean.FALSE).build();
        recruitmentRepository.save(create4);
        clear();
    }

    @Test
    public void 모집글_전체조회_카테고리_Slice(){
        //given
        List<String> category = Arrays.asList("001","002");
        String sido = null;
        String sigungu = null;
        String volunteeringType = null;
        String volunteerType = null;
        Boolean isIssued = null;

        SearchType searchType = new SearchType(category, sido, sigungu, volunteeringType, volunteerType, isIssued);
        PageRequest page = PageRequest.of(0, 10); //첫페이지 10개

        //when
        Slice<RecruitmentQueryDto> result = recruitmentQueryDtoRepository.findRecruitmentJoinImageBySearchType(page, searchType);

        //then
        Assertions.assertThat(result.getContent().size()).isEqualTo(10);
        Assertions.assertThat(result.hasNext()).isFalse();
        Assertions.assertThat(result.getNumber()).isEqualTo(0);
    }

    @Test
    public void 모집글_전체조회_카테고리_시도_Slice(){
        //given
        List<String> category = Arrays.asList("001");
        String sido = "11";
        String sigungu = null;
        String volunteeringType = null;
        String volunteerType = null;
        Boolean isIssued = null;

        SearchType searchType = new SearchType(category, sido, sigungu, volunteeringType, volunteerType, isIssued);
        PageRequest page = PageRequest.of(0, 5); //첫페이지 5개

        //when
        Slice<RecruitmentQueryDto> result = recruitmentQueryDtoRepository.findRecruitmentJoinImageBySearchType(page, searchType);

        //then
        Assertions.assertThat(result.getContent().size()).isEqualTo(5);
        Assertions.assertThat(result.hasNext()).isFalse();
        Assertions.assertThat(result.getNumber()).isEqualTo(0);
    }

    @Test
    public void 모집글_전체조회_카테고리_시도_시군구_Slice() {
        //given
        List<String> category = Arrays.asList("001");
        String sido = "11";
        String sigungu = "2222";
        String volunteeringType = null;
        String volunteerType = null;
        Boolean isIssued = null;

        SearchType searchType = new SearchType(category, sido, sigungu, volunteeringType, volunteerType, isIssued);
        PageRequest page = PageRequest.of(0, 5); //첫페이지 5개

        //when
        Slice<RecruitmentQueryDto> result = recruitmentQueryDtoRepository.findRecruitmentJoinImageBySearchType(page, searchType);

        //then
        Assertions.assertThat(result.getContent().size()).isEqualTo(0);
        Assertions.assertThat(result.hasNext()).isFalse();
        Assertions.assertThat(result.getNumber()).isEqualTo(0);
    }

    @Test
    public void 모집글_전체조회_카테고리_시도_시군구_봉사타입_Slice(){
        //given
        List<String> category = Arrays.asList("001");
        String sido = "11";
        String sigungu = "1111";
        String volunteeringType = VolunteeringType.SHORT.name();
        String volunteerType = null;
        Boolean isIssued = null;

        SearchType searchType = new SearchType(category, sido, sigungu, volunteeringType, volunteerType, isIssued);
        PageRequest page = PageRequest.of(1, 3); //두번째 페이지 size=3

        //when
        Slice<RecruitmentQueryDto> result = recruitmentQueryDtoRepository.findRecruitmentJoinImageBySearchType(page, searchType);

        //then
        Assertions.assertThat(result.getContent().size()).isEqualTo(2);
        Assertions.assertThat(result.hasNext()).isFalse();
        Assertions.assertThat(result.getNumber()).isEqualTo(1);
    }

    @Test
    public void 모집글_전체조회_카테고리_시도_시군구_봉사타입_봉사자타입_Slice(){
        //given
        List<String> category = Arrays.asList("001");
        String sido = "11";
        String sigungu = "1111";
        String volunteeringType = VolunteeringType.SHORT.name();
        String volunteerType = VolunteerType.TEENAGER.name();
        Boolean isIssued = null;

        SearchType searchType = new SearchType(category, sido, sigungu, volunteeringType, volunteerType, isIssued);
        PageRequest page = PageRequest.of(0, 5);

        //when
        Slice<RecruitmentQueryDto> result = recruitmentQueryDtoRepository.findRecruitmentJoinImageBySearchType(page, searchType);

        //then
        Assertions.assertThat(result.getContent().size()).isEqualTo(0);
        Assertions.assertThat(result.hasNext()).isFalse();
        Assertions.assertThat(result.getNumber()).isEqualTo(0);
    }

    @Test
    public void 모집글_전체조회_카테고리_시도_시군구_봉사타입_봉사자타입_봉사시간여부_Slice(){
        //given
        List<String> category = Arrays.asList("001");
        String sido = "11";
        String sigungu = "1111";
        String volunteeringType = VolunteeringType.SHORT.name();
        String volunteerType = VolunteerType.ALL.name();
        Boolean isIssued = true;

        SearchType searchType = new SearchType(category, sido, sigungu, volunteeringType, volunteerType, isIssued);
        PageRequest page = PageRequest.of(0, 4);

        //when
        Slice<RecruitmentQueryDto> result = recruitmentQueryDtoRepository.findRecruitmentJoinImageBySearchType(page, searchType);

        //then
        Assertions.assertThat(result.getContent().size()).isEqualTo(4);
        Assertions.assertThat(result.hasNext()).isTrue();
        Assertions.assertThat(result.getNumber()).isEqualTo(0);
    }

    @Test
    public void 모집글_전체조회_임시저장글제외_Slice(){
        //givenE

        List<String> category = new ArrayList<>();
        String sido = null;
        String sigungu = null;
        String volunteeringType = null;
        String volunteerType =null;
        Boolean isIssued = null;

        SearchType searchType = new SearchType(category, sido, sigungu, volunteeringType, volunteerType, isIssued);
        PageRequest page = PageRequest.of(0, 15);

        //when
        Slice<RecruitmentQueryDto> result = recruitmentQueryDtoRepository.findRecruitmentJoinImageBySearchType(page, searchType);

        //then
        Assertions.assertThat(result.getContent().size()).isEqualTo(15);
        Assertions.assertThat(result.hasNext()).isFalse();
        Assertions.assertThat(result.getNumber()).isEqualTo(0);
    }

}