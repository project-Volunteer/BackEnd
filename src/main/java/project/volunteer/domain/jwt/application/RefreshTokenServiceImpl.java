package project.volunteer.domain.jwt.application;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.User;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
	@Autowired
	private final UserRepository userRepository;
	
	// 리프레시 토큰 저장
	@Transactional
	public void updateRefreshToken(Long userNo, String refreshToken) {
		Optional<User> findUser = userRepository.findByUserNo(userNo);
		findUser.get().setRefreshToken(refreshToken);
	}

	// 회원 정보 조회
	public Optional<User> findByUserNo(Long userNo) {
		return userRepository.findByUserNo(userNo);
	}

	// 리프레시 토큰 검증
	public void validRefreshTokenValue(Long userNo, String refreshToken) throws IllegalAccessException {
		Optional<User> findUser = userRepository.findByUserNo(userNo);
        String dbRefreshToken = findUser.map(t -> new String(t.getRefreshToken())).orElseThrow(() -> new NullPointerException());

        if(!refreshToken.equals(dbRefreshToken))
            throw new IllegalAccessException();
    }
	
}
