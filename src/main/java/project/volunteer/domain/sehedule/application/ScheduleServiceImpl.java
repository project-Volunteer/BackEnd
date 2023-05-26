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
import project.volunteer.domain.scheduleParticipation.dao.ScheduleParticipationRepository;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;
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
    private final ScheduleParticipationRepository scheduleParticipationRepository;

    @Override
    @Transactional
    public Long addSchedule(Long recruitmentNo, Long loginUserNo, ScheduleParam dto) {

        //봉사 모집글 검증
        Recruitment recruitment = isValidRecruitment(recruitmentNo);

        //모집글 방장 검증
        isRecruitmentOwner(recruitment, loginUserNo);

        //일정 참여가능 최대 수는 봉사 팀원 가능 인원보다 많을 수 없음.
        if(recruitment.getVolunteerNum() < dto.getVolunteerNum()){
            throw new BusinessException(ErrorCode.EXCEED_CAPACITY_PARTICIPANT,
                    String.format("Recruitment VolunteerNum = [%d], Schedule VolunteerNum = [%d]", recruitment.getVolunteerNum(), dto.getVolunteerNum()));
        }

        Schedule createSchedule =
                Schedule.createSchedule(dto.getTimetable(), dto.getContent(), dto.getOrganizationName(), dto.getAddress(), dto.getVolunteerNum());
        createSchedule.setRecruitment(recruitment);
        return scheduleRepository.save(createSchedule).getScheduleNo();
    }

    @Override
    @Transactional
    public void addRegSchedule(Long recruitmentNo, ScheduleParamReg dto) {

        //봉사 모집글 검증
        Recruitment recruitment = isValidRecruitment(recruitmentNo);

        List<LocalDate> scheduleDate = (dto.getRepeatPeriodParam().getPeriod().equals(Period.MONTH))?
                (makeDatesOfRegMonth(dto.getTimetable().getStartDay(), dto.getTimetable().getEndDay(),
                dto.getRepeatPeriodParam().getWeek(), dto.getRepeatPeriodParam().getDays())):
                (makeDatsOfRegWeek(dto.getTimetable().getStartDay(), dto.getTimetable().getEndDay(), dto.getRepeatPeriodParam().getDays()));

        //스케줄 등록
        scheduleDate.stream()
                .forEach(date -> {
                    Timetable timetable = Timetable.createTimetable(date, date, dto.getTimetable().getHourFormat(),
                            dto.getTimetable().getStartTime(), dto.getTimetable().getProgressTime());

                    Address address =
                            Address.createAddress(dto.getAddress().getSido(), dto.getAddress().getSigungu(), dto.getAddress().getDetails());

                    Schedule schedule =
                            Schedule.createSchedule(timetable, dto.getContent(), dto.getOrganizationName(), address, dto.getVolunteerNum());
                    schedule.setRecruitment(recruitment);

                    scheduleRepository.save(schedule);
                });
    }

    @Override
    @Transactional
    public Long editSchedule(Long scheduleNo, Long loginUserNo, ScheduleParam dto) {

        //일정 검증
        Schedule findSchedule = isValidSchedule(scheduleNo);

        //봉사 모집글 검증
        Recruitment recruitment = isValidRecruitment(findSchedule.getRecruitment().getRecruitmentNo());

        //모집글 방장 검증
        isRecruitmentOwner(recruitment, loginUserNo);

        //수정할 참여 인원수는 현재 일정에 참여중인 인원수보다 적을 수 없음.
        Integer activeVolunteerNum = scheduleParticipationRepository.countActiveParticipant(scheduleNo);
        if(activeVolunteerNum > dto.getVolunteerNum()){
            throw new BusinessException(ErrorCode.INSUFFICIENT_CAPACITY_PARTICIPANT,
                    String.format("ScheduleNo = [%d], activeVolunteerNum = [%d], editVolunteerNum = [%d]",
                            scheduleNo, activeVolunteerNum, dto.getVolunteerNum()));
        }

        //일정 참여가능 최대 수는 봉사 팀원 가능 인원보다 많을 수 없음.
        if(recruitment.getVolunteerNum() < dto.getVolunteerNum()){
            throw new BusinessException(ErrorCode.EXCEED_CAPACITY_PARTICIPANT,
                    String.format("ScheduleNo = [%d], Recruitment VolunteerNum = [%d], editVolunteerNum = [%d]",
                            scheduleNo, recruitment.getVolunteerNum(), dto.getVolunteerNum()));
        }

        //일정 정보 수정
        findSchedule.changeSchedule(dto.getTimetable(), dto.getContent(), dto.getOrganizationName(), dto.getAddress(), dto.getVolunteerNum());
        return findSchedule.getScheduleNo();
    }

    @Override
    @Transactional
    public void deleteSchedule(Long scheduleNo, Long loginUserNo) {

        //일정 검증
        Schedule findSchedule = isValidSchedule(scheduleNo);

        //봉사 모집글 검증
        Recruitment recruitment = isValidRecruitment(findSchedule.getRecruitment().getRecruitmentNo());

        //모집글 방장 검증
        isRecruitmentOwner(recruitment, loginUserNo);

        //일정 삭제
        findSchedule.delete();

        //일정 참가자 리스트 삭제
        List<ScheduleParticipation> participants = scheduleParticipationRepository.findBySchedule_ScheduleNo(scheduleNo);
        participants.stream()
                .forEach(p -> p.delete());

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
    private Schedule isValidSchedule(Long scheduleNo){
        return scheduleRepository.findValidByScheduleNo(scheduleNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_SCHEDULE, String.format("Schedule No = [%d]", scheduleNo)));
    }

   //모집 글 유효성 검사
    private Recruitment isValidRecruitment(Long recruitmentNo){
        return recruitmentRepository.findPublishedByRecruitmentNo(recruitmentNo)
                .orElseThrow(() ->  new BusinessException(ErrorCode.NOT_EXIST_RECRUITMENT, String.format("Recruitment No = [%d]", recruitmentNo)));
    }

    //모집글 방장 검증 메서드
    private void isRecruitmentOwner(Recruitment recruitment, Long loginUserNo){
        if(!recruitment.getWriter().getUserNo().equals(loginUserNo)){
            throw new BusinessException(ErrorCode.FORBIDDEN_RECRUITMENT,
                    String.format("RecruitmentNo = [%d], UserNo = [%d]", recruitment.getRecruitmentNo(), loginUserNo));
        }
    }

}
