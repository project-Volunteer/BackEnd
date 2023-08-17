package project.volunteer.domain.logboard.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import project.volunteer.domain.like.dao.LikeRepository;
import project.volunteer.domain.like.domain.Like;
import project.volunteer.domain.logboard.application.dto.LogboardDetail;
import project.volunteer.domain.logboard.application.dto.LogboardEditDetail;
import project.volunteer.domain.logboard.dao.LogboardRepository;
import project.volunteer.domain.logboard.domain.Logboard;
import project.volunteer.domain.scheduleParticipation.dao.ScheduleParticipationRepository;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;
import project.volunteer.domain.sehedule.dao.ScheduleRepository;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.global.common.validate.LogboardValidate;
import project.volunteer.global.common.validate.UserValidate;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LogboardServiceImpl implements LogboardService{

	private final LogboardRepository logboardRepository;
	private final ScheduleRepository scheduleRepository;
	private final ScheduleParticipationRepository scheduleParticipationRepository;
	private final LikeRepository likeRepository;

	private final UserValidate userValidate;
	private final LogboardValidate logboardValidate;

	@Transactional
	@Override
	public Long addLog(Long userNo, String content, Long scheduleNo, Boolean isPublished) {
		// 사용자 존재 유무 검증
		User writer = userValidate.validateAndGetUser(userNo);
		
		// 일정 유무 검증(is_deleted Y도 가능)
		Schedule findSchedule = vaildateAndGetSchedule(scheduleNo);
		
		// 로그 이미 등록 했는지 여부 검증
		logboardValidate.validateLogboardAlreadyWrite(userNo, scheduleNo);
		
		// 일정 참가 완료 승인 상태 검증
		validateAndGetScheduleParticipationCompleteApprovalUser(userNo, scheduleNo);
		
		Logboard logboard = Logboard.createLogBoard(content, isPublished, userNo);
		logboard.setWriter(writer);
		logboard.setSchedule(findSchedule);
		
		return logboardRepository.save(logboard).getLogboardNo();
	}

	@Override
	public LogboardEditDetail findLogboard(Long logboardNo) {
		Logboard logboard = logboardValidate.validateAndGetLogboard(logboardNo);

		return new LogboardEditDetail(logboard);
	}

	@Transactional
	@Override
	public void editLog(Long logboardNo, Long userNo, String content, Long scheduleNo, Boolean isPublished) {
		// 사용자 존재 유무 검증
		userValidate.validateAndGetUser(userNo);

		// 로그 존재 유무 확인
		Logboard findLogboard = logboardValidate.validateAndGetLogboard(logboardNo);

		// 작성자 여부 검증
		logboardValidate.validateEqualParamUserNoAndFindUserNo(userNo, findLogboard);

		// 일정 유무 검증(is_deleted Y도 가능)
		Schedule findSchedule = vaildateAndGetSchedule(scheduleNo);

		// 일정 참가 완료 승인 상태 검증
		validateAndGetScheduleParticipationCompleteApprovalUser(userNo, scheduleNo);

		// 수정
		findLogboard.editLogBoard(content, isPublished, userNo);
		findLogboard.setSchedule(findSchedule);
		
		logboardRepository.save(findLogboard);
	}
	
	@Override
    @Transactional
	public void deleteLog(Long userNo, Long logboardNo) {
		// 로그 존재 유무 확인
		Logboard findLogboard = logboardValidate.validateAndGetLogboard(logboardNo);
		
		// 작성자 여부 검증
		logboardValidate.validateEqualParamUserNoAndFindUserNo(userNo, findLogboard);
		
		findLogboard.delete();
	}

	@Override
	@Transactional
	public LogboardDetail detailLog(Long no) {
		// 로그 존재 유무 확인
		logboardValidate.validateAndGetLogboard(no);

		Logboard findLogboard = logboardValidate.validateAndGetLogboard(no);
		findLogboard.increaseViewNum();

		return new LogboardDetail(findLogboard);
	}

	@Override
	@Transactional
	public void likeLogboard(Long userNo, Long no) {
		//로그 존재 유무 확인(낙관적락 사용)
		Logboard findLogboard = logboardValidate.validateAndGetLogboardWithOPTIMSTIC_LOCK(no);

		//로그 좋아요 유무에 따른 분기
		Optional<Like> findLike = likeRepository.findLike(userNo, RealWorkCode.LOG, findLogboard.getLogboardNo());
		if(findLike.isPresent()){
			likeRepository.delete(findLike.get());
			findLogboard.decreaseLikeNum();
		} else{
			Like createLike = Like.createLike(RealWorkCode.NOTICE, findLogboard.getLogboardNo());
			User loginUser = userValidate.validateAndGetUser(userNo);
			createLike.setUser(loginUser);

			likeRepository.save(createLike);
			findLogboard.increaseLikeNum();
		}

	}


	// validation 메서드
	// 일정 존재 유무 확인
	public Schedule vaildateAndGetSchedule(Long scheduleNo) {
		return scheduleRepository.findById(scheduleNo)
				.orElseThrow(()-> new BusinessException(ErrorCode.NOT_EXIST_SCHEDULE, 
						String.format("not found schedule = [%d]", scheduleNo)));
	}

	// 일정 참여 후 일정 참가 완료 승인 상태 여부 체크
	public ScheduleParticipation validateAndGetScheduleParticipationCompleteApprovalUser(Long userNo, Long scheduleNo) {
		return scheduleParticipationRepository
				.findByUserNoAndScheduleNoAndState(userNo, scheduleNo, ParticipantState.PARTICIPATION_COMPLETE_APPROVAL)
					.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_STATE_LOGBOARD,
							String.format("UserNo = [%d], ScheduleNo = [%d]", userNo, scheduleNo)));
	}

}
