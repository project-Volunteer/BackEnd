package project.volunteer.domain.recruitment.api;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.volunteer.domain.image.application.ImageService;
import project.volunteer.domain.recruitment.dao.queryDto.RecruitmentQueryDtoRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.mapper.RecruitmentFacade;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.domain.image.application.dto.ImageParam;
import project.volunteer.domain.recruitment.api.dto.response.*;
import project.volunteer.domain.recruitment.api.dto.request.RecruitmentRequest;
import project.volunteer.domain.recruitment.application.RecruitmentDtoService;
import project.volunteer.domain.recruitment.application.RecruitmentService;
import project.volunteer.domain.recruitment.application.dto.RecruitmentDetails;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.application.dto.RecruitmentParam;
import project.volunteer.domain.recruitment.dao.queryDto.dto.RecruitmentCond;
import project.volunteer.domain.recruitment.application.RepeatPeriodService;
import project.volunteer.domain.recruitment.application.dto.RepeatPeriodParam;
import project.volunteer.domain.sehedule.application.ScheduleService;
import project.volunteer.domain.sehedule.application.dto.ScheduleParamReg;
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

    private final RecruitmentService recruitmentService;
    private final RecruitmentDtoService recruitmentDtoService;
    private final RecruitmentQueryDtoRepository recruitmentQueryDtoRepository;
    private final ImageService imageService;
    private final RecruitmentFacade recruitmentFacade;

    @PostMapping(value = "/recruitment", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Map<String,Object>> recruitmentAdd(@ModelAttribute @Valid RecruitmentRequest form) {
        Long recruitmentNo = recruitmentFacade.registerVolunteerPost(SecurityUtil.getLoginUserNo(), form);

        Map<String, Object> result = new HashMap<>();
        result.put("no", recruitmentNo);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(result);
    }

    @GetMapping("/recruitment")
    public ResponseEntity<RecruitmentListResponse> recruitmentList(@PageableDefault(size = 6) Pageable pageable,
                                                                   @RequestParam(required = false) List<String> volunteering_category,
                                                                   @RequestParam(required = false) String sido,
                                                                   @RequestParam(required = false) String sigungu,
                                                                   @RequestParam(required = false) String volunteering_type,
                                                                   @RequestParam(required = false) String volunteer_type,
                                                                   @RequestParam(required = false) Boolean is_issued) {

        return ResponseEntity.ok(recruitmentDtoService.findRecruitmentDtos(pageable,
                new RecruitmentCond(volunteering_category, sido, sigungu, volunteering_type, volunteer_type, is_issued)));
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

        //response
        Map<String, Object> result = new HashMap<>();
        result.put("totalCnt", recruitmentsCount);
        return ResponseEntity.ok(result);
    }

//    @LogExecutionTime
    @GetMapping("/recruitment/{no}")
    public ResponseEntity<RecruitmentDetails> recruitmentDetails(@PathVariable Long no){

        RecruitmentDetails dto = recruitmentDtoService.findRecruitmentDto(no);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/recruitment/{no}/status")
    public ResponseEntity<Map<String,Object>> teamStateDetails(@PathVariable Long no){
        String status = recruitmentDtoService.findRecruitmentTeamStatus(no, SecurityUtil.getLoginUserNo());

        //response
        Map<String,Object> result = new HashMap<>();
        result.put("status", status);
        return ResponseEntity.ok(result);
    }

    @OrganizationAuth(auth = Auth.ORGANIZATION_ADMIN)
    @DeleteMapping("/recruitment/{recruitmentNo}")
    public ResponseEntity recruitmentDelete(@PathVariable("recruitmentNo") Long no) {
        recruitmentFacade.deleteVolunteerPost(no);
        return new ResponseEntity(HttpStatus.OK);
    }
}
