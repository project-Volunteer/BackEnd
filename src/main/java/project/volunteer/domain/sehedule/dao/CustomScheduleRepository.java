package project.volunteer.domain.sehedule.dao;

import project.volunteer.domain.sehedule.domain.Schedule;

import java.util.Optional;

public interface CustomScheduleRepository {

    //JPQL에서 first, limit 쿼리 지원하지 않음
    //Querydsl 사용
    public Optional<Schedule> findNearestSchedule(Long recruitmentNo);

}
