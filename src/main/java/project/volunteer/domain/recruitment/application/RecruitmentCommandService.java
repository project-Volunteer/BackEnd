package project.volunteer.domain.recruitment.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.image.application.ImageService;
import project.volunteer.domain.image.application.dto.ImageParam;
import project.volunteer.domain.recruitment.domain.repeatPeriod.validator.PeriodValidation;
import project.volunteer.domain.recruitment.domain.repeatPeriod.validator.PeriodValidationProvider;
import project.volunteer.domain.recruitment.repository.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.application.dto.command.RecruitmentCreateCommand;
import project.volunteer.domain.sehedule.application.ScheduleCommandUseCase;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RecruitmentCommandService implements RecruitmentCommandUseCase {
    private final RecruitmentRepository recruitmentRepository;
    private final ScheduleCommandUseCase scheduleCommandUseCase;
    private final ImageService imageService;
    private final PeriodValidationProvider periodValidationProvider;

    public Long addRecruitment(final User writer, final RecruitmentCreateCommand command) {
        final Recruitment recruitment = command.toRecruitmentDomain(writer);
        recruitmentRepository.save(recruitment);

        if (recruitment.isRegularRecruitment()) {
            PeriodValidation periodValidator = periodValidationProvider.getValidator(
                    command.getRepeatPeriodCommand().getPeriod());
            recruitment.setRepeatPeriods(command.toRepeatPeriodDomains(periodValidator));
            scheduleCommandUseCase.addRegularSchedule(recruitment, command.toRegularScheduleCreateCommand());
        }

        if (command.isUploadImage()) {
            final ImageParam imageCreateCommand = command.toUploadImageCreateCommand(recruitment.getRecruitmentNo());
            imageService.addImage(imageCreateCommand);
        }

        return recruitment.getRecruitmentNo();
    }

    @Override
    public void deleteRecruitment(final Long recruitmentNo) {
        Recruitment recruitment = recruitmentRepository.findNotDeletedRecruitment(recruitmentNo).orElseThrow(
                () -> new BusinessException(ErrorCode.NOT_EXIST_RECRUITMENT,
                        String.format("Delete Recruitment ID = [%d]", recruitmentNo)));
        recruitment.delete();
    }

}
