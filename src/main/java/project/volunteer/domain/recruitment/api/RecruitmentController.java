package project.volunteer.domain.recruitment.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import project.volunteer.domain.image.application.ImageService;
import project.volunteer.domain.image.domain.RealWorkCode;
import project.volunteer.domain.image.dto.SaveImageDto;
import project.volunteer.domain.recruitment.api.form.SaveRecruitForm;
import project.volunteer.domain.recruitment.application.RecruitmentService;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.dto.SaveRecruitDto;
import project.volunteer.domain.repeatPeriod.application.RepeatPeriodService;
import project.volunteer.domain.repeatPeriod.dto.SaveRepeatPeriodDto;
import project.volunteer.global.common.response.DataResponse;

@RestController
@RequiredArgsConstructor
public class RecruitmentController {

    private final RecruitmentService recruitmentService;
    private final RepeatPeriodService repeatPeriodService;
    private final ImageService imageService;

    @PostMapping(value = "/recruitment", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<DataResponse> save(@ModelAttribute SaveRecruitForm form) {

        //모집글 정보 저장
        Long recruitmentNo = recruitmentService.addRecruitment(new SaveRecruitDto(form));

        //장기일 경우, 반복 주기 저장
        if(form.getVolunteeringType().toUpperCase().equals(VolunteeringType.LONG.name())) {
            repeatPeriodService.addRepeatPeriod(recruitmentNo,
                    new SaveRepeatPeriodDto(form.getPeriod(), form.getWeek(), form.getDays()));
        }

        //이미지 저장
        imageService.addImage(
                new SaveImageDto(RealWorkCode.RECRUITMENT, recruitmentNo, form.getPicture()));

        return ResponseEntity.ok(new DataResponse<>(recruitmentNo, "모집글 등록이 완료되었습니다."));
    }

}
