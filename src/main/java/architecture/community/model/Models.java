package architecture.community.model;

import architecture.community.category.Category;
import architecture.community.comment.Comment;
import architecture.community.page.Page;
import architecture.community.page.api.Api;
import architecture.community.tag.ContentTag;
import architecture.community.user.AvatarImage;
import architecture.community.user.Company;
import architecture.community.user.Role;
import architecture.community.user.User;

public enum Models {
	
	UNKNOWN(-1, null), 
	USER(1, User.class), 
	COMPANY(2, Company.class), 
	ROLE(3, Role.class),
	CATEGORY(4, Category.class),
	COMMENT(8, Comment.class),
	PAGE(14, Page.class),
	TAG(15, ContentTag.class), 
	AVATAR_IMAGE(13, AvatarImage.class),
	API(30, Api.class)
	;
	
	private int objectType;
	
	private Class objectClass;
	
	private Models(int objectType, Class clazz) {
		this.objectType = objectType;
		this.objectClass = clazz;
	}
	
	public Class getObjectClass() {
		return objectClass;
	}

	public int getObjectType()
	{
		return objectType;
	} 
	
	public static Models valueOf(int objectType){
		Models selected = Models.UNKNOWN ;
		for( Models m : Models.values() )
		{
			if( m.getObjectType() == objectType ){
				selected = m;
				break;
			}
		}
		return selected;
	}
}


