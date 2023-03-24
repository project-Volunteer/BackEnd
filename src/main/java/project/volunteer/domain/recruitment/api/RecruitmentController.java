package project.volunteer.domain.recruitment.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import project.volunteer.domain.recruitment.api.form.SaveRecruitForm;
import project.volunteer.domain.recruitment.application.RecruitmentService;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.dto.SaveRecruitDto;
import project.volunteer.domain.repeatPeriod.application.RepeatPeriodService;
import project.volunteer.domain.repeatPeriod.dto.SaveRepeatPeriodDto;

@RestController
@RequiredArgsConstructor
public class RecruitmentController {

    private RecruitmentService recruitmentService;
    private RepeatPeriodService repeatPeriodService;

    @PostMapping("/write")
    public void save(@ModelAttribute SaveRecruitForm form) {

        Long recruitmentNo = recruitmentService.addRecruitment(new SaveRecruitDto(form));

        if(form.getVolunteeringType().equals(VolunteeringType.LONG.name())) //장기일 경우
            repeatPeriodService.addRepeatPeriod(recruitmentNo,
                    new SaveRepeatPeriodDto(form.getPeriod(),form.getWeek(), form.getDays()));



    }


}
