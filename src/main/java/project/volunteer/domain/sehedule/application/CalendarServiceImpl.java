package project.volunteer.domain.sehedule.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.participation.dao.ParticipantRepository;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.sehedule.dao.ScheduleRepository;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService{

    private final ScheduleRepository scheduleRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final ParticipantRepository participantRepository;

    @Override
    public List<Schedule> findCalendarSchedules(Long recruitmentNo, Long loginUserNo, LocalDate startDay, LocalDate endDay) {

        //모집글 검증
        Recruitment findRecruitment = isValidRecruitment(recruitmentNo);

        //봉사 팀원 검증
        isRecruitmentTeamMember(findRecruitment, loginUserNo);

        //기간 내에 일정 리스트 조회
        return scheduleRepository.findScheduleWithinPeriod(recruitmentNo, startDay, endDay);
    }


    private Recruitment isValidRecruitment(Long recruitmentNo){
        return recruitmentRepository.findPublishedByRecruitmentNo(recruitmentNo)
                .orElseThrow(() ->  new BusinessException(ErrorCode.NOT_EXIST_RECRUITMENT, String.format("Recruitment No = [%d]", recruitmentNo)));
    }
    private void isRecruitmentTeamMember(Recruitment recruitment, Long loginUserNo){
        if(!participantRepository.existRecruitmentTeamMember(recruitment.getRecruitmentNo(), loginUserNo)){
            throw new BusinessException(ErrorCode.FORBIDDEN_RECRUITMENT_TEAM,
                    String.format("RecruitmentNo = [%d], UserNo = [%d]", recruitment.getRecruitmentNo(), loginUserNo));
        }
    }
}
