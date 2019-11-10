package architecture.community.user;

public interface UserProfile extends java.io.Serializable {

	public long getUserId();

	public String getUsername();

	public String getName(); 
	
	public String getEmail();
	
}
