package architecture.community.web.spring.controller.data;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

public class ResourceUtils {


	private static final String JPEG_CONTENT_TYPE = "image/jpeg"; 
	private static final String PNG_CONTENT_TYPE = "png/jpeg"; 
	private static final String IMAGES_NO_THUMBNAIL = "assets/images/no-thumbnail.jpg";
	private static final String IMAGES_NO_AVATAR = "assets/images/no-avatar.png";
	
	public static void noThumbnails(HttpServletRequest request, HttpServletResponse response) throws IOException {
		ClassPathResource resource = new ClassPathResource(IMAGES_NO_THUMBNAIL);
		if( resource.exists() ) {
			InputStream input = resource.getInputStream();
			int length = input.available();
			response.setContentType(JPEG_CONTENT_TYPE);
			response.setContentLength(length);
			IOUtils.copy(input, response.getOutputStream());
			response.flushBuffer(); 
		}
	} 

	public static void noAvatars(HttpServletRequest request, HttpServletResponse response) throws IOException {
		ClassPathResource resource = new ClassPathResource(IMAGES_NO_AVATAR);
		if( resource.exists() ) {
			InputStream input = resource.getInputStream();
			int length = input.available();
			response.setContentType(PNG_CONTENT_TYPE);
			response.setContentLength(length);
			IOUtils.copy(input, response.getOutputStream());
			response.flushBuffer(); 
		}
	} 
}
