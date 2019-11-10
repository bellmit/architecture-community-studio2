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
import architecture.community.query.CustomQueryService;
import architecture.community.tag.DefaultContentTag;
import architecture.community.viewcount.ViewCountService;
import architecture.community.web.model.Result;
import architecture.ee.service.ConfigService;
import architecture.ee.service.Repository;

@Controller("community-mgmt-services-viewcounts-secure-data-controller")
@RequestMapping("/data/secure/mgmt/services/")
public class ServicesViewCountDataController {
	
	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	@Qualifier("repository")
	private Repository repository;	
	
	@Autowired
	@Qualifier("configService")
	private ConfigService configService;

	@Autowired(required = false) 
	@Qualifier("customQueryService")
	private CustomQueryService customQueryService;
	
	@Autowired(required = false) 
	@Qualifier("viewCountService")
	private ViewCountService viewCountService;
	
	public ServicesViewCountDataController() { 
	
	}
  
	@Secured({ "ROLE_ADMINISTRATOR", "ROLE_SYSTEM", "ROLE_DEVELOPER"})
	@RequestMapping(value = "/viewcounts/refresh.json", method = { RequestMethod.POST })
	@ResponseBody
	public Result refresh (NativeWebRequest request) throws NotFoundException { 
		
		if(viewCountService.isViewCountsEnabled())
			viewCountService.updateViewCounts();
		return Result.newResult();
	}
}
