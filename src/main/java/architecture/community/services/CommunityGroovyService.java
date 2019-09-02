package architecture.community.services;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.inject.Inject;

import org.apache.commons.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scripting.ScriptFactory;
import org.springframework.scripting.ScriptSource;
import org.springframework.scripting.groovy.GroovyScriptFactory;
import org.springframework.scripting.support.RefreshableScriptTargetSource;
import org.springframework.scripting.support.ResourceScriptSource;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

import architecture.ee.exception.RuntimeError;
import architecture.ee.service.Repository;

public class CommunityGroovyService implements InitializingBean, ResourceLoaderAware , ApplicationContextAware {
	
	public static final String INLINE_SCRIPT_PREFIX = "inline:";
	
	public static final String JDBC_SCRIPT_PREFIX = "jdbc:";
	
	private ResourceLoader resourceLoader;

	protected Logger log = LoggerFactory.getLogger(getClass().getName());
	 
	protected ReentrantLock lock = new ReentrantLock();
	
	private ApplicationContext applicationContext = null;
	 
	private com.google.common.cache.LoadingCache<ScriptServiceKey, Object> scriptServiceCache = null;
	
	@Inject
	@Qualifier("repository")
	private Repository repository;
	
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}
	
	/** Map from bean name String to ScriptSource object */
	private final Map<String, ScriptSource> scriptSourceCache = new HashMap<String, ScriptSource>();
	
	private File scriptDir;	
	
	public CommunityGroovyService() {
		
	} 
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	public void initialize(){		
		log.debug("creating script service cache ...");		
		scriptServiceCache = CacheBuilder.newBuilder().maximumSize(5000).expireAfterAccess(24, TimeUnit.HOURS ).build(		
			new CacheLoader<ScriptServiceKey, Object>(){			
				public Object load(ScriptServiceKey key) throws Exception {
					log.debug("creating new refreshable script service.");		
					return getefreshableService(key.scriptSourceLocator, key.requiredType);
				}
			}
		);
	}	
	
 
	public void afterPropertiesSet() throws Exception {
		initialize();
	} 
	
	protected synchronized File getScriptDir() {
		if(scriptDir == null)
        {
			scriptDir = repository.getFile("groovy-script");
			if(!scriptDir.exists())
            {
                boolean result = scriptDir.mkdir();
                if(!result)
                	log.error((new StringBuilder()).append("Unable to create script directory: '").append(scriptDir).append("'").toString());
            }
        }
        return scriptDir;
	}
	
	protected ScriptSource createScriptSource(String fileName) {
		return new ResourceScriptSource( getScriptResource(fileName) );
	}
	
	protected Resource getScriptResource(String fileName) { 
		File file = new File(getScriptDir(), fileName );
		FileSystemResource resource = new FileSystemResource( file ) ;
		return resource; 
	}
	
	
	protected ScriptSource getScriptSource(String name) {
		synchronized (this.scriptSourceCache) {
			ScriptSource scriptSource = this.scriptSourceCache.get(name);
			if (scriptSource == null) {
				scriptSource = createScriptSource(name);
				this.scriptSourceCache.put(name, scriptSource);
			}
			return scriptSource;
		}
	}
	
	
	protected RefreshableScriptTargetSource getRefreshableScriptTargetSource(BeanFactory beanFactory, String beanName, ScriptFactory scriptFactory, ScriptSource scriptSource, boolean isFactoryBean) {  
		RefreshableScriptTargetSource rsts = new RefreshableScriptTargetSource(beanFactory, beanName, scriptFactory, scriptSource, isFactoryBean) {
			protected Object obtainFreshBean(BeanFactory beanFactory, String beanName) {
				/*
				 * we ask the factory to create a new script bean directly instead
				 * asking the beanFactory for simplicity. 
				 */
				try {
					return scriptFactory.getScriptedObject(scriptSource);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};
		rsts.setRefreshCheckDelay(1000L);	 
		return rsts;
	}
	
	private GroovyScriptFactory getGroovyScriptFactory(String scriptSourceLocator) {
		GroovyScriptFactory factory = new GroovyScriptFactory(scriptSourceLocator); 
		return factory;
	}
	
	
	public <T> T getService(String scriptSourceLocator, Class<T> requiredType, boolean refreshable ) {	
		
		if( refreshable )
			try {
				return (T) scriptServiceCache.get( new ScriptServiceKey( scriptSourceLocator, requiredType ) );
			} catch (ExecutionException e) {
				throw new RuntimeError(e);
			}
		else
			return getService(scriptSourceLocator, requiredType);
	}
	 
	
	public <T> T getService(String scriptSourceLocator, Class<T> requiredType) { 
		GroovyScriptFactory factory = getGroovyScriptFactory(scriptSourceLocator);
		AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
		beanFactory.autowireBean(factory);
		ResourceScriptSource script = new ResourceScriptSource(getScriptResource(scriptSourceLocator));
		try { 
			T obj = (T) factory.getScriptedObject(script, requiredType);
			applicationContext.getAutowireCapableBeanFactory().autowireBean(obj);
			return obj;
		} catch (Exception e) { 
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	
	public <T> T getefreshableService(String scriptSourceLocator, Class<T> requiredType) {  
		GroovyScriptFactory factory = getGroovyScriptFactory(scriptSourceLocator);
		AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
		beanFactory.autowireBean(factory);
		
		ResourceScriptSource script = new ResourceScriptSource(getScriptResource(scriptSourceLocator));
		RefreshableScriptTargetSource rsts = getRefreshableScriptTargetSource(beanFactory, "groovy_script_servcie____", factory , script, false); 
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setTargetSource(rsts);
		proxyFactory.setInterfaces(requiredType); 
		DelegatingIntroductionInterceptor introduction = new DelegatingIntroductionInterceptor(rsts);
		introduction.suppressInterface(TargetSource.class); 
		proxyFactory.addAdvice(introduction);  
		T obj =  (T) proxyFactory.getProxy(); 
		applicationContext.getAutowireCapableBeanFactory().autowireBean(obj);		
		return obj;	
	}
	
	
	static class ScriptServiceKey implements Serializable {
		String scriptSourceLocator ;
		Class<?> requiredType ; 
		boolean refreshable;
		
		public ScriptServiceKey(String scriptSourceLocator, Class<?> requiredType) { 
			this.scriptSourceLocator = scriptSourceLocator;
			this.requiredType = requiredType;
			this.refreshable = true;
		}

		public String getScriptSourceLocator() {
			return scriptSourceLocator;
		} 
		
		public void setScriptSourceLocator(String scriptSourceLocator) {
			this.scriptSourceLocator = scriptSourceLocator;
		} 

		public Class<?> getRequiredType() {
			return requiredType;
		} 

		public void setRequiredType(Class<?> requiredType) {
			this.requiredType = requiredType;
		} 

 
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((scriptSourceLocator == null) ? 0 : scriptSourceLocator.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ScriptServiceKey other = (ScriptServiceKey) obj;
			if (scriptSourceLocator == null) {
				if (other.scriptSourceLocator != null)
					return false;
			} else if (!scriptSourceLocator.equals(other.scriptSourceLocator))
				return false;
			return true;
		}

		public boolean equals(ScriptServiceKey obj) {
			if(StringUtils.equals(scriptSourceLocator, obj.scriptSourceLocator) && requiredType == obj.requiredType)
				return true;
			return true;
		}
	}

}
