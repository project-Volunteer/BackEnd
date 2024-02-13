package project.volunteer.domain.recruitment.api;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.volunteer.domain.recruitment.api.dto.response.RecruitmentSaveResponse;
import project.volunteer.domain.recruitment.api.dto.response.StatusResponse;
import project.volunteer.domain.recruitment.application.dto.query.RecruitmentCountResult;
import project.volunteer.domain.recruitment.application.dto.query.detail.RecruitmentDetailSearchResult;
import project.volunteer.domain.recruitment.application.dto.query.list.RecruitmentListSearchResult;
import project.volunteer.domain.recruitment.application.RecruitmentFacade;
import project.volunteer.domain.recruitment.api.dto.request.RecruitmentRequest;
import project.volunteer.domain.recruitment.application.RecruitmentQueryUseCase;
import project.volunteer.domain.recruitment.application.dto.query.list.RecruitmentSearchCond;
import project.volunteer.global.Interceptor.OrganizationAuth;
import project.volunteer.global.common.dto.StateResult;
import project.volunteer.global.util.SecurityUtil;

import javax.validation.Valid;
import java.util.List;

import static project.volunteer.global.Interceptor.OrganizationAuth.*;

@RestController
@RequiredArgsConstructor
public class RecruitmentController {
    private final RecruitmentFacade recruitmentFacade;
    private final RecruitmentQueryUseCase recruitmentQueryService;

    @PostMapping(value = "/recruitment", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<RecruitmentSaveResponse> recruitmentAdd(@ModelAttribute @Valid RecruitmentRequest request) {
        Long recruitmentNo = recruitmentFacade.registerRecruitment(SecurityUtil.getLoginUserNo(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RecruitmentSaveResponse(recruitmentNo));
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
                RecruitmentSearchCond.of(volunteering_category, sido, sigungu, volunteering_type, volunteer_type,
                        is_issued)));
    }

    @GetMapping("/recruitment/search")
    public ResponseEntity<RecruitmentListSearchResult> recruitmentListByKeyWord(
            @PageableDefault(size = 6) Pageable pageable,
            @RequestParam String keyword) {

        return ResponseEntity.ok(recruitmentQueryService.searchRecruitmentList(pageable, keyword));
    }

    @GetMapping("/recruitment/count")
    public ResponseEntity<RecruitmentCountResult> recruitmentListCount(
            @RequestParam(required = false) List<String> volunteering_category,
            @RequestParam(required = false) String sido,
            @RequestParam(required = false) String sigungu,
            @RequestParam(required = false) String volunteering_type,
            @RequestParam(required = false) String volunteer_type,
            @RequestParam(required = false) Boolean is_issued) {
        RecruitmentCountResult result = recruitmentQueryService.searchRecruitmentCount(
                RecruitmentSearchCond.of(volunteering_category, sido, sigungu, volunteering_type, volunteer_type,
                        is_issued));

        return ResponseEntity.ok(result);
    }

    //    @LogExecutionTime
    @GetMapping("/recruitment/{no}")
    public ResponseEntity<RecruitmentDetailSearchResult> recruitmentDetails(@PathVariable Long no) {
        RecruitmentDetailSearchResult result = recruitmentQueryService.searchRecruitmentDetail(no);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/recruitment/{no}/status")
    public ResponseEntity<StatusResponse> teamStateDetails(@PathVariable Long no) {
        StateResult status = recruitmentFacade.findState(no, SecurityUtil.getLoginUserNo());
        return ResponseEntity.ok(new StatusResponse(status.getId()));
    }

    @OrganizationAuth(auth = Auth.ORGANIZATION_ADMIN)
    @DeleteMapping("/recruitment/{recruitmentNo}")
    public ResponseEntity<Void> recruitmentDelete(@PathVariable("recruitmentNo") Long no) {
        recruitmentFacade.deleteRecruitment(no);
        return ResponseEntity.status(HttpStatus.OK)
                .build();
    }

}
