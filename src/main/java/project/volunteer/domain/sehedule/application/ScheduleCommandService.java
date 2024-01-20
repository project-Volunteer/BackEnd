package project.volunteer.domain.sehedule.application;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.Day;
import project.volunteer.domain.recruitment.domain.Period;
import project.volunteer.domain.recruitment.domain.Week;
import project.volunteer.domain.scheduleParticipation.dao.ScheduleParticipationRepository;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;
import project.volunteer.domain.sehedule.application.dto.ScheduleParamReg;
import project.volunteer.domain.sehedule.dao.ScheduleRepository;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.domain.sehedule.application.dto.ScheduleCreateCommand;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.common.component.Timetable;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;
import project.volunteer.global.util.DateUtil;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ScheduleCommandService implements ScheduleCommandUseCase {
    private final ScheduleRepository scheduleRepository;
    private final ScheduleParticipationRepository scheduleParticipationRepository;

    @Override
    public Long addSchedule(Recruitment recruitment, ScheduleCreateCommand command) {
        Schedule schedule = command.toDomain(recruitment);
        return scheduleRepository.save(schedule).getScheduleNo();
    }

    @Override
    public List<Long> addRegSchedule(Recruitment recruitment, ScheduleParamReg dto) {
        List<LocalDate> scheduleDate = (dto.getRepeatPeriodParam().getPeriod().equals(Period.MONTH))?
                (makeDatesOfRegMonth(dto.getTimetable().getStartDay(), dto.getTimetable().getEndDay(),
                dto.getRepeatPeriodParam().getWeek(), dto.getRepeatPeriodParam().getDays())):
                (makeDatsOfRegWeek(dto.getTimetable().getStartDay(), dto.getTimetable().getEndDay(), dto.getRepeatPeriodParam().getDays()));

        //스케줄 등록
        return scheduleDate.stream()
                .map(date -> {
                    Timetable timetable = Timetable.createTimetable(date, date, dto.getTimetable().getHourFormat(),
                            dto.getTimetable().getStartTime(), dto.getTimetable().getProgressTime());

                    Address address =
                            Address.createAddress(dto.getAddress().getSido(), dto.getAddress().getSigungu(), dto.getAddress().getDetails(), dto.getAddress().getFullName());

                    Schedule schedule =
                            Schedule.create(timetable, dto.getContent(), dto.getOrganizationName(), address, dto.getVolunteerNum());
                    schedule.setRecruitment(recruitment);
                    return schedule;
                })
                .map(s -> {
                    Schedule saveSchedule = scheduleRepository.save(s);
                    return saveSchedule.getScheduleNo();
                })
                .collect(Collectors.toList());
    }

    @Override
    public Schedule editSchedule(Long scheduleNo, Recruitment recruitment, ScheduleCreateCommand dto) {
        //일정 검증
        Schedule findSchedule = validAndGetSchedule(scheduleNo);

        //수정할 참여 인원수는 현재 일정에 참여중인 인원수보다 적을 수 없음.
        if(findSchedule.getCurrentVolunteerNum() > dto.getMaxParticipationNum()){
            throw new BusinessException(ErrorCode.INSUFFICIENT_CAPACITY_PARTICIPANT,
                    String.format("ScheduleNo = [%d], activeVolunteerNum = [%d], editVolunteerNum = [%d]",
                            scheduleNo, findSchedule.getCurrentVolunteerNum(), dto.getMaxParticipationNum()));
        }

        //일정 참여가능 최대 수는 봉사 팀원 가능 인원보다 많을 수 없음.
        if(recruitment.getVolunteerNum() < dto.getMaxParticipationNum()){
            throw new BusinessException(ErrorCode.EXCEED_PARTICIPATION_NUM_THAN_RECRUITMENT_PARTICIPATION_NUM,
                    String.format("ScheduleNo = [%d], Recruitment VolunteerNum = [%d], editVolunteerNum = [%d]",
                            scheduleNo, recruitment.getVolunteerNum(), dto.getMaxParticipationNum()));
        }

        //일정 정보 수정
        findSchedule.changeSchedule(dto.getTimetable(), dto.getContent(), dto.getOrganizationName(), dto.getAddress(), dto.getMaxParticipationNum());
        return findSchedule;
    }

    @Override
    public void deleteSchedule(Long scheduleNo) {
        //일정 검증
        Schedule findSchedule = validAndGetSchedule(scheduleNo);

        //삭제 플래그 처리 및 연관관계 끊기
        findSchedule.delete();
        findSchedule.removeRecruitment();
    }

    @Override
    public void deleteAllSchedule(Long recruitmentNo) {
        scheduleRepository.findByRecruitment_RecruitmentNo(recruitmentNo)
                .forEach(s -> {
                    s.delete();
                    s.removeRecruitment();
                });
    }

    //TODO: 단일 쿼리로 리펙토링 필요
    //TODO: 배치 스케줄링 메서드
    //TODO: 리팩토링 해서 ScheduleParticipationRepository와 의존관계 없애기
    @Override
    public void scheduleParticipantStateUpdateProcess() {
        //완료된 일정 찾기
        List<Schedule> completedSchedules = scheduleRepository.findCompletedSchedule();

        for(Schedule schedule : completedSchedules){
            List<ScheduleParticipation> findSps = scheduleParticipationRepository.findBySchedule_ScheduleNoAndState(schedule.getScheduleNo(), ParticipantState.PARTICIPATING);
            for(ScheduleParticipation sp : findSps){
                //일정 참여 완료 미승인 상태로 업데이트
                sp.updateState(ParticipantState.PARTICIPATION_COMPLETE_UNAPPROVED);
            }
        }
    }

    //반복 주기가 week 인 일정 날짜 생성
   private List<LocalDate> makeDatsOfRegWeek(LocalDate startDay, LocalDate endDay, List<Day> days){

       //초기 날짜 세팅
       List<LocalDate> init = days.stream()
               .map(day -> DateUtil.findNearestDayOfWeek(startDay, day))
               .collect(Collectors.toList());
       List<LocalDate> newSchedule = new ArrayList<>();

       //마감 날짜까지 반복
       for(int index=0; index<init.size(); index++){
           LocalDate startScheduleDate = init.get(index);
           for(LocalDate start=startScheduleDate; DateUtil.isBefore(start, endDay); start=DateUtil.nextWeek(start)){
               //스케줄 예정 날짜로 추가
               LocalDate newScheduleDate = LocalDate.of(start.getYear(), start.getMonthValue(), start.getDayOfMonth());
               newSchedule.add(newScheduleDate);
           }
       }
       return newSchedule;
   }

   //반복주기가 month 인 일정 날짜 생성
   private List<LocalDate> makeDatesOfRegMonth(LocalDate startDay, LocalDate endDay, Week week, List<Day> days){
        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        class DateSet {
            LocalDate localDate;
            Integer dayOfWeekValue; //요일 값
        }
        //초기 날짜 세팅(연도 넘어가는 경우 포함해서)
        List<DateSet> init = new ArrayList<>();
        for(int i=0; i<=((endDay.getYear()*12+endDay.getMonthValue())-(startDay.getYear()*12+startDay.getMonthValue())); i++) {
            for (Day day : days) {
                init.add(new DateSet(startDay.plusMonths(i), day.getValue()));
            }
        }
        //스케줄로 등록될 날짜 생성
        List<LocalDate> scheduleDate = init.stream()
               //해당 달의 특정 주가 존재하는지 검증
               .filter(set -> DateUtil.isExistWeekDay(set.getLocalDate(), week.getValue()))
               //년,월,주,요일에 해당하는 날짜 생성
               .map(set -> DateUtil.findSpecificWeekDay(set.getLocalDate(), week.getValue(), DayOfWeek.of(set.getDayOfWeekValue())))
               //시작 날짜 이후인지 검증
               .filter(date -> DateUtil.isAfter(date, startDay))
               //종료 날짜 이전인지 검증
               .filter(date -> DateUtil.isBefore(date, endDay))
               .collect(Collectors.toList());

        return scheduleDate;
   }

    //일정 유효성 검사
    private Schedule validAndGetSchedule(Long scheduleNo){
        return scheduleRepository.findValidSchedule(scheduleNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_SCHEDULE, String.format("Schedule No = [%d]", scheduleNo)));
    }

}
