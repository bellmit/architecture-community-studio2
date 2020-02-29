package architecture.community.user.profile;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import architecture.community.user.UserProfile;

public class CustomUserProfile implements UserProfile {

	private long userId;
	
	private String username;
	
	private String name;
	
	private String email;
	
	private Map<String, Object> data = null;

	public CustomUserProfile(long userId) { 
		this.userId = userId;
		this.data = new HashMap<String, Object>();
	}

	public long getUserId() {
		return userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
 
	public Map<String, Object> getProfileAsMap() { 
		synchronized (this) {
			if (data == null) {
				data = new ConcurrentHashMap<String, Object>();
			}
		}
		return data;
	} 
}
