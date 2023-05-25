package project.volunteer.domain.sehedule.application;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.repeatPeriod.domain.Day;
import project.volunteer.domain.repeatPeriod.domain.Period;
import project.volunteer.domain.repeatPeriod.domain.Week;
import project.volunteer.domain.sehedule.application.dto.ScheduleParamReg;
import project.volunteer.domain.sehedule.dao.ScheduleRepository;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.domain.sehedule.application.dto.ScheduleParam;
import project.volunteer.global.common.component.Address;
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
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService{

    private final ScheduleRepository scheduleRepository;
    private final RecruitmentRepository recruitmentRepository;

    @Override
    @Transactional
    public Long addSchedule(Long recruitmentNo, ScheduleParam dto) {

        Recruitment recruitment = recruitmentRepository.findById(recruitmentNo)
                .orElseThrow(() ->  new BusinessException(ErrorCode.NOT_EXIST_RECRUITMENT, String.format("Recruitment No = [%d]", recruitmentNo)));

        Schedule createSchedule = Schedule.builder()
                .timetable(dto.getTimetable())
                .content(dto.getContent())
                .volunteerNum(dto.getVolunteerNum())
                .organizationName(dto.getOrganizationName())
                .address(dto.getAddress())
                .build();
        createSchedule.setRecruitment(recruitment);

        return scheduleRepository.save(createSchedule).getScheduleNo();
    }

    @Override
    @Transactional
    public void addRegSchedule(Long recruitmentNo, ScheduleParamReg dto) {

        //공통 메서드로 분리 생각해보기
        Recruitment recruitment = recruitmentRepository.findById(recruitmentNo)
                .orElseThrow(() ->  new BusinessException(ErrorCode.NOT_EXIST_RECRUITMENT, String.format("Recruitment No = [%d]", recruitmentNo)));

        List<LocalDate> scheduleDate = (dto.getRepeatPeriodParam().getPeriod().equals(Period.MONTH))?
                (makeDatesOfRegMonth(dto.getTimetable().getStartDay(), dto.getTimetable().getEndDay(),
                dto.getRepeatPeriodParam().getWeek(), dto.getRepeatPeriodParam().getDays())):
                (makeDatsOfRegWeek(dto.getTimetable().getStartDay(), dto.getTimetable().getEndDay(), dto.getRepeatPeriodParam().getDays()));

        //스케줄 등록
        scheduleDate.stream()
                .forEach(date -> {
                    Timetable timetable = Timetable.builder()
                            .startDay(date)
                            .endDay(date)
                            .hourFormat(dto.getTimetable().getHourFormat())
                            .startTime(dto.getTimetable().getStartTime())
                            .progressTime(dto.getTimetable().getProgressTime())
                            .build();
                    Address address = Address.builder()
                            .sido(dto.getAddress().getSido())
                            .sigungu(dto.getAddress().getSigungu())
                            .details(dto.getAddress().getSigungu())
                            .build();
                    Schedule schedule = Schedule.builder()
                            .timetable(timetable)
                            .address(address)
                            .organizationName(dto.getOrganizationName())
                            .content(dto.getContent())
                            .volunteerNum(dto.getVolunteerNum())
                            .build();
                    schedule.setRecruitment(recruitment);
                    scheduleRepository.save(schedule);
                });
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

}
