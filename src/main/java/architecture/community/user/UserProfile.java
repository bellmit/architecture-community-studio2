package architecture.community.user;

import java.util.Map;

public interface UserProfile extends java.io.Serializable {

	public long getUserId();

	public String getUsername();

	public String getName(); 
	
	public String getEmail();
	
	public Map<String, Object> getProfileAsMap();
	
}
