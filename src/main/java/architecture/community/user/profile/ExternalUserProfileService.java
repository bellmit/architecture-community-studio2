package architecture.community.user.profile;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.springframework.jdbc.core.RowMapper;

import architecture.community.query.Utils;
import architecture.community.user.UserProfile;

public class ExternalUserProfileService extends AbstractUserProfileService {
 
	protected UserProfile loadUserProfile(Long userId) throws Exception {
		if(isEnabled()) { 
			customQueryJdbcDao.getJdbcTemplate().query(
				"SELECT * FROM DUAL", 
				new RowMapper<CustomUserProfile>(){ 
					public CustomUserProfile mapRow(ResultSet rs, int rowNum) throws SQLException { 
						return null;
					}}, 
				Utils.newSqlParameterValue(Types.NUMERIC, userId));
		}
		return null;
	}
 
	protected void saveOrUpdate(UserProfile profile) {
		if(isEnabled()) {
			customQueryJdbcDao.getJdbcTemplate().update(
				"", 
				Utils.newSqlParameterValue(Types.NUMERIC, profile.getUserId())); 
			
		}
	} 
	
	public static class CustomUserProfile implements UserProfile, Serializable {
		
		private long userId;
		
		private String username;
		
		private String name;
		
		private String email;
		
		private String zipCode;
		
		public long getUserId() { 
			return userId;
		}
 
		public String getUsername() { 
			return username;
		} 
		
		public String getName() { 
			return name;
		}
 
		public String getEmail() { 
			return email;
		} 
		
	}
	
}
