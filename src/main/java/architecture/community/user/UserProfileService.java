package architecture.community.user;

public interface UserProfileService {
 
	public <T> T  getUserProfile( User user , Class<T> elementType ) ;
	
	public void saveOrUpdate ( User user , Object profile);
	
}
