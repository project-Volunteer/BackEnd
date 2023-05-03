package project.volunteer.global.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import project.volunteer.domain.user.dao.UserRepository;

@Service
@RequiredArgsConstructor
public class PrincipalDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
    	// 로그인 인증 로직
        return userRepository.findById(id)
                .map(m -> new PrincipalDetails(m))
                .orElseThrow( () -> new UsernameNotFoundException("존재하지 않은 사용자 입니다."));
    }
}
