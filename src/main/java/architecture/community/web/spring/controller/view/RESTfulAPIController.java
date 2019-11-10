package architecture.community.web.spring.controller.view;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.ui.Model;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UrlPathHelper;

import com.google.common.base.Stopwatch;

import architecture.community.exception.NotFoundException;
import architecture.community.exception.UnAuthorizedException;
import architecture.community.model.Models;
import architecture.community.page.PathPattern;
import architecture.community.page.api.Api;
import architecture.community.page.api.ApiService;
import architecture.community.query.CustomQueryService;
import architecture.community.security.spring.acls.CommunityAclService;
import architecture.community.security.spring.acls.PermissionsBundle;
import architecture.community.services.CommunityGroovyService;
import architecture.community.services.CommunitySpringEventPublisher;
import architecture.community.services.audit.event.AuditLogEvent;
import architecture.community.util.SecurityHelper;
import architecture.community.viewcount.ViewCountService;
import architecture.community.web.model.Result;
import architecture.community.web.spring.view.script.DataView;
import architecture.community.web.util.ServletUtils;
import architecture.ee.service.ConfigService;
/**
 * 
 * 
 * @author donghyuck
 *
 */
@RestController("restful-api-data-controller")
@RequestMapping("/data/api")
public class RESTfulAPIController {

	private Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired(required=false)
	@Qualifier("apiService")
	private ApiService apiService;
	
	@Autowired(required=false)
	@Qualifier("configService")
	private ConfigService configService;
	
	@Autowired(required=false)
	@Qualifier("customQueryService")
	private CustomQueryService customQueryService;
	
	@Autowired(required=false)
	@Qualifier("communityAclService")
	private CommunityAclService communityAclService;
	
	@Autowired(required=false)
	@Qualifier("communityGroovyService")
	private CommunityGroovyService communityGroovyService;
	
	@Autowired(required=false)
	@Qualifier("viewCountService")
	private ViewCountService viewCountService;
	
	
	@Autowired(required=false)
	@Qualifier("communityEventPublisher")
	private CommunitySpringEventPublisher communitySpringEventPublisher;
	
	public RESTfulAPIController() { 
	
	}
	
	private boolean isSetApiService() {
		return apiService != null;
	}

	@RequestMapping(value = "/{filename:.+}", method = { RequestMethod.POST, RequestMethod.GET })
    public Object api (
    		@PathVariable String filename,
    		@RequestParam(value = "version", defaultValue = "1", required = false) int version,
    		@RequestParam(value = "preview", defaultValue = "false", required = false) boolean preview,
	    HttpServletRequest request, 
	    HttpServletResponse response, 
	    Model model) 
	    throws NotFoundException, UnAuthorizedException , Exception {
		
		log.debug("RESTful API : {}, {}", filename, version );
		Api api = apiService.getApi(filename); 
		
		if( !api.isEnabled() )
			throw new UnAuthorizedException("RESTful API is disabled."); 
		// checking permissions.
		if( api.getApiId() > 0 ) {
			if( api.isSecured() ) {
				PermissionsBundle bundle = communityAclService.getPermissionBundle(SecurityHelper.getAuthentication(), Models.API.getObjectClass(), api.getApiId());
				if( !bundle.isRead() )
					throw new UnAuthorizedException("Access Permission Required.");
			}
		} 
		
		Result result = Result.newResult();
		model.addAttribute("__page", api );
		
		if(communitySpringEventPublisher!=null)
			communitySpringEventPublisher.fireEvent((new AuditLogEvent.Builder(request, response, SecurityHelper.getAuthentication())).object(Models.API.getObjectType(), api.getApiId()).action(AuditLogEvent.READ_ACTION).label(api.getName()).build());
		
		
		if(StringUtils.isNotEmpty(api.getScriptSource())) { 
			Stopwatch stopwatch = Stopwatch.createStarted(); 
			try { 
				DataView _view = communityGroovyService.getService(api.getScriptSource(), DataView.class); 
				log.debug("set content type : {}", api.getContentType () );
				if(StringUtils.isNotEmpty(api.getContentType()))
					ServletUtils.setContentType(api.getContentType(), response);
				return _view.handle(model.asMap(), request, response);
			} catch (Exception e) { 
				//result.setError(e);
				throw e;
			} finally {
				stopwatch.stop();
				log.info("script:{}, time:{} ", filename, stopwatch);
			}
		} 
		if(result.isSuccess()) {
			result.setSuccess(false);
		}
		return result;
	
	}	
	
	
	/**
	 * 
	 * pattern 기반의 페이지 호출 처리 .
	 * 
	 */
	@RequestMapping(value = "/*/**", method = { RequestMethod.POST, RequestMethod.GET })
    public Object apiByPattern (
    	@RequestParam(value = "version", defaultValue = "1", required = false) int version,
    	@RequestParam(value = "preview", defaultValue = "false", required = false) boolean preview,
    	HttpServletRequest request, 
	    HttpServletResponse response, 
	    Model model) 
	    throws NotFoundException, UnAuthorizedException , Exception {	  
		Result result = Result.newResult(); 
		UrlPathHelper pathHelper = new UrlPathHelper();
 		String path = pathHelper.getLookupPathForRequest(request);
 		AntPathMatcher pathMatcher = new AntPathMatcher(); 
 		for( PathPattern pattern : apiService.getPathPatterns("/data/apis") )
 		{ 	
 			boolean isPattern = pathMatcher.isPattern(pattern.getPattern()); 
 			boolean match = false;
 			Map<String, String> variables = null; 
 			if( isPattern) {
 				AntPathRequestMatcher matcher = new AntPathRequestMatcher(pattern.getPattern());
 				match = matcher.matches(request) ;
 				variables = matcher.extractUriTemplateVariables(request);  
 			}else {  
 				try {
					variables = pathMatcher.extractUriTemplateVariables(pattern.getPattern(), path);
				} catch (Exception e) {
					variables = new HashMap<String, String>();
				} 
 				match = pathMatcher.match( pattern.getPattern(), path);
 			}	
 			
 			log.debug("Path Pattern Checking (pattern:{}) : {}, match : {}, variables: {}", isPattern, pattern.getPattern(), match, variables);
 			if( match ) {  
 				StopWatch watch = new StopWatch();
 				try {
 					watch.start();
	 				Api page = apiService.getApiById( pattern.getObjectId() );
	 				if( page.getApiId() > 0 ) {
	 					if( page.isSecured() ) {
	 						PermissionsBundle bundle = communityAclService.getPermissionBundle(SecurityHelper.getAuthentication(), Models.API.getObjectClass(), page.getApiId());
	 						if( !bundle.isRead() )
	 							throw new UnAuthorizedException();
	 					}
	 					if( viewCountService!=null && !preview  )
	 						viewCountService.addViewCount(Models.API.getObjectType(), page.getApiId() );	
	 				}
	 				model.addAttribute("__page", page); 
	 				model.addAttribute("__variables", variables);  
	 				
	 				if(communitySpringEventPublisher!=null)
	 					communitySpringEventPublisher.fireEvent((new AuditLogEvent.Builder(request, response, SecurityHelper.getAuthentication())).object(Models.API.getObjectType(), page.getApiId()).action(AuditLogEvent.READ_ACTION).label(page.getName()).build());
	 				
	 				if(StringUtils.isNotEmpty(page.getScriptSource())) {
	 					DataView _view = communityGroovyService.getService(page.getScriptSource(), DataView.class);
	 					try {
	 						if(StringUtils.isNotEmpty(page.getContentType()))
	 							ServletUtils.setContentType(page.getContentType(), response);
	 						return _view.handle(model.asMap(), request, response);
						} catch (Exception e) { 
							log.error("Error process data ..", e);
							//result.setError(e);
							throw e;
						}
	 				}
 				}finally {
 					watch.stop();
 					log.debug("DATA API CALLED : {}" , watch.prettyPrint());
 				}
 				break;
 			}
 		}
		
 		
 		if(result.isSuccess()) {
			result.setSuccess(false);
		}
 		
 		
		return result;
	}
}
