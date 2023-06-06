package project.volunteer.global.jwt.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.Cookie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.jwt.application.JwtService;
import project.volunteer.global.jwt.dto.JwtToken;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RefreshTokenControllerTest {

	@Autowired MockMvc mockMvc;
	@Autowired UserRepository userRepository;
	@Autowired ObjectMapper objectMapper;
	@Autowired JwtService jwtService;
	@PersistenceContext EntityManager em;

	
	private void clear() {
		em.flush();
		em.clear();
	}
	
	private JwtToken setData() throws IOException {
		User user = userRepository.save(User.builder()
				.id("kakao_2727267348")
				.password("$2a$10$.Mkm52/YGxFCF8Bbx55Df.oZ1ILf5BSdiSVdinqiVdNcxgnWs7fGW")
				.nickName("nickname11")
				.email("email11@gmail.com")
				.gender(Gender.M)
				.birthDay(LocalDate.now())
				.picture("picture")
				.joinAlarmYn(true).beforeAlarmYn(true).noticeAlarmYn(true)
				.role(Role.USER)
				.provider("kakao")
				.providerId("111111")
				.build());
		
		JwtToken jwtToken = jwtService.login(user.getId());
		user.setRefreshToken(jwtToken.getRefreshToken());
		
		clear();
		return jwtToken;
	}

    @WithMockUser(username = "kakao_2727267348", password = "$2a$10$.Mkm52/YGxFCF8Bbx55Df.oZ1ILf5BSdiSVdinqiVdNcxgnWs7fGW", roles = "USER")
	@Test
	public void 엑세스토큰_재발급_성공() throws Exception {
    	// given
    	JwtToken jwtToken = setData();
    	
    	// when then
		Cookie cookie = new Cookie("refreshToken", jwtToken.getRefreshToken());
		
		mockMvc.perform(post("/reissue").contentType(MediaType.APPLICATION_JSON)
				.cookie(cookie))
		        .andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print());
	}
    
    
    @WithMockUser(username = "kakao_2727267348", password = "$2a$10$.Mkm52/YGxFCF8Bbx55Df.oZ1ILf5BSdiSVdinqiVdNcxgnWs7fGW", roles = "USER")
	@Test
	public void 엑세스토큰_재발급_실패() throws Exception {
		Cookie cookie = new Cookie("refreshToken", "notAccessToken");

		mockMvc.perform(post("/reissue").contentType(MediaType.APPLICATION_JSON)
				.cookie(cookie))
		        .andExpect(MockMvcResultMatchers.status().is4xxClientError()).andDo(MockMvcResultHandlers.print());;
	}
    
}
