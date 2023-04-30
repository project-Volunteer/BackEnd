package project.volunteer.domain.recruitment.api;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.query.Param;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import project.volunteer.domain.image.application.ImageService;
import project.volunteer.domain.image.domain.RealWorkCode;
import project.volunteer.domain.image.application.dto.ImageParam;
import project.volunteer.domain.recruitment.api.dto.response.*;
import project.volunteer.domain.recruitment.api.dto.request.RecruitmentRequest;
import project.volunteer.domain.recruitment.application.RecruitmentDtoService;
import project.volunteer.domain.recruitment.application.RecruitmentService;
import project.volunteer.domain.recruitment.application.dto.RecruitmentDetails;
import project.volunteer.domain.recruitment.dao.queryDto.RecruitmentQueryDtoRepository;
import project.volunteer.domain.recruitment.dao.queryDto.dto.RecruitmentListQuery;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.application.dto.RecruitmentParam;
import project.volunteer.domain.recruitment.dao.queryDto.dto.RecruitmentCond;
import project.volunteer.domain.repeatPeriod.application.RepeatPeriodService;
import project.volunteer.domain.repeatPeriod.application.dto.RepeatPeriodParam;
import project.volunteer.domain.sehedule.application.ScheduleService;
import project.volunteer.domain.sehedule.application.dto.ScheduleParamReg;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class RecruitmentController {

    private final RecruitmentService recruitmentService;
    private final RecruitmentDtoService recruitmentDtoService;
    private final RepeatPeriodService repeatPeriodService;
    private final ScheduleService scheduleService;
    private final ImageService imageService;
    private final RecruitmentQueryDtoRepository recruitmentQueryDtoRepository;

    @PostMapping(value = "/recruitment", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<RecruitmentSaveResponse> recruitmentAdd(@Validated  @ModelAttribute RecruitmentRequest form) {

        //모집글 정보 저장
        Long recruitmentNo = recruitmentService.addRecruitment(new RecruitmentParam(form));

        //정기일 경우
        if(form.getVolunteeringType().toUpperCase().equals(VolunteeringType.REG.name())) {
            RepeatPeriodParam periodParam = new RepeatPeriodParam(form.getPeriod(), form.getWeek(), form.getDays());
            //반복 주기 저장
            repeatPeriodService.addRepeatPeriod(recruitmentNo, periodParam);

            //스케줄 자동 할당
            scheduleService.addRegSchedule(recruitmentNo,
                    new ScheduleParamReg(form.getStartDay(), form.getEndDay(), form.getHourFormat(), form.getStartTime(), form.getProgressTime(),
                            form.getOrganizationName(), form.getAddress().getSido(), form.getAddress().getSigungu(), form.getAddress().getDetails(),
                            form.getContent(), periodParam));
        }

        //이미지 저장
        imageService.addImage(
                new ImageParam(RealWorkCode.RECRUITMENT, recruitmentNo, form.getPicture()));

        return ResponseEntity.status(HttpStatus.CREATED)
                        .body(new RecruitmentSaveResponse("success save recruitment",recruitmentNo));
    }

    @GetMapping("/recruitment")
    public ResponseEntity<RecruitmentListResponse> recruitmentList(@PageableDefault(size = 6) Pageable pageable,
                                                                   @RequestParam(required = false) List<String> volunteering_category,
                                                                   @RequestParam(required = false) String sido,
                                                                   @RequestParam(required = false) String sigungu,
                                                                   @RequestParam(required = false) String volunteering_type,
                                                                   @RequestParam(required = false) String volunteer_type,
                                                                   @RequestParam(required = false) Boolean is_issued) {

        Slice<RecruitmentListQuery> result = recruitmentQueryDtoRepository.findRecruitmentDtos(pageable,
                new RecruitmentCond(volunteering_category, sido, sigungu, volunteering_type, volunteer_type, is_issued));

        //response DTO 변환
        List<RecruitmentList> dtos = result.getContent().stream().map(dto -> new RecruitmentList(dto)).collect(Collectors.toList());
        return ResponseEntity.ok(new RecruitmentListResponse("success search recruitment list",
                        dtos, result.isLast(), (dtos.isEmpty())?null:(dtos.get(dtos.size()-1).getNo())));
    }

    @GetMapping("/recruitment/count")
    public ResponseEntity<RecruitmentCountResponse> recruitmentListCount(@RequestParam(required = false) List<String> volunteering_category,
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
        return ResponseEntity.ok(new RecruitmentCountResponse("success count recruitment list", recruitmentsCount));
    }

    @GetMapping("/recruitment/{no}")
    public ResponseEntity<RecruitmentDetailsResponse> recruitmentDetails(@PathVariable Long no){

        RecruitmentDetails dto = recruitmentDtoService.findRecruitment(no);
        return ResponseEntity.ok(new RecruitmentDetailsResponse("success search recruitment details", dto));
    }

    @DeleteMapping("/recruitment/{no}")
    public ResponseEntity recruitmentDelete(@PathVariable Long no) {

        //반복 주기 삭제 -> 정기일때만 실행시키기??!
        repeatPeriodService.deleteRepeatPeriod(no);

        //봉사 참여자 리스트 삭제 필요

        //스케줄 삭제 필요

        //스케줄 참여자 리스트 삭제 필요

        //공지사항 삭제 필요

        //공지사항 확인 리스트 삭제 필요

        //이미지 삭제
        imageService.deleteImage(RealWorkCode.RECRUITMENT, no);

        //모집글 삭제
        recruitmentService.deleteRecruitment(no);

        return new ResponseEntity(HttpStatus.OK);
    }
}
