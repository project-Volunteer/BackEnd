package project.volunteer.domain.signup.application;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import project.volunteer.domain.signup.api.dto.response.MailSendResultResponse;

@Slf4j
@Service
public class MailSendServiceImpl implements MailSendService{
	@Autowired
	private JavaMailSender sender;

	public ResponseEntity<MailSendResultResponse> sendEmail(String toAddress, String subject, String authCode) {
		String resultMessage = "";
		MimeMessage message = sender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		try {
			helper.setTo(toAddress);
			helper.setSubject(subject);
			helper.setText("volunteer auth code : " + authCode);
			sender.send(message);
			
			resultMessage = "success send mail";
			return ResponseEntity.ok(new MailSendResultResponse(resultMessage, authCode));
		} catch (MailSendException e) {
			log.error("MailSendException {}", e.getMessage(), e);
			resultMessage = "메일 전송에 실패했습니다.";
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MailSendResultResponse(resultMessage, authCode));
		} catch(MessagingException e) {
			log.error("MessagingException {}", e.getMessage(), e);
			resultMessage = "메일 전송에 실패했습니다.";
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MailSendResultResponse(resultMessage, authCode));
		}
	}
}
