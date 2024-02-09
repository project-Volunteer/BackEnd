package project.volunteer.domain.recruitment.api;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.volunteer.domain.recruitment.repository.queryDto.RecruitmentQueryDtoRepository;
import project.volunteer.domain.recruitment.mapper.RecruitmentFacade;
import project.volunteer.domain.recruitment.api.dto.response.*;
import project.volunteer.domain.recruitment.api.dto.request.RecruitmentRequest;
import project.volunteer.domain.recruitment.application.RecruitmentQueryUseCase;
import project.volunteer.domain.recruitment.repository.queryDto.dto.RecruitmentCond;
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
    private final RecruitmentQueryUseCase recruitmentDtoService;
    private final RecruitmentQueryDtoRepository recruitmentQueryDtoRepository;
    private final RecruitmentFacade recruitmentFacade;

    @PostMapping(value = "/recruitment", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Map<String,Object>> recruitmentAdd(@ModelAttribute @Valid RecruitmentRequest form) {
        Long recruitmentNo = recruitmentFacade.registerRecruitment(SecurityUtil.getLoginUserNo(), form);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(getSingleResponseDto("no", recruitmentNo));
    }

    @GetMapping("/recruitment")
    public ResponseEntity<RecruitmentListResponse> recruitmentList(@PageableDefault(size = 6) Pageable pageable,
                                                                   @RequestParam(required = false) List<String> volunteering_category,
                                                                   @RequestParam(required = false) String sido,
                                                                   @RequestParam(required = false) String sigungu,
                                                                   @RequestParam(required = false) String volunteering_type,
                                                                   @RequestParam(required = false) String volunteer_type,
                                                                   @RequestParam(required = false) Boolean is_issued) {

        return ResponseEntity.ok(recruitmentDtoService.findSliceRecruitmentDtosByRecruitmentCond(pageable,
                new RecruitmentCond(volunteering_category, sido, sigungu, volunteering_type, volunteer_type, is_issued)));
    }

    @GetMapping("/recruitment/search")
    public ResponseEntity<RecruitmentListResponse> recruitmentListByKeyWord(@PageableDefault(size = 6) Pageable pageable,
                                                                            @RequestParam String keyword){

        return ResponseEntity.ok(recruitmentDtoService.findSliceRecruitmentDtosByKeyWord(pageable, keyword));
    }

    @GetMapping("/recruitment/count")
    public ResponseEntity<Map<String,Object>> recruitmentListCount(@RequestParam(required = false) List<String> volunteering_category,
                                                                         @RequestParam(required = false) String sido,
                                                                         @RequestParam(required = false) String sigungu,
                                                                         @RequestParam(required = false) String volunteering_type,
                                                                         @RequestParam(required = false) String volunteer_type,
                                                                         @RequestParam(required = false) Boolean is_issued){
        Long recruitmentsCount = recruitmentQueryDtoRepository.findRecruitmentCountBySearchType(
                RecruitmentCond.builder()
                        .category(volunteering_category)
                        .sido(sido)
                        .sigungu(sigungu)
                        .volunteeringType(volunteering_type)
                        .volunteerType(volunteer_type)
                        .isIssued(is_issued)
                        .build());

        return ResponseEntity.ok(getSingleResponseDto("totalCnt", recruitmentsCount));
    }

//    @LogExecutionTime
    @GetMapping("/recruitment/{no}")
    public ResponseEntity<RecruitmentDetailsResponse> recruitmentDetails(@PathVariable Long no){
        RecruitmentDetailsResponse dto = recruitmentFacade.findVolunteerPostDetails(no);
        return ResponseEntity.ok(dto);
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
