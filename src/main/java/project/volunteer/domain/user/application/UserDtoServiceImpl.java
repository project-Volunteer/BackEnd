package project.volunteer.domain.user.application;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.volunteer.domain.user.api.dto.response.UserAlarmResponse;
import project.volunteer.domain.user.api.dto.response.UserJoinRequestListResponse;
import project.volunteer.domain.user.api.dto.response.UserRecruitingListResponse;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.dao.queryDto.UserQueryDtoRepository;
import project.volunteer.domain.user.dao.queryDto.dto.UserRecruitingQuery;
import project.volunteer.domain.user.dao.queryDto.dto.UserRecruitmentJoinRequestQuery;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDtoServiceImpl implements UserDtoService{
	
	private final UserRepository userRepository;
	private final UserQueryDtoRepository userQueryDtoRepository;
	
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
	
	// 유저 존재 유무 확인
	public User isUserExists(Long userNo) {
		return userRepository.findByUserNo(userNo)
				.orElseThrow(()-> new BusinessException(ErrorCode.NOT_EXIST_USER, 
						String.format("not found user = [%d]", userNo)));
	}
}
