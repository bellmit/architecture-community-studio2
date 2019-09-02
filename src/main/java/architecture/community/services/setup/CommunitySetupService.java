package architecture.community.services.setup;

import java.util.Properties;

import org.apache.poi.ss.formula.functions.T;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.scripting.groovy.GroovyScriptFactory;
import org.springframework.scripting.support.RefreshableScriptTargetSource;
import org.springframework.scripting.support.ResourceScriptSource;

import architecture.ee.component.editor.DataSourceEditor.PooledDataSourceConfig;
import architecture.ee.spring.jdbc.datasource.DataSourceFactoryBean;

public class CommunitySetupService implements ApplicationContextAware {

	private ApplicationContext applicationContext = null;
	

	
	public CommunitySetupService() { 
		
		
	}
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
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
		
		/*
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(DriverManagerDataSource.class);
		builder.addPropertyValue("url", connectionProperties.getProperty("url"));
		builder.addPropertyValue("url", connectionProperties.getProperty("url"));
		builder.addPropertyValue("url", connectionProperties.getProperty("url"));
		builder.addPropertyValue("url", connectionProperties.getProperty("url"));
		*/
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
}
