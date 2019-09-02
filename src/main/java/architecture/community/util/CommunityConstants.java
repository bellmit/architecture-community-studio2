package architecture.community.util;

import java.io.File;

public class CommunityConstants {
	
	/** SECURITY PROPERTY KEY */
    public static final String SECURITY_AUTHENTICATION_ENCODING_ALGORITHM_PROP_NAME = "security.authentication.encoding.algorithm";
    public static final String SECURITY_AUTHENTICATION_ENCODING_SALT_PROP_NAME = "security.authentication.encoding.salt";
    public static final String SECURITY_AUTHENTICATION_AUTHORITY_PROP_NAME = "security.authentication.authority";
    
    /** LOCALE PROPERTY KEY */
    public static final String LOCALE_LANGUAGE_PROP_NAME = "locale.language";
    public static final String LOCALE_COUNTRY_PROP_NAME = "locale.country";
    public static final String LOCALE_CHARACTER_ENCODING_PROP_NAME = "locale.characterEncoding";
    public static final String LOCALE_TIMEZONE_PROP_NAME = "locale.timeZone";
    
    /** VIER RENDER */
    public static final String VIEW_RENDER_FREEMARKER_PROP_NAME = "view.render.freemarker";
    public static final String VIEW_RENDER_FREEMARKER_DEBUG_PROP_NAME = "view.render.freemarker.debug";
    public static final String VIEW_RENDER_FREEMARKER_TEMPLATE_LOCATION_PROP_NAME = "view.render.freemarker.template.location";    
    public static final String VIEW_RENDER_FREEMARKER_VARIABLES_PROP_NAME = "view.render.freemarker.freemarkerVariables";
    
    public static final String VIEW_RENDER_JSP_LOCATION_PROP_NAME = "view.render.jsp.location";    
    
    public static final String FREEMARKER_TEMPLATE_UPDATE_DELAY_PROP_NAME = "framework.freemarker.templateUpdateDelay";
    public static final String FREEMARKER_LOG_ERROR_PROP_NAME = "framework.freemarker.logError";
    public static final String FREEMARKER_STRONG_TEMPLATE_CACHE_SIZE_PROP_NAME = "framework.freemarker.strongTemplateCacheSize";
    public static final String FREEMARKER_WEAK_TEMPLATE_CACHE_SIZE_PROP_NAME = "framework.freemarker.weakTemplateCacheSize";


	public enum Platform {
		WINDOWS(';'), UNIX(':');
		public final char pathSeparator;
		private Platform(char pathSeparator) {
			this.pathSeparator = pathSeparator;
		}
		public static Platform current() {
			if (File.pathSeparatorChar == ':')
				return UNIX;
			return WINDOWS;
		}
	}
}
