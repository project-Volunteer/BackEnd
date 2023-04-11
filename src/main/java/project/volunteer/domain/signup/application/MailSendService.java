package project.volunteer.domain.signup.application;

import java.util.Map;

import javax.mail.MessagingException;

public interface MailSendService {
	public Map<String, String> sendEmail(String toAddress, String subject, String body);
}
