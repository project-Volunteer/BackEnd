package project.volunteer.domain.signup.application;

import javax.mail.MessagingException;

import org.springframework.http.ResponseEntity;

import project.volunteer.domain.signup.api.dto.response.MailSendResultResponse;

public interface MailSendService {
	public ResponseEntity<MailSendResultResponse> sendEmail(String toAddress, String subject, String body);
}
