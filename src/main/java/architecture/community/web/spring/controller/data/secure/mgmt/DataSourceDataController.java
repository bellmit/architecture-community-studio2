package architecture.community.web.spring.controller.data.secure.mgmt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;

import architecture.community.services.CommunityAdminService;
import architecture.community.web.model.ItemList;
import architecture.ee.component.editor.DataSourceConfig;
import architecture.ee.component.editor.DataSourceConfigReader;
import architecture.ee.service.ApplicationProperties;
import architecture.ee.service.ConfigService;
import architecture.ee.service.Repository;
import architecture.ee.util.ApplicationConstants;

@Controller("community-mgmt-datasource-secure-data-controller")
@RequestMapping("/data/secure/mgmt")
public class DataSourceDataController {

	@Autowired( required = true) 
	@Qualifier("repository")
	private Repository repository;
	
	@Inject
	@Qualifier("configService")
	private ConfigService configService;
	
	@Inject
	@Qualifier("adminService")
	private CommunityAdminService adminService;
	
	public DataSourceDataController() { 
	
	}

	private DataSourceConfigReader getDataSourceConfigReader() {
		return new DataSourceConfigReader(repository.getSetupApplicationProperties());
	}
	
	@Secured({ "ROLE_ADMINISTRATOR", "ROLE_SYSTEM", "ROLE_DEVELOPER"})
	@RequestMapping(value = "/datasoruce/config/names.json", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ItemList getDataSourceNames( NativeWebRequest request){  
		
		ApplicationProperties config = repository.getSetupApplicationProperties();
		Collection<String> names = config.getChildrenNames(ApplicationConstants.DATABASE_PROP_NAME); 
		List<String> list = new ArrayList<String>();
		for( String name : names ) {
			list.add(name);
		} 
		return new ItemList(list, list.size());
	}
	
	@Secured({ "ROLE_ADMINISTRATOR", "ROLE_SYSTEM", "ROLE_DEVELOPER"})
	@RequestMapping(value = "/datasource/config/{name}/get.json", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public DataSourceConfig getDataSourcConfig(@PathVariable String name, NativeWebRequest request){
		return getDataSourceConfigReader().getDataSoruceConfig(name);
	}
	
	@Secured({ "ROLE_ADMINISTRATOR", "ROLE_SYSTEM", "ROLE_DEVELOPER"})
	@RequestMapping(value = "/datasource/config/list.json", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public List<DataSourceConfig> getDataSourcConfig(NativeWebRequest request){
		ApplicationProperties config = repository.getSetupApplicationProperties();
		Collection<String> names = config.getChildrenNames(ApplicationConstants.DATABASE_PROP_NAME); 
		List<DataSourceConfig> list = new ArrayList<DataSourceConfig>();
		DataSourceConfigReader reader = getDataSourceConfigReader();
		for( String name : names ) {
			DataSourceConfig dsc = reader.getDataSoruceConfig(name);
			dsc.setActive(adminService.isExists(dsc.getBeanName(), DataSource.class)); 
			list.add(dsc);
			
		}
		return list;
	}
	
}
