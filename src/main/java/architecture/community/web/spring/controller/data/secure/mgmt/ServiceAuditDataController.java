package architecture.community.web.spring.controller.data.secure.mgmt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;

import architecture.community.exception.NotFoundException;
import architecture.community.util.CommunityConstants;
import architecture.community.web.spring.controller.data.model.ServicesConfig;
import architecture.ee.service.ConfigService;

@Controller("community-mgmt-services-audit-secure-data-controller")
@RequestMapping("/data/secure/mgmt/services/audit")
public class ServiceAuditDataController {

	private Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired
	@Qualifier("configService")
	private ConfigService configService;
	
	@Secured({ "ROLE_ADMINISTRATOR", "ROLE_SYSTEM", "ROLE_DEVELOPER"})
	@RequestMapping(value = "/config.json", method = { RequestMethod.POST })
	@ResponseBody
	public ServicesConfig getConfig (NativeWebRequest request) throws NotFoundException { 
		boolean enabled = configService.getApplicationBooleanProperty(CommunityConstants.SERVICES_AUDIT_ENABLED_PROP_NAME, false);
		ServicesConfig config = new ServicesConfig();
		config.setEnabled(enabled);
		return config;
	}
	
	@Secured({ "ROLE_ADMINISTRATOR", "ROLE_SYSTEM", "ROLE_DEVELOPER"})
	@RequestMapping(value = "/config/save-or-update.json", method = { RequestMethod.POST })
	@ResponseBody
	public ServicesConfig saveOrUpdate (@RequestBody  ServicesConfig config,  NativeWebRequest request) throws NotFoundException {  
		log.debug("viewcounts : {}", config.isEnabled());
		configService.setApplicationProperty( CommunityConstants.SERVICES_AUDIT_ENABLED_PROP_NAME, Boolean.toString(config.isEnabled()));
		return config;
	}	
}
