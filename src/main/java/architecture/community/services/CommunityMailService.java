package architecture.community.services;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;

import architecture.ee.service.ConfigService;
import architecture.ee.service.Repository;


public class CommunityMailService implements MailService {

	private Logger log = LoggerFactory.getLogger(CommunityMailService.class);
	
	
	@Inject
	@Qualifier("repository")
	private Repository repository;	
	
	@Inject
	@Qualifier("configService")
	private ConfigService configService;	
	
	@Inject
	@Qualifier("mailSender")
	private JavaMailSender mailSender;	 
	
	public boolean isEnabled () {  
		boolean defaultValue = repository.getSetupApplicationProperties().getBooleanProperty("services.mail.enabled", false);
		boolean enabled = configService.getApplicationBooleanProperty("services.mail.enabled", defaultValue); 
		return enabled;
	}	
	
	@Async
	public void send(String fromUser, String toUser, String subject, String body, boolean html ) throws Exception {
        try {
        	
        	log.debug("mail service enabled : {}", isEnabled()); 
	        if(isEnabled()) {
	        	MimeMessage message = mailSender.createMimeMessage();
	            MimeMessageHelper helper = new MimeMessageHelper(message, true);
	            
	            helper.setFrom(fromUser);
	            helper.setTo(toUser); 
	            helper.setSubject(subject);
	            helper.setText(body, html);    	
	            
	    		mailSender.send(message);
	        
	        }
        } catch (MessagingException e) {
        	log.error(e.getMessage(), e);
        }
	}
	
	@Async
	public void send(String fromEmail, String fromName,  String toMail, String subject, String body, boolean html ) throws Exception {
        try {
        	log.debug("mail service enabled : {}", isEnabled());
        
	        if(isEnabled()) {
	        	MimeMessage message = mailSender.createMimeMessage();
	            MimeMessageHelper helper = new MimeMessageHelper(message, true);
	            helper.setFrom(fromEmail, fromName);
	            helper.setTo(toMail); 
	            helper.setSubject(subject);
	            helper.setText(body, html);    	
	            
	    		mailSender.send(message);
	        
	        }
        } catch (MessagingException e) {
        	log.error(e.getMessage(), e);
        }
	}
	
}
