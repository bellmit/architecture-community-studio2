package architecture.community.web.spring.controller.data.secure.mgmt;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;

import architecture.community.exception.NotFoundException;
import architecture.community.user.UserProfile;
import architecture.community.user.UserProfileService;
import architecture.community.util.SecurityHelper;
import architecture.community.web.model.Result;
import architecture.ee.service.ConfigService;
import architecture.ee.service.Repository;

@Controller("community-mgmt-services-profile-secure-data-controller")
@RequestMapping("/data/secure/mgmt/services/profile")
public class ServicesProfileDataController {
	
	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	@Qualifier("repository")
	private Repository repository;	
	
	@Autowired
	@Qualifier("configService")
	private ConfigService configService;

	@Autowired(required=false)
	@Qualifier("customUserProfileService")
	private UserProfileService profileService; 
	
	public ServicesProfileDataController() { 
	}
	
	@Secured({ "ROLE_ADMINISTRATOR", "ROLE_SYSTEM", "ROLE_DEVELOPER"})
	@RequestMapping(value = "/config.json", method = { RequestMethod.POST, RequestMethod.GET })
    @ResponseBody
    public ProfileServiceConfig getProfileServicesConfig(
    		NativeWebRequest request) throws NotFoundException, IOException {  		
		return getProfileServiceConfig(); 
    }  
	
	@Secured({ "ROLE_ADMINISTRATOR", "ROLE_SYSTEM", "ROLE_DEVELOPER"})
	@RequestMapping(value = "/save-or-update.json", method = { RequestMethod.POST })
	@ResponseBody
    public ProfileServiceConfig saveOrUpdate(
    		@RequestBody ProfileServiceConfig config, 
    		@RequestParam(value = "restart", defaultValue = "true", required = false) Boolean restart, 
    		NativeWebRequest request) throws NotFoundException, IOException {  
		configService.setApplicationProperty("services.user.profile.cacheable", Boolean.toString(config.cacheable));
		configService.setApplicationProperty("services.user.profile.enabled", Boolean.toString(config.enabled));
		log.debug("Restart profile service : {}", restart);
		profileService.refresh();
		return getProfileServiceConfig(); 
	}
	
	
	@Secured({ "ROLE_ADMINISTRATOR", "ROLE_SYSTEM", "ROLE_DEVELOPER"})
	@RequestMapping(value = "/test.json", method = { RequestMethod.POST, RequestMethod.GET })
    @ResponseBody
    public Result testConnection(NativeWebRequest request) {   
		Result result = Result.newResult();
		log.debug("Profile Service Enabled  : {}", profileService.isEnabled() );
		if( profileService.isEnabled())
		{
			log.debug("Test .....");
			try {
				UserProfile p = profileService.getUserProfile(SecurityHelper.getUser());
				result.getData().put("profile", p);
				log.debug("Test Connection Successed.");
			} catch (Exception e) {
				result.setError(e);
			}
		}
		return result; 
    } 
	
	private ProfileServiceConfig getProfileServiceConfig() { 
		
		ProfileServiceConfig config = new ProfileServiceConfig();
		config.cacheable = configService.getApplicationBooleanProperty("services.user.profile.cacheable", false);
		config.enabled = configService.getApplicationBooleanProperty("services.user.profile.enabled", false);
		config.scriptSource = "/WEB-INF/groovy-script/services/customUserProfileService.groovy";
		
		return config;
	}
	
	public static class ProfileServiceConfig implements java.io.Serializable {
		
		private boolean enabled;
		
		private boolean cacheable;
		
		private String scriptSource;
		
		public ProfileServiceConfig() { 
		}
		
		public boolean isEnabled() {
			return enabled;
		}
		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
		public boolean isCacheable() {
			return cacheable;
		}
		public void setCacheable(boolean cacheable) {
			this.cacheable = cacheable;
		}
		public String getScriptSource() {
			return scriptSource;
		}
		public void setScriptSource(String scriptSource) {
			this.scriptSource = scriptSource;
		} 
	}
	
}
