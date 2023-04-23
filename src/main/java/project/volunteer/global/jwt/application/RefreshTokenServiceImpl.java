package project.volunteer.global.jwt.application;

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
	public void updateRefreshToken(String userId, String refreshToken) {
		Optional<User> findUser = userRepository.findById(userId);
		findUser.get().setRefreshToken(refreshToken);
	}

	// 회원 정보 조회
	public Optional<User> findById(String userId) {
		return userRepository.findById(userId);
	}

	// 리프레시 토큰 검증
	public void validRefreshTokenValue(String userId, String refreshToken) throws IllegalAccessException {
		Optional<User> findUser = userRepository.findById(userId);
        String dbRefreshToken = findUser.map(t -> new String(t.getRefreshToken())).orElseThrow(() -> new NullPointerException());

        if(!refreshToken.equals(dbRefreshToken))
            throw new IllegalAccessException();
    }

	@Override
	public Optional<User> findByRefreshToken(String refreshToken) {
		return userRepository.findByRefreshToken(refreshToken);
	}
	
}
