package architecture.community.audit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.Assert;

import architecture.community.query.CustomQueryService;
import architecture.community.query.CustomTransactionCallback;
import architecture.community.query.CustomTransactionCallbackWithoutResult;
import architecture.community.query.Utils;
import architecture.community.query.dao.CustomQueryJdbcDao;

public class JdbcAuditTrailService extends AbstractStringAuditTrailService{

	private static final int DEFAULT_COLUMN_LENGTH = 100;
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired(required = false)
	@Qualifier("customQueryService")
	private CustomQueryService customQueryService;
	
	@Async
	public void record(final AuditActionContext auditActionContext) {  
		try { 
		customQueryService.execute(new CustomTransactionCallbackWithoutResult() { 
			protected void doInTransactionWithoutResult(CustomQueryJdbcDao dao) {  				
				final String userId = auditActionContext.getPrincipal().length() <= DEFAULT_COLUMN_LENGTH ? auditActionContext.getPrincipal() : auditActionContext.getPrincipal().substring(0, DEFAULT_COLUMN_LENGTH);
                final String resource = auditActionContext.getResourceOperatedUpon().length() <= DEFAULT_COLUMN_LENGTH ? auditActionContext.getResourceOperatedUpon() : auditActionContext.getResourceOperatedUpon().substring(0, DEFAULT_COLUMN_LENGTH);
                final String action = auditActionContext.getActionPerformed().length() <= DEFAULT_COLUMN_LENGTH ? auditActionContext.getActionPerformed() : auditActionContext.getActionPerformed().substring(0, DEFAULT_COLUMN_LENGTH);
				
                dao.getJdbcTemplate().update(
                    "INSERT INTO AC_UI_AUDIT_TRAIL (AUD_USER, AUD_CLIENT_IP, AUD_SERVER_IP, AUD_RESOURCE, AUD_ACTION, APPLIC_CD, AUD_DATE) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    Utils.newSqlParameterValue(Types.VARCHAR, userId),
					Utils.newSqlParameterValue(Types.VARCHAR, auditActionContext.getClientIpAddress()),
					Utils.newSqlParameterValue(Types.VARCHAR, auditActionContext.getServerIpAddress()),
					Utils.newSqlParameterValue(Types.VARCHAR, resource),
					Utils.newSqlParameterValue(Types.VARCHAR, action),
					Utils.newSqlParameterValue(Types.VARCHAR, auditActionContext.getApplicationCode()),
					Utils.newSqlParameterValue(Types.TIMESTAMP, auditActionContext.getWhenActionWasPerformed())
				);
			}
		});
		}catch(Exception ignore) {
			logger.warn("Error ...", ignore);
		}
	}
 
	public List<? extends AuditActionContext> getAuditRecordsSince(LocalDate sinceDate) { 
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00.000000");
        return getAuditRecordsSince(sinceDate.format(formatter));
	} 
	
	private List<? extends AuditActionContext> getAuditRecordsSince(final String sinceDate) { 
		return customQueryService.execute(new CustomTransactionCallback<List<AuditActionContext>>() {   
			public List<AuditActionContext> doInTransaction(CustomQueryJdbcDao dao) throws DataAccessException {  
				return dao.getJdbcTemplate().query(
						
					"SELECT * FROM AC_UI_AUDIT_TRAIL WHERE AUD_DATE>=? ORDER BY AUD_DATE DESC", 
					new RowMapper<AuditActionContext>() { 
						public AuditActionContext mapRow(ResultSet rs, int rowNum) throws SQLException {  
							final String principal = rs.getString("AUD_USER");
			                final String resource = rs.getString("AUD_RESOURCE");
			                final String clientIp = rs.getString("AUD_CLIENT_IP");
			                final String serverIp = rs.getString("AUD_SERVER_IP");
			                final Date audDate = rs.getDate("AUD_DATE");
			                final String appCode = rs.getString("APPLIC_CD");
			                final String action = rs.getString("AUD_ACTION"); 
			                final String userAgent = rs.getString("AUD_CLIENT_USERAGENT"); 
			                Assert.notNull(principal, "AUD_USER cannot be null");
			                Assert.notNull(resource, "AUD_RESOURCE cannot be null");
			                Assert.notNull(clientIp, "AUD_CLIENT_IP cannot be null");
			                Assert.notNull(serverIp, "AUD_SERVER_IP cannot be null");
			                Assert.notNull(audDate, "AUD_DATE cannot be null");
			                Assert.notNull(appCode, "APPLIC_CD cannot be null");
			                Assert.notNull(action, "AUD_ACTION cannot be null"); 
			                
			                final AuditActionContext audit = new AuditActionContext( principal, resource, action, appCode, audDate, clientIp, serverIp, userAgent); 
							
			                return audit;
					}}, 
					Utils.newSqlParameterValue(Types.TIMESTAMP, sinceDate));
			} 
		});
    }	
 
	public void removeAll() { 
		
	} 
	
}
