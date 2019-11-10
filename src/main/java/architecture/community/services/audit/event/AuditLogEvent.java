package architecture.community.services.audit.event;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationEvent;
import org.springframework.security.core.Authentication;

import architecture.community.security.spring.userdetails.CommuintyUserDetails;
import architecture.community.user.User;
import architecture.community.util.SecurityHelper;
import architecture.ee.jdbc.sqlquery.mapping.ParameterMapping.Builder;

public class AuditLogEvent extends ApplicationEvent  {

	public static String CREATE_ACTION = "create";
	public static String UPDATE_ACTION = "update";
	public static String DELETE_ACTION = "delete";
	public static String READ_ACTION = "read";
	
	private User actor ;
	
	private String objectLabel;
	
	private Integer objectType;
	
	private Long objectId;
	
	private String action;
	
	private String description;
	
	private String url;
	
	private String ip ;
	
	private String userAgent;  
	
	public AuditLogEvent(Object source) {
		super(source); 
	}

	
	public User getActor() {
		return actor;
	}


	public void setActor(User actor) {
		this.actor = actor;
	}


	public String getObjectLabel() {
		return objectLabel;
	}


	public void setObjectLabel(String objectLabel) {
		this.objectLabel = objectLabel;
	}


	public Integer getObjectType() {
		return objectType;
	}


	public void setObjectType(Integer objectType) {
		this.objectType = objectType;
	}


	public Long getObjectId() {
		return objectId;
	}


	public void setObjectId(Long objectId) {
		this.objectId = objectId;
	}


	public String getAction() {
		return action;
	}


	public void setAction(String action) {
		this.action = action;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public String getUrl() {
		return url;
	}


	public void setUrl(String url) {
		this.url = url;
	}


	public String getIp() {
		return ip;
	}


	public void setIp(String ip) {
		this.ip = ip;
	}


	public String getUserAgent() {
		return userAgent;
	}


	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

 
	public String toString() {
		StringBuilder builder2 = new StringBuilder();
		builder2.append("AuditLogEvent [");
		if (actor != null)
			builder2.append("actor=").append(actor).append(", ");
		if (objectLabel != null)
			builder2.append("objectLabel=").append(objectLabel).append(", ");
		if (objectType != null)
			builder2.append("objectType=").append(objectType).append(", ");
		if (objectId != null)
			builder2.append("objectId=").append(objectId).append(", ");
		if (action != null)
			builder2.append("action=").append(action).append(", ");
		if (description != null)
			builder2.append("description=").append(description).append(", ");
		if (url != null)
			builder2.append("url=").append(url).append(", ");
		if (ip != null)
			builder2.append("ip=").append(ip).append(", ");
		if (userAgent != null)
			builder2.append("userAgent=").append(userAgent);
		builder2.append("]");
		return builder2.toString();
	}


	public static class Builder {
		
		private AuditLogEvent event ;
		
		public Builder(HttpServletRequest request, HttpServletResponse response, Object source) {
			
			this.event = new AuditLogEvent(source);
			if( source != null && source instanceof Authentication ) {
				event.actor = getUser((Authentication)source);
			}
			event.ip = request.getRemoteAddr();
			event.url = request.getRequestURI();
			event.userAgent = request.getHeader("User-Agent"); 
		}
		
		public AuditLogEvent build() {
			return event;
		}
		

		public Builder label(String label) {
			event.action = label;
			return this;
		}
		
		public Builder object(int objectType, long objectId) {
			event.objectType = objectType;
			event.objectId = objectId;
			return this;
		}
		public Builder action(String action) {
			event.action = action;
			return this;
		}
		
		public Builder description(String description) {
			event.description = description;
			return this;
		}
		
		private User getUser(Authentication authen) {
			if(authen == null) {
				return SecurityHelper.getUser();
			}
			try {
			    Object obj = authen.getPrincipal();
			    if (obj instanceof CommuintyUserDetails)
				return ((CommuintyUserDetails) obj).getUser();
			} catch (Exception ignore) {
				
			}
			return SecurityHelper.ANONYMOUS;
		}
	}
}
