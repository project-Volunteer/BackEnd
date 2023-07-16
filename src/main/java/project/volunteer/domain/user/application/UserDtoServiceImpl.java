package project.volunteer.domain.user.application;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.volunteer.domain.logboard.dao.LogboardRepository;
import project.volunteer.domain.participation.dao.ParticipantRepository;
import project.volunteer.domain.participation.domain.Participant;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.scheduleParticipation.dao.ScheduleParticipationRepository;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;
import project.volunteer.domain.user.api.dto.response.*;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.dao.queryDto.UserQueryDtoRepository;
import project.volunteer.domain.user.dao.queryDto.dto.UserRecruitingQuery;
import project.volunteer.domain.user.dao.queryDto.dto.UserRecruitmentJoinRequestQuery;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDtoServiceImpl implements UserDtoService{
	
	private final UserRepository userRepository;
	private final UserQueryDtoRepository userQueryDtoRepository;

	private final ParticipantRepository participantRepository;
	private final RecruitmentRepository recruitmentRepository;
	private final LogboardRepository logboardRepository;
	private final ScheduleParticipationRepository scheduleParticipationRepository;

	@Override
	public UserJoinRequestListResponse findUserJoinRequest(Long userNo) {
		isUserExists(userNo);
		List<UserRecruitmentJoinRequestQuery> data = userQueryDtoRepository.findUserRecruitmentJoinRequestDto(userNo);
		
		return new UserJoinRequestListResponse(data);
	}

	@Override
	public UserRecruitingListResponse findUserRecruiting(Long userNo) {
		isUserExists(userNo);
		List<UserRecruitingQuery> data = userQueryDtoRepository.findUserRecruitingDto(userNo);
		
		return new UserRecruitingListResponse(data);
	}

	@Override
	public UserAlarmResponse findUserAlarm(Long userNo) {
		User user = isUserExists(userNo);
		
		return new UserAlarmResponse(user.getJoinAlarmYn(), user.getNoticeAlarmYn(), user.getBeforeAlarmYn());
	}

	@Override
	public UserInfo findUserInfo(Long loginUserNo) {
		User user = isUserExists(loginUserNo);
		return new UserInfo(user.getNickName(), user.getEmail(), user.getPicture());
	}

	@Override
	public HistoryTimeInfo findHistoryTimeInfo(Long loginUserNo) {
		HistoryTimeInfo historyTimeInfo = new HistoryTimeInfo();
		int sumProgressTime = 0;
		List<ScheduleParticipation> scheduleParticipationList = scheduleParticipationRepository.findScheduleJoinHistoryByUserno(loginUserNo);
		for(ScheduleParticipation sp :scheduleParticipationList){
			sumProgressTime += sp.getSchedule().getScheduleTimeTable().getProgressTime();
		}
		historyTimeInfo.setTotalCnt(scheduleParticipationList.size());
		historyTimeInfo.setTotalTime(sumProgressTime);
		return historyTimeInfo;
	}

	@Override
	public ActivityInfo findActivityInfo(Long loginUserNo) {
		ActivityInfo activityInfo = new ActivityInfo();
		int joinApprovalCnt=0;
		int joinRequestCnt=0;

		List <Participant> participantList = participantRepository.findJoinStatusByTeamUserno(loginUserNo);
		for(Participant p : participantList){
			if(p.getState().equals(ParticipantState.JOIN_APPROVAL)){
				joinApprovalCnt+=1;
			} else if(p.getState().equals(ParticipantState.JOIN_REQUEST)){
				joinRequestCnt+=1;
			}
		}
		int tempSavingCnt =0;
		tempSavingCnt += recruitmentRepository.findRecruitmentByUserNoAndPublishedYn(loginUserNo,false);
		tempSavingCnt += logboardRepository.findNotPublishedByUserNo(loginUserNo);


		activityInfo.setRecruitingCnt(recruitmentRepository.findRecruitmentByUserNoAndPublishedYn(loginUserNo,true));
		activityInfo.setJoinApprovalCnt(joinApprovalCnt);
		activityInfo.setJoinRequestCnt(joinRequestCnt);
		activityInfo.setTempSavingCnt(tempSavingCnt);

		return activityInfo;
	}

	// 유저 존재 유무 확인
	public User isUserExists(Long userNo) {
		return userRepository.findByUserNo(userNo)
				.orElseThrow(()-> new BusinessException(ErrorCode.NOT_EXIST_USER, 
						String.format("not found user = [%d]", userNo)));
	}
}
