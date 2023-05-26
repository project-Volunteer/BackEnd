package project.volunteer.domain.user.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.volunteer.domain.recruitment.dao.queryDto.dto.RecruitmentListQuery;
import project.volunteer.domain.user.api.dto.ActivityInfo;
import project.volunteer.domain.user.api.dto.HistoryTimeInfo;
import project.volunteer.domain.user.api.dto.UserDashboardResponse;
import project.volunteer.domain.user.api.dto.UserInfo;
import project.volunteer.domain.user.api.dto.response.UserJoinRequestListResponse;
import project.volunteer.domain.user.api.dto.response.UserRecruitingListResponse;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.dao.queryDto.UserQueryDtoRepository;
import project.volunteer.domain.user.dao.queryDto.dto.UserRecruitingQuery;
import project.volunteer.domain.user.dao.queryDto.dto.UserRecruitmentJoinRequestQuery;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;
import project.volunteer.global.util.SecurityUtil;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
	
	private final UserRepository userRepository;
	private final UserQueryDtoRepository userQueryDtoRepository;

	@Override
	public UserJoinRequestListResponse findUserJoinRequest(Long userNo) {
		User user = userRepository.findByUserNo(userNo)
				.orElseThrow(()-> new BusinessException(ErrorCode.NOT_EXIST_USER, 
						String.format("not found user = [%d]", SecurityUtil.getLoginUserId())));
		
		List<UserRecruitmentJoinRequestQuery> data = userQueryDtoRepository.findUserRecruitmentJoinRequestDto(userNo);
		
		return new UserJoinRequestListResponse(data);
	}

	@Override
	public UserRecruitingListResponse findUserRecruiting(Long userNo) {
		User user = userRepository.findByUserNo(userNo)
				.orElseThrow(()-> new BusinessException(ErrorCode.NOT_EXIST_USER, 
						String.format("not found user = [%d]", SecurityUtil.getLoginUserId())));
		
		List<UserRecruitingQuery> data = userQueryDtoRepository.findUserRecruitingDto(userNo);
		
		return new UserRecruitingListResponse(data);
	}

}
