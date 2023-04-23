package project.volunteer.domain.signup.application;

import java.util.Map;

import javax.mail.MessagingException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import project.volunteer.domain.signup.api.dto.response.MailSendResultResponse;
import project.volunteer.domain.signup.application.MailSendService;

@SpringBootTest
class MailSendServiceImplTest {
	
	@Autowired
	MailSendService mailSendService;
	
	@Test
	void mail_발송성공() throws MessagingException {
		// given
		String toAddress="jw_passion@naver.com";
		String subject="Volunteer Sign Auth code";
		String authCode="123456";
		
		// when
		ResponseEntity<MailSendResultResponse> result = mailSendService.sendEmail(toAddress, subject, authCode);
		
		// then
		Assertions.assertThat(result.getBody().getMessage()).contains("success");
	}
	

	@Test
	void mail_발송실패() {
		// given
		String toAddress="jw_passion";
		String subject="Volunteer Sign Auth code";
		String authCode="123456";

		// when
		ResponseEntity<MailSendResultResponse> result = mailSendService.sendEmail(toAddress, subject, authCode);
		
		// then
		Assertions.assertThat(result.getBody().getMessage()).contains("Fail");
	}

}
