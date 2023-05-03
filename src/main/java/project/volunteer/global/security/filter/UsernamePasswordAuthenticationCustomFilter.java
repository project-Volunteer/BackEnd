package project.volunteer.global.security.filter;

import java.io.IOException;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import project.volunteer.domain.signup.api.dto.response.KakaoUserInfoResponse;
import project.volunteer.domain.signup.application.KakaoLoginService;
import project.volunteer.global.security.UserLoginSuccessCustomHandler;
import project.volunteer.global.security.failhandler.UserLoginFailureCustomHandler;


@Slf4j
//로그인 인증 처리 커스텀 필터
public class UsernamePasswordAuthenticationCustomFilter extends AbstractAuthenticationProcessingFilter {
	private final ObjectMapper objectMapper;
	private final KakaoLoginService kakaoLoginService;
	private static final AntPathRequestMatcher DEFAULT_ANT_PATH_REQUEST_MATCHER = new AntPathRequestMatcher("/login", "POST");
	private final UserLoginSuccessCustomHandler successHandler;
	private final UserLoginFailureCustomHandler failureHandler;

	public UsernamePasswordAuthenticationCustomFilter(AuthenticationManager authenticationManager,
														ObjectMapper objectMapper,
														KakaoLoginService kakaoLoginService,
														UserLoginSuccessCustomHandler successHandler,
														UserLoginFailureCustomHandler failureHandler) {
		super(DEFAULT_ANT_PATH_REQUEST_MATCHER, authenticationManager);
		this.objectMapper = objectMapper;
		this.kakaoLoginService = kakaoLoginService;
		this.successHandler = successHandler;
		this.failureHandler = failureHandler;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException {
		// 1. body에서 json 데이터 받아오기
		Map<String, Object> map = objectMapper.readValue(request.getInputStream(), Map.class);
		
		// 2. kakao에서 발급한 authorizationCode으로 aceessToken 받아오기 
		String kakaoAccessToken = kakaoLoginService.getKakaoAccessToken((String)map.get("authorizationCode"));
		
		// 3. accessToken으로 사용자 정보 및 kakao USER 고유 키 받아오기
		String providerId = kakaoLoginService.getKakaoProviderId(kakaoAccessToken);
		
		// 4. usernamePasswordAuthenticationToken 생성을 위한 로그인아이디 및 비밀번호 설정
		String id = "kakao_" + providerId;
		String password = "kakao";

		// 5. 카카오 고유 ID와 암호화된 kakao String을 기반으로 AuthenticationToken 생성
		// 인증이 끝나고 SecurityContextHolder.getContext()에 등록될 Authentication 객체
		UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken 
													= new UsernamePasswordAuthenticationToken(id, password);

		// 6. User Password 인증이 이루어지는 부분
		// "authenticate" 가 실행될때 "PrincipalDetailService.loadUserByUsername" 실행
		Authentication authenticate = this.getAuthenticationManager().authenticate(usernamePasswordAuthenticationToken);

		return authenticate;
	}
	
	// 로그인 성공 시 handler설정(jwt 토큰 생성 및 관리)
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        this.successHandler.onAuthenticationSuccess(request, response, authResult);
    }

	// 로그인 실패 시 handler설정
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        this.failureHandler.onAuthenticationFailure(request,response, failed);
    }
}
