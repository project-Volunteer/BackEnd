package project.volunteer.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import project.volunteer.domain.signup.application.KakaoLoginService;
import project.volunteer.global.jwt.util.JwtProvider;
import project.volunteer.global.security.UserLoginSuccessCustomHandler;
import project.volunteer.global.security.failhandler.JwtAccessDeniedHandler;
import project.volunteer.global.security.failhandler.JwtAuthenticationEntryPoint;
import project.volunteer.global.security.failhandler.UserLoginFailureCustomHandler;
import project.volunteer.global.security.filter.JwtAuthenticationFilter;
import project.volunteer.global.security.filter.UsernamePasswordAuthenticationCustomFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	private final KakaoLoginService kakaoLoginService;
	private final ObjectMapper objectMapper;
	private final UserLoginSuccessCustomHandler successHandler;
	private final UserLoginFailureCustomHandler failureHandler;
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
	private final JwtProvider jwtProvider;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return
			http
			.csrf().disable()
			.cors()
			.and()
			
			.sessionManagement()
			.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			.and()
			
			.httpBasic().disable()
			.formLogin().disable()
			
			// exception handling 할 때 우리가 만든 클래스를 추가
            .exceptionHandling()
            // 인증되지 않은 유저 요청 시
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            // 권한이 없는 유저 요청 시
            .accessDeniedHandler(jwtAccessDeniedHandler)
            .and()
			
			 // 커스텀 필터 등록
            .apply(new MyCustomDsl())
			.and()

			.authorizeRequests()

					//봉사 모집글 관련
					.antMatchers(HttpMethod.POST, "/recruitment").hasAuthority("USER")
					.antMatchers(HttpMethod.DELETE, "/recruitment/*").hasAuthority("USER")
					.antMatchers(HttpMethod.GET, "/recruitment", "/recruitment/count").permitAll()
					.antMatchers(HttpMethod.GET,"/recruitment/*").permitAll()
					.antMatchers(HttpMethod.GET, "/recruitment/*/status").hasAuthority("USER")

					//팀원 관리
					.antMatchers(HttpMethod.POST, "/recruitment/join").hasAuthority("USER")
					.antMatchers(HttpMethod.POST, "/recruitment/cancel").hasAuthority("USER")
					.antMatchers(HttpMethod.POST, "/recruitment/approval").hasAuthority("USER")
					.antMatchers(HttpMethod.POST, "/recruitment/kick").hasAuthority("USER")

					//일정 관련
					.antMatchers(HttpMethod.GET, "/schedule/*").hasAuthority("USER")
					.antMatchers(HttpMethod.POST, "/schedule").hasAuthority("USER")
					.antMatchers(HttpMethod.PUT, "/schedule").hasAuthority("USER")
					.antMatchers(HttpMethod.DELETE, "/schedule/*").hasAuthority("USER")
					.antMatchers(HttpMethod.GET, "/schedule/*/calendar").hasAuthority("USER")

			.anyRequest().permitAll()
			.and()
			.build();
	}

	 //jwt 커스텀 필터 등록
    public class MyCustomDsl extends AbstractHttpConfigurer<MyCustomDsl, HttpSecurity> {

        @Override
        public void configure(HttpSecurity http)  {
            AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);

            http.addFilterBefore(new UsernamePasswordAuthenticationCustomFilter(authenticationManager, objectMapper, kakaoLoginService, successHandler,failureHandler), UsernamePasswordAuthenticationFilter.class);
            http.addFilter(new JwtAuthenticationFilter(authenticationManager, jwtProvider));
        }
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
