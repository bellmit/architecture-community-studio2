package architecture.community.services.setup;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import architecture.ee.component.editor.DataSourceEditor.PooledDataSourceConfig;
import architecture.ee.service.ApplicationProperties;
import architecture.ee.service.Repository;
import architecture.ee.spring.jdbc.datasource.DataSourceFactoryBean;

@Service("communitySetupService")
public class CommunitySetupService implements ApplicationContextAware , InitializingBean {
	
	private ApplicationContext applicationContext = null;
	private Logger log = LoggerFactory.getLogger(getClass().getName());
	
	@Autowired(required=true)
	@Qualifier("repository")
	private Repository repository;
	
	public CommunitySetupService() { 
		
	}
	
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
 
	public void afterPropertiesSet() throws Exception {
		log.info("STARTING SETUP.");
		setupDataSources();
		log.info("COMPLETE SETUP.");
	}
	
	
	private ConfigurableListableBeanFactory getConfigurableListableBeanFactory() {
		ConfigurableListableBeanFactory beanFactory = (ConfigurableListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
		return beanFactory;
	}
	
	private BeanDefinitionRegistry getBeanDefinitionRegistry() {
		AutowireCapableBeanFactory factory =  applicationContext.getAutowireCapableBeanFactory();
		BeanDefinitionRegistry registry = (BeanDefinitionRegistry) factory; 
		return registry;
	}

	
	protected void setupDataSources() {
		ApplicationProperties config = repository.getSetupApplicationProperties(); 
		for( String dataSource : config.getChildrenNames("database")) {
			log.info("Setup DataSource : {}.", dataSource );
			createDataSourceIfNotExist( dataSource );
		}
	}
	
	public void createDataSourceIfNotExist(String dataSource ) { 
		log.debug("Checking context contain bean ({}): {}", dataSource , applicationContext.containsBeanDefinition(dataSource));
		if( !applicationContext.containsBeanDefinition(dataSource) ) { 
			String key = String.format( "database.%s" , dataSource ); 
			for( String name : repository.getSetupApplicationProperties().getChildrenNames(key)) {
				if( name.equals("pooledDataSourceProvider")) {
					PooledDataSourceConfig config = new PooledDataSourceConfig();
					config.setExportName(dataSource);
					registerDataSourceBean(config);
				}
			}  
		}
	} 
	
	protected void registerDataSourceBean(PooledDataSourceConfig dataSource) { 
		DataSourceFactoryBean bean = new DataSourceFactoryBean();
		bean.setProfileName(dataSource.getExportName()); 
		BeanDefinitionRegistry registry = getBeanDefinitionRegistry();  
		if(registry.containsBeanDefinition(dataSource.getExportName())) {
			registry.removeBeanDefinition(dataSource.getExportName());
		} 
		GenericBeanDefinition myBeanDefinition = new GenericBeanDefinition();
		MutablePropertyValues mutablePropertyValues = new MutablePropertyValues();
		mutablePropertyValues.add("profileName", dataSource.getExportName());
		myBeanDefinition.setBeanClass(DataSourceFactoryBean.class);
		myBeanDefinition.setPropertyValues(mutablePropertyValues);
		registry.registerBeanDefinition(dataSource.getExportName(), myBeanDefinition); 
	}
	
	protected void registerDataSourceBean(String beanName, Properties connectionProperties) {  
		BeanDefinitionRegistry registry = getBeanDefinitionRegistry();  
		if(registry.containsBeanDefinition(beanName)) {
			registry.removeBeanDefinition(beanName);
		} 
		GenericBeanDefinition myBeanDefinition = new GenericBeanDefinition(); 
		MutablePropertyValues mutablePropertyValues = new MutablePropertyValues();
		mutablePropertyValues.add("url", connectionProperties.getProperty("url"));
		mutablePropertyValues.add("username", connectionProperties.getProperty("username"));
		mutablePropertyValues.add("password", connectionProperties.getProperty("password"));
		mutablePropertyValues.add("connectionProperties", connectionProperties); 
		myBeanDefinition.setBeanClass(DriverManagerDataSource.class);
		myBeanDefinition.setPropertyValues(mutablePropertyValues);
		registry.registerBeanDefinition(beanName, myBeanDefinition); 
	}

}
