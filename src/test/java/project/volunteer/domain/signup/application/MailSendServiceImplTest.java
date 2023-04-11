package project.volunteer.domain.signup.application;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import project.volunteer.domain.signup.application.MailSendService;

@SpringBootTest
class MailSendServiceImplTest {
	
	@Autowired
	MailSendService mailSendService;
	
	@Test
	void mail_발송성공() {
		// given
		String toAddress="jw_passion@naver.com";
		String subject="Volunteer Sign Auth code";
		String authCode="123456";
		
		// when
		Map<String, String> result = mailSendService.sendEmail(toAddress, subject, authCode);
		
		// then
		Assertions.assertThat(result).containsKeys("authCode");
	}
	

	@Test
	void mail_발송실패() {
		// given
		String toAddress="jw_passion";
		String subject="Volunteer Sign Auth code";
		String authCode="123456";
		
		// when
		Map<String, String> result = mailSendService.sendEmail(toAddress, subject, authCode);
		
		// then
		Assertions.assertThat(result).containsKeys("resultMessage");
	}

}
