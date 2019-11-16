package tests;

import java.util.Properties;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration("WebContent/")
@ContextConfiguration(locations = { 
		"classpath:setup-context.xml"})


public class DataSourceTest {

	private static final Logger logger = LoggerFactory.getLogger(DataSourceTest.class);

	
	@Autowired
	SetupService setupService;
	
	public DataSourceTest() {
	}
	
	@Test
	public void testCreateDataSource(){		
		logger.debug("SETUP {}", setupService );
		Properties connectionProperties = new Properties();
		connectionProperties.setProperty("DriverClassName", "oracle.jdbc.OracleDriver");
		connectionProperties.setProperty("url", "jdbc:oracle:thin:@//222.122.47.196:15213/PODODB");
		connectionProperties.setProperty("username", "U_HELPDESK" );
		connectionProperties.setProperty("password", "podoq23$");   
		setupService.registerDataSourceBean("dataSource", connectionProperties);
		DataSource dataSource = setupService.getComponent("dataSource", DataSource.class);
		setupService.testConnection(dataSource);
	}
	
	
	
}
