package project.volunteer.domain.recruitment.api;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.volunteer.domain.recruitment.application.dto.query.RecruitmentCountResult;
import project.volunteer.domain.recruitment.application.dto.query.detail.RecruitmentDetailSearchResult;
import project.volunteer.domain.recruitment.application.dto.query.list.RecruitmentListSearchResult;
import project.volunteer.domain.recruitment.application.RecruitmentFacade;
import project.volunteer.domain.recruitment.api.dto.request.RecruitmentRequest;
import project.volunteer.domain.recruitment.application.RecruitmentQueryUseCase;
import project.volunteer.domain.recruitment.application.dto.query.list.RecruitmentSearchCond;
import project.volunteer.global.Interceptor.OrganizationAuth;
import project.volunteer.global.util.SecurityUtil;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static project.volunteer.global.Interceptor.OrganizationAuth.*;

@RestController
@RequiredArgsConstructor
public class RecruitmentController {
    private final RecruitmentFacade recruitmentFacade;
    private final RecruitmentQueryUseCase recruitmentQueryService;

    @PostMapping(value = "/recruitment", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Map<String,Object>> recruitmentAdd(@ModelAttribute @Valid RecruitmentRequest form) {
        Long recruitmentNo = recruitmentFacade.registerRecruitment(SecurityUtil.getLoginUserNo(), form);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(getSingleResponseDto("no", recruitmentNo));
    }

    @GetMapping("/recruitment")
    public ResponseEntity<RecruitmentListSearchResult> recruitmentList(@PageableDefault(size = 6) Pageable pageable,
                                                                       @RequestParam(required = false) List<String> volunteering_category,
                                                                       @RequestParam(required = false) String sido,
                                                                       @RequestParam(required = false) String sigungu,
                                                                       @RequestParam(required = false) String volunteering_type,
                                                                       @RequestParam(required = false) String volunteer_type,
                                                                       @RequestParam(required = false) Boolean is_issued) {

        return ResponseEntity.ok(recruitmentQueryService.searchRecruitmentList(pageable,
                RecruitmentSearchCond.of(volunteering_category, sido, sigungu, volunteering_type, volunteer_type, is_issued)));
    }

    @GetMapping("/recruitment/search")
    public ResponseEntity<RecruitmentListSearchResult> recruitmentListByKeyWord(@PageableDefault(size = 6) Pageable pageable,
                                                                            @RequestParam String keyword){

        return ResponseEntity.ok(recruitmentQueryService.searchRecruitmentList(pageable, keyword));
    }

    @GetMapping("/recruitment/count")
    public ResponseEntity<RecruitmentCountResult> recruitmentListCount(@RequestParam(required = false) List<String> volunteering_category,
                                                                         @RequestParam(required = false) String sido,
                                                                         @RequestParam(required = false) String sigungu,
                                                                         @RequestParam(required = false) String volunteering_type,
                                                                         @RequestParam(required = false) String volunteer_type,
                                                                         @RequestParam(required = false) Boolean is_issued){
        RecruitmentCountResult result = recruitmentQueryService.searchRecruitmentCount(
                RecruitmentSearchCond.of(volunteering_category, sido, sigungu, volunteering_type, volunteer_type, is_issued));

        return ResponseEntity.ok(result);
    }

//    @LogExecutionTime
    @GetMapping("/recruitment/{no}")
    public ResponseEntity<RecruitmentDetailSearchResult> recruitmentDetails(@PathVariable Long no){
        RecruitmentDetailSearchResult result = recruitmentQueryService.searchRecruitmentDetail(no);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/recruitment/{no}/status")
    public ResponseEntity<Map<String,Object>> teamStateDetails(@PathVariable Long no){
        String status = recruitmentFacade.findVolunteerPostParticipationState(no, SecurityUtil.getLoginUserNo());
        return ResponseEntity.ok(getSingleResponseDto("status", status));
    }

    @OrganizationAuth(auth = Auth.ORGANIZATION_ADMIN)
    @DeleteMapping("/recruitment/{recruitmentNo}")
    public ResponseEntity recruitmentDelete(@PathVariable("recruitmentNo") Long no) {
        recruitmentFacade.deleteRecruitment(no);
        return new ResponseEntity(HttpStatus.OK);
    }

    private Map<String, Object> getSingleResponseDto(String field, Object value){
        Map<String,Object> result = new HashMap<>();
        result.put(field, value);
        return result;
    }
}
