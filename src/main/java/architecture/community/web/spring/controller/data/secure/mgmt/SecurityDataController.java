package architecture.community.web.spring.controller.data.secure.mgmt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;

import architecture.community.comment.event.CommentEvent.Type;
import architecture.community.exception.NotFoundException;
import architecture.community.query.CustomQueryService;
import architecture.community.user.User;
import architecture.community.user.UserManager;
import architecture.community.user.UserNotFoundException;
import architecture.community.user.UserProvider;
import architecture.community.web.model.DataSourceRequest;
import architecture.community.web.model.ItemList;
import architecture.ee.util.StringUtils; 

@Controller("community-mgmt-security-secure-data-controller")
@RequestMapping("/data/secure/mgmt/security")
public class SecurityDataController {
	
	private Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired(required = false) 
	@Qualifier("customQueryService")
	private CustomQueryService customQueryService;

	
	@Autowired(required = false) 
	@Qualifier("userManager")
	private UserManager userManager;
	
	
	@Autowired(required = false) 
	private List<UserProvider> userProvisers;
	
	@Secured({ "ROLE_ADMINISTRATOR", "ROLE_SYSTEM" })
	@RequestMapping(value = "/users/find.json", method = { RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public ItemList findUsers (
		@RequestBody DataSourceRequest dataSourceRequest,
		NativeWebRequest request) throws NotFoundException {	
				 
		if( dataSourceRequest.getPageSize() == 0)
			dataSourceRequest.setPageSize(30);
		dataSourceRequest.setStatement("COMMUNITY_USER.COUNT_USERS_BY_REQUEST");	
		int totalCount = customQueryService.queryForObject(dataSourceRequest, Integer.class);
		
		//customQueryService.list(dataSourceRequest);
		
		List<User> users = new ArrayList<User>(totalCount);
		if( totalCount > 0) {
			dataSourceRequest.setStatement("COMMUNITY_USER.FIND_USER_IDS_BY_REQUEST");		
			List<Long> userIds = customQueryService.list(dataSourceRequest, Long.class);
			for( Long userId : userIds ) {
				try {
					users.add(userManager.getUser(userId));
				} catch (UserNotFoundException e) {
				}
			}
		}
		return new ItemList(users, totalCount);
	}
	
	
	@Secured({ "ROLE_ADMINISTRATOR", "ROLE_SYSTEM" })
	@RequestMapping(value = "/users/providers/{provider}/list.json", method = { RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public ItemList listUserProvider (
		@PathVariable String name,
		@RequestBody DataSourceRequest dataSourceRequest,
		NativeWebRequest request) throws NotFoundException {	
		
		
		UserProvider provider = getUserProviderByName(name);
		if( provider.getType() == UserProvider.Type.JDBC ) {
			// using query ...
			log.debug("extract user with external '{}'", name );
		}
		
		return provider.findUsers(dataSourceRequest, userManager);
	}
	
	private UserProvider getUserProviderByName(String name) {
		UserProvider provider = null ;
		for(UserProvider p : userProvisers ){
			if(StringUtils.equals(p.getName(), name))
				provider = p;
		}
		return provider;
	} 
	
	@Secured({ "ROLE_ADMINISTRATOR", "ROLE_SYSTEM" })
	@RequestMapping(value = "/users/providers/list.json", method = { RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public ItemList listUserProvider (
		@RequestParam(value = "enabled", defaultValue = "true", required = false) Boolean enabled, 	
		NativeWebRequest request) throws NotFoundException {			 
		
		List<UserProviderInfo> list = new ArrayList<UserProviderInfo>();
		for(UserProvider p : userProvisers ){
			list.add(new UserProviderInfo(p));
		}
		return new ItemList(list, list.size());
	}
	
	
	
	public static class UserProviderInfo implements Serializable {
		
		String name ;
		boolean enabled ;
		boolean paginationable ;
		boolean updatable;
		
		public UserProviderInfo(UserProvider p) {
			this.name = p.getName();
			this.paginationable = p.supportsPagination();
			this.enabled = p.isEnabled();
			this.updatable = p.supportsUpdate();		
		}

		public String getName() {
			return name;
		}

		public boolean isEnabled() {
			return enabled;
		}

		public boolean isPaginationable() {
			return paginationable;
		}

		public boolean isUpdatable() {
			return updatable;
		}
		
		
	}
}