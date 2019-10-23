package architecture.community.model;

import architecture.community.attachment.Attachment;
import architecture.community.category.Category;
import architecture.community.comment.Comment;
import architecture.community.image.Image;
import architecture.community.image.LogoImage;
import architecture.community.navigator.menu.Menu;
import architecture.community.navigator.menu.MenuItem;
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
	COMPANY(3, Company.class),  
	ROLE(4, Role.class),
	CATEGORY(5, Category.class),
	COMMENT(8, Comment.class),
	ATTACHMENT(10, Attachment.class),
	IMAGE(11, Image.class),
	LOGO_IMAGE(12, LogoImage.class),
	AVATAR_IMAGE(13, AvatarImage.class),
	PAGE(14, Page.class),
	TAG(16, ContentTag.class),  
	MENU(15, Menu.class),  
	MENU_ITEM(16, MenuItem.class),  
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


