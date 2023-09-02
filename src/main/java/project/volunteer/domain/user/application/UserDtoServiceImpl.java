package project.volunteer.domain.user.application;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.volunteer.domain.logboard.dao.LogboardRepository;
import project.volunteer.domain.logboard.domain.Logboard;
import project.volunteer.domain.participation.dao.ParticipantRepository;
import project.volunteer.domain.participation.dao.dto.UserRecruitmentDetails;
import project.volunteer.domain.participation.domain.Participant;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.dto.PictureDetails;
import project.volunteer.domain.scheduleParticipation.dao.ScheduleParticipationRepository;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;
import project.volunteer.domain.user.api.dto.response.*;
import project.volunteer.domain.user.dao.queryDto.UserQueryDtoRepository;
import project.volunteer.domain.user.dao.queryDto.dto.UserRecruitingQuery;
import project.volunteer.domain.user.dao.queryDto.dto.UserRecruitmentJoinRequestQuery;
import project.volunteer.global.common.component.ParticipantState;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDtoServiceImpl implements UserDtoService{
	private final UserQueryDtoRepository userQueryDtoRepository;
	private final ParticipantRepository participantRepository;
	private final RecruitmentRepository recruitmentRepository;
	private final LogboardRepository logboardRepository;
	private final ScheduleParticipationRepository scheduleParticipationRepository;

	@Override
	public UserJoinRequestListResponse findUserJoinRequest(Long userNo) {
		List<UserRecruitmentJoinRequestQuery> data = userQueryDtoRepository.findUserRecruitmentJoinRequestDto(userNo);
		return new UserJoinRequestListResponse(data);
	}

	@Override
	public UserRecruitingListResponse findUserRecruiting(Long userNo) {
		List<UserRecruitingQuery> data = userQueryDtoRepository.findUserRecruitingDto(userNo);
		return new UserRecruitingListResponse(data);
	}

	@Override
	public HistoryTimeInfo findHistoryTimeInfo(Long loginUserNo) {
		HistoryTimeInfo historyTimeInfo = new HistoryTimeInfo();
		int sumProgressTime = 0;
		List<ScheduleParticipation> scheduleParticipationList =
				scheduleParticipationRepository.findScheduleListByUsernoAndStatus(loginUserNo, ParticipantState.PARTICIPATION_COMPLETE_APPROVAL);
		for(ScheduleParticipation sp : scheduleParticipationList){
			sumProgressTime += sp.getSchedule().getScheduleTimeTable().getProgressTime();
		}
		historyTimeInfo.setTotalCnt(scheduleParticipationList.size());
		historyTimeInfo.setTotalTime(sumProgressTime);
		return historyTimeInfo;
	}

	@Override
	public ActivityInfo findActivityInfo(Long loginUserNo) {
		int joinApprovalCnt = 0;
		int joinRequestCnt = 0;
		int tempSavingCnt = 0;

		List <Participant> participantList = participantRepository.findJoinStatusByTeamUserno(loginUserNo);
		for(Participant p : participantList){
			if(p.getState().equals(ParticipantState.JOIN_APPROVAL)){
				joinApprovalCnt+=1;
			} else if(p.getState().equals(ParticipantState.JOIN_REQUEST)){
				joinRequestCnt+=1;
			}
		}
		tempSavingCnt += recruitmentRepository.findRecruitmentListByUserNoAndPublishedYn(loginUserNo,false).size();
		tempSavingCnt += logboardRepository.findLogboardListByUserNoAndPublishedYn(loginUserNo,false).size();

		ActivityInfo activityInfo = ActivityInfo.makeActivityInfo(
				recruitmentRepository.findRecruitmentListByUserNoAndPublishedYn(loginUserNo,true).size()
				,joinApprovalCnt
				,joinRequestCnt
				,tempSavingCnt);

		return activityInfo;
	}

	@Override
	public JoinScheduleListResponse findUserSchedule(Long loginUserNo) {
		List<ScheduleParticipation> scheduleParticipationList =
				scheduleParticipationRepository.findScheduleListByUsernoAndStatus(loginUserNo, ParticipantState.PARTICIPATING);
		return new JoinScheduleListResponse(scheduleParticipationList.stream().map(dto->{
			return new JoinScheduleList(dto.getScheduleParticipationNo()
					, dto.getSchedule().getScheduleTimeTable().getStartDay().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"))
					, dto.getSchedule().getAddress().getSido()
					, dto.getSchedule().getAddress().getSigungu()
					, dto.getSchedule().getAddress().getDetails()
					, dto.getSchedule().getOrganizationName()
					, dto.getSchedule().getScheduleTimeTable().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"))
					, dto.getSchedule().getScheduleTimeTable().getHourFormat().getDesc()
					, dto.getSchedule().getScheduleTimeTable().getProgressTime()
					);
		}).collect(Collectors.toList()));
	}

	@Override
	public JoinRecruitmentListResponse findUserRecruitment(Long loginUserNo) {
		List<UserRecruitmentDetails> userRecruitmentDetails = participantRepository.findRecuitmentByUsernoAndStatus(loginUserNo, ParticipantState.JOIN_APPROVAL);

		return new JoinRecruitmentListResponse(userRecruitmentDetails.stream()
				.map(dto -> {
					JoinRecruitmentList joinRecruitmentList = JoinRecruitmentList.makeJoinRecruitmentList(
							dto.getNo()
							, dto.getImagePath()
							, dto.getStartDay().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"))
							, dto.getEndDay().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"))
							, dto.getTitle()
							, dto.getSido()
							, dto.getSigungu()
							, dto.getDetails()
							, dto.getVolunteeringCategory().getDesc()
							, dto.getVolunteeringType().getViewName()
							, dto.getIsIssued()
							, dto.getVolunteerType().getDesc());
					return joinRecruitmentList;
				})
				.collect(Collectors.toList()));
	}

	@Override
	public RecruitmentTempListResponse findRecruitmentTempDtos(Long loginUserNo) {
		List<Recruitment> tempList = recruitmentRepository.findRecruitmentListByUserNoAndPublishedYn(loginUserNo, false);
		return new RecruitmentTempListResponse(tempList.stream().map(t ->{
				return new RecruitmentTempList(
						 t.getRecruitmentNo()
						,t.getTitle()
						,t.getCreatedDate().format(DateTimeFormatter.ofPattern("HH:mm")).toString()
						,t.getCreatedDate().format(DateTimeFormatter.ofPattern("MM-dd-yyyy")).toString());
			}).collect(Collectors.toList()));
	}

	@Override
	public LogboardTempListResponse findLoboardTempDtos(Long loginUserNo) {
		List<Logboard> tempList = logboardRepository.findLogboardListByUserNoAndPublishedYn(loginUserNo, false);
		return new LogboardTempListResponse(tempList.stream().map(t ->{
			return new LogboardTempList(
					 t.getLogboardNo()
					,t.getContent()
					,t.getCreatedDate().format(DateTimeFormatter.ofPattern("HH:mm")).toString()
					,t.getCreatedDate().format(DateTimeFormatter.ofPattern("MM-dd-yyyy")).toString());
		}).collect(Collectors.toList()));
	}
}
