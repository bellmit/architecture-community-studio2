package architecture.community.services.audit.event.listener;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.Subscribe;

import architecture.community.services.audit.event.AuditLogEvent;
import architecture.community.util.CommunityConstants;
import architecture.ee.service.ConfigService;

@Component
public class AuditLogEventListener {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	@Inject
	@Qualifier("configService")
	private ConfigService configService;
	
	@PostConstruct
	public void initialize() throws Exception {
		if( configService != null)
		{ 
		}
	}
	
	@PreDestroy
	public void destory(){
		if( configService != null)
		{ 
		}
	}
	
	@Subscribe 
	@EventListener 
	@Async
	public void handelAuditLogEvent(AuditLogEvent e) {

		boolean enabled = configService.getApplicationBooleanProperty(CommunityConstants.SERVICES_AUDIT_ENABLED_PROP_NAME);
		
		logger.debug("AUDTI ENABLED: {}", enabled );
		logger.debug("AUDIT : {}, ACTIVITY:{}" , e.toString() );
	}
		
	
}
