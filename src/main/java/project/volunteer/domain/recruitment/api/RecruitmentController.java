package project.volunteer.domain.recruitment.api;

import com.amazonaws.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import project.volunteer.domain.image.application.ImageService;
import project.volunteer.domain.image.domain.RealWorkCode;
import project.volunteer.domain.image.application.dto.SaveImageDto;
import project.volunteer.domain.recruitment.api.dto.response.*;
import project.volunteer.domain.recruitment.api.dto.request.SaveRecruitForm;
import project.volunteer.domain.recruitment.application.RecruitmentDtoService;
import project.volunteer.domain.recruitment.application.RecruitmentService;
import project.volunteer.domain.recruitment.application.dto.RecruitmentDto;
import project.volunteer.domain.recruitment.dao.queryDto.RecruitmentQueryDtoRepository;
import project.volunteer.domain.recruitment.dao.queryDto.dto.RecruitmentQueryDto;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.application.dto.SaveRecruitDto;
import project.volunteer.domain.recruitment.dao.queryDto.dto.SearchType;
import project.volunteer.domain.repeatPeriod.application.RepeatPeriodService;
import project.volunteer.domain.repeatPeriod.application.dto.SaveRepeatPeriodDto;
import project.volunteer.domain.sehedule.application.ScheduleService;
import project.volunteer.domain.sehedule.application.dto.SaveScheduleDto;

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
    public ResponseEntity<SaveRecruitResponse> recruitmentAdd(@Validated  @ModelAttribute SaveRecruitForm form) {

        //모집글 정보 저장
        Long recruitmentNo = recruitmentService.addRecruitment(new SaveRecruitDto(form));

        //장기일 경우, 반복 주기 저장
        if(form.getVolunteeringType().toUpperCase().equals(VolunteeringType.LONG.name())) {
            repeatPeriodService.addRepeatPeriod(recruitmentNo,
                    new SaveRepeatPeriodDto(form.getPeriod(), form.getWeek(), form.getDays()));
        }
        //단기일 경우, 스케쥴 자동 할당
        else {
            scheduleService.addSchedule(recruitmentNo,
                    new SaveScheduleDto(form.getStartDay(), form.getEndDay(), form.getStartTime(), form.getProgressTime(),
                            form.getOrganizationName(), form.getAddress().getSido(), form.getAddress().getSigungu(),form.getAddress().getDetails(),
                            null));
        }
        //이미지 저장
        imageService.addImage(
                new SaveImageDto(RealWorkCode.RECRUITMENT, recruitmentNo, form.getPicture()));

        return ResponseEntity.ok(new SaveRecruitResponse("success save recruitment",recruitmentNo));
    }

    @GetMapping("/recruitment")
    public ResponseEntity<RecruitmentListResponse> recruitmentList(@PageableDefault(size = 6) Pageable pageable,
                                                                   @RequestParam(required = false) List<String> volunteering_category,
                                                                   @RequestParam(required = false) String sido,
                                                                   @RequestParam(required = false) String sigungu,
                                                                   @RequestParam(required = false) String volunteering_type,
                                                                   @RequestParam(required = false) String volunteer_type,
                                                                   @RequestParam(required = false) Boolean is_issued) {

        Slice<RecruitmentQueryDto> result = recruitmentQueryDtoRepository.findRecruitmentDtos(pageable,
                new SearchType(volunteering_category, sido, sigungu, volunteering_type, volunteer_type, is_issued));
        return ResponseEntity.ok(setRecruitmentListResponse(result));
    }

    private RecruitmentListResponse setRecruitmentListResponse(Slice<RecruitmentQueryDto> result ) {
        List<RecruitmentListDto> dtos = result.getContent().stream().map(dto -> new RecruitmentListDto(dto)).collect(Collectors.toList());
        return new RecruitmentListResponse("success search recruitment list",
                dtos, result.isLast(), (dtos.isEmpty())?null:(dtos.get(dtos.size()-1).getNo()));
    }

    @GetMapping("/recruitment/{no}")
    public ResponseEntity<RecruitmentResponse> recruitmentDetails(@PathVariable Long no){

        RecruitmentDto dto = recruitmentDtoService.findRecruitment(no);
        return ResponseEntity.ok(new RecruitmentResponse("success search recruitment details", dto));
    }

}
