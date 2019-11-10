package architecture.community.user.profile;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

import architecture.community.exception.NotFoundException;
import architecture.community.query.dao.CustomQueryJdbcDao;
import architecture.community.user.User;
import architecture.community.user.UserProfile;
import architecture.community.user.UserProfileService;
import architecture.ee.service.ConfigService;

public abstract class AbstractUserProfileService implements UserProfileService , InitializingBean {
	
	protected Logger log = LoggerFactory.getLogger(getClass());
	
	@Inject
	@Qualifier("configService")
	ConfigService configService;	
	
	@Autowired(required=false)
	@Qualifier("customQueryJdbcDao")
	protected CustomQueryJdbcDao customQueryJdbcDao ;
	
	private com.google.common.cache.LoadingCache<Long, UserProfile> profileCache = null;
	 
	public boolean isCacheable() {
		return configService.getApplicationBooleanProperty("services.user.profile.cacheable", false);
	}

	public void setCacheable(boolean cacheable) {
		configService.setApplicationProperty("services.user.profile.cacheable", Boolean.toString(cacheable));
	}

	public void setEnabled(boolean enabled) {
		configService.setApplicationProperty("services.user.profile.enabled", Boolean.toString(enabled));
	}

	public boolean isEnabled() {
		return configService.getApplicationBooleanProperty("services.user.profile.enabled", false);
	}	
	
	public void afterPropertiesSet() throws Exception { 
		log.debug("Creating cache for User Profile.");
		if( isCacheable() ) {
			createCacheIfNotExist();
		}
	}
	
	private void createCacheIfNotExist() {
		if( profileCache == null)
		profileCache = CacheBuilder.newBuilder().maximumSize(5000).expireAfterAccess( 10, TimeUnit.MINUTES).build(		
			new CacheLoader<Long, UserProfile>(){			
				public UserProfile load(Long userId) throws Exception {
					return loadUserProfile(userId);
			}}
		);
	}
	
	public void refresh () {
		
		createCacheIfNotExist();
		
		if( isCacheable()  && profileCache != null ) {  
			profileCache.invalidateAll(); 
		}
		
	} 
	
	public UserProfile getUserProfile(User user) throws NotFoundException { 
		try {
			if( isCacheable() )
				return profileCache.get(user.getUserId());
			else
				return loadUserProfile (user.getUserId()); 
		} catch (Exception e) {
			String msg = (new StringBuilder()).append("Unable to find profile object by ").append(user.getUserId()).toString(); 
			throw new NotFoundException(msg, e);
		}
	} 
	 
	public void saveOrUpdate(User user, UserProfile profile) {  
		saveOrUpdate(profile);
		if( isCacheable()  && profileCache != null )
			profileCache.invalidate(user.getUserId());
	}	

	protected abstract UserProfile loadUserProfile( Long userId ) throws  Exception;	
	
	protected abstract void saveOrUpdate( UserProfile profile ) ; 
	
}
