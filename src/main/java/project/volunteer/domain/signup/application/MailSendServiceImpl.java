package project.volunteer.domain.signup.application;

import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MailSendServiceImpl implements MailSendService{
	@Autowired
	private JavaMailSender sender;

	public Map<String, String> sendEmail(String toAddress, String subject, String authCode) {
		Map<String, String> result = new HashMap<String, String>();
		MimeMessage message = sender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		try {
			helper.setTo(toAddress);
			helper.setSubject(subject);
			helper.setText("volunteer auth code : " + authCode);
			sender.send(message);
			
			result.put("authCode", authCode);
		} catch (MailSendException e) {
			log.error("MailSendException {}", e.getMessage(), e);
			result.put("resultMessage", "Failed to send mail");
		} catch(MessagingException e) {
			log.error("MessagingException {}", e.getMessage(), e);
			result.put("resultMessage", "Failed to send mail");
		}
		return result;
	}
}
