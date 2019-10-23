package architecture.community.web.spring.controller.data.secure.mgmt;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;

import architecture.community.services.setup.CommunitySetupService;
import architecture.community.web.model.Result;
import architecture.ee.component.editor.DataSourceConfigReader;
import architecture.ee.service.ConfigService;
import architecture.ee.service.Repository; 

@Controller("community-mgmt-setup-secure-data-controller")
@RequestMapping("/data/secure/mgmt")
public class SetupDataController {
	
	private Logger log = LoggerFactory.getLogger(SetupDataController.class);
	
	@Autowired( required = true) 
	@Qualifier("repository")
	private Repository repository;
	
	@Inject
	@Qualifier("configService")
	private ConfigService configService;
	

	
	@Inject
	@Qualifier("setupService")
	private CommunitySetupService setupService;
	
	public SetupDataController() { 
	
	}

	private DataSourceConfigReader getDataSourceConfigReader() {
		return new DataSourceConfigReader(repository.getSetupApplicationProperties());
	}
	
	
	
	@Secured({ "ROLE_ADMINISTRATOR", "ROLE_SYSTEM", "ROLE_DEVELOPER"})
	@RequestMapping(value = "/setup/database/init.json", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public Result setupDatabase( NativeWebRequest request){  
		Result result = Result.newResult();
			
		log.debug("SETUP Database Init...");
		if( configService.isSetDataSource() && !configService.isDatabaseInitialized())
		{
			log.debug("Database Initializing.");
			setupService.setupDatabase();
		}
		return result;
	}
	 
}
