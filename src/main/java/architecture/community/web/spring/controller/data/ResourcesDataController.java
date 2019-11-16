package architecture.community.web.spring.controller.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import architecture.community.attachment.Attachment;
import architecture.community.attachment.AttachmentService;
import architecture.community.attachment.DefaultAttachment;
import architecture.community.exception.NotFoundException;
import architecture.community.exception.UnAuthorizedException;
import architecture.community.image.DefaultImage;
import architecture.community.image.Image;
import architecture.community.image.ImageLink;
import architecture.community.image.ImageService;
import architecture.community.model.Models;
import architecture.community.query.CustomQueryService;
import architecture.community.share.SharedLink;
import architecture.community.share.SharedLinkService;
import architecture.community.user.User;
import architecture.community.util.SecurityHelper;
import architecture.community.web.model.DataSourceRequest;
import architecture.community.web.model.ItemList;
import architecture.community.web.model.Result;

@Controller("community-resources-data-controller")
public class ResourcesDataController {

	
	@Inject
	@Qualifier("attachmentService")
	private AttachmentService attachmentService;
	
	@Autowired(required = false) 
	@Qualifier("customQueryService")
	private CustomQueryService customQueryService;
	
	@Autowired(required = false) 
	@Qualifier("sharedLinkService")
	private SharedLinkService sharedLinkService;	
	
	@Autowired
	@Qualifier("imageService") 
	private ImageService imageService;
 
	private Logger log = LoggerFactory.getLogger(ResourcesDataController.class);
 
    @RequestMapping(value = "/data/files/0/upload.json", method = RequestMethod.POST)
    @ResponseBody
    public List<Attachment> uploadFiles (
    		@RequestParam(value = "objectType", defaultValue = "-1", required = false) Integer objectType,
    		@RequestParam(value = "objectId", defaultValue = "-1", required = false) Long objectId,
    		@RequestParam(value = "attachmentId", defaultValue = "-1", required = false) Long attachmentId,
    		@RequestParam(value = "shared", defaultValue = "true", required = false) Boolean shared,
    	    MultipartHttpServletRequest request ) throws NotFoundException, IOException, UnAuthorizedException {
		
		User user = SecurityHelper.getUser();
		if(user.isAnonymous())
			throw new UnAuthorizedException("No Authorized. Please signin first.");
		
		List<Attachment> list = new ArrayList<Attachment>();
		Iterator<String> names = request.getFileNames(); 
		while (names.hasNext()) {
		    String fileName = names.next();
		    MultipartFile mpf = request.getFile(fileName);
		    InputStream is = mpf.getInputStream();
		    
		    log.debug("upload - file:{}, size:{}, type:{} ", mpf.getOriginalFilename(), mpf.getSize() , mpf.getContentType() );
		    Attachment attachment ;
		    if(attachmentId > 0)
		    {
			    attachment = attachmentService.getAttachment(attachmentId);
			    attachment.setContentType(mpf.getContentType());
			    attachment.setInputStream(is);
			    attachment.setSize((int) mpf.getSize());
			    attachment.setName(mpf.getOriginalFilename());
		    }else {
		    	attachment = attachmentService.createAttachment(objectType, objectId, mpf.getOriginalFilename(), mpf.getContentType(), is, (int) mpf.getSize());
		    }
		    attachment.setUser(user);		
		    attachmentService.saveAttachment(attachment);
		    
		    if( shared ) {
		    	SharedLink link = sharedLinkService.getSharedLink(Models.ATTACHMENT.getObjectType(), attachment.getAttachmentId(), shared);
		    	((DefaultAttachment) attachment ).setSharedLink(link);
		    } 
		    list.add(attachment);
		}			
		return list;
	}
	
    
    @RequestMapping(value = "/data/files/{attachmentId:[\\p{Digit}]+}/get.json", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public Attachment getAttachment (
		@PathVariable Long attachmentId,  
		NativeWebRequest request) throws NotFoundException { 
		
		Attachment attachment = 	attachmentService.getAttachment(attachmentId);
		try {
			SharedLink link = sharedLinkService.getSharedLink(Models.ATTACHMENT.getObjectType(), attachment.getAttachmentId());
			((DefaultAttachment) attachment ).setSharedLink(link);
		} catch (Exception ignore) {}
		return attachment;
	}
	
    
	 
	@RequestMapping(value = "/data/files/{attachmentId:[\\p{Digit}]+}/delete.json", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public Result removeFile (
		@PathVariable Long attachmentId,
		@RequestBody DataSourceRequest dataSourceRequest, 
		NativeWebRequest request) throws NotFoundException, UnAuthorizedException {
		
		User me = SecurityHelper.getUser(); 
		Attachment attachment = 	attachmentService.getAttachment(attachmentId);
		if( isAllowed( attachment.getUser(),  me) ) {
			attachmentService.removeAttachment(attachment);
			try {
				SharedLink link = sharedLinkService.getSharedLink(Models.ATTACHMENT.getObjectType(), attachment.getAttachmentId());
				sharedLinkService.removeSharedLink(link.getLinkId());
			} catch (Exception e) { 
			}
		}else {
			throw new UnAuthorizedException();
		} 
		return Result.newResult();
	}	
	
	@RequestMapping(value = "/data/images/0/upload.json", method = RequestMethod.POST)
    @ResponseBody
    public ItemList uploadImages( 
    		@RequestParam(value = "objectType", defaultValue = "-1", required = false) Integer objectType,
    		@RequestParam(value = "objectId", defaultValue = "-1", required = false) Long objectId,
    		@RequestParam(value = "imageId", defaultValue = "-1", required = false) Long imageId,
    		@RequestParam(value = "shared", defaultValue = "true", required = false) Boolean shared,
    		MultipartHttpServletRequest request) throws NotFoundException, IOException, UnAuthorizedException { 
		
		User user = SecurityHelper.getUser();
		if(user.isAnonymous())
			throw new UnAuthorizedException("No Authorized. Please signin first.");
		
		List<Image> images = new ArrayList<Image>(); 
		Iterator<String> names = request.getFileNames();		
		while (names.hasNext()) {
		    String fileName = names.next();
		    MultipartFile mpf = request.getFile(fileName);
		    InputStream is = mpf.getInputStream();
		    log.debug("upload file:{}, size:{}, type:{} ", mpf.getOriginalFilename(), mpf.getSize() , mpf.getContentType() ); 
		    Image image ;
		    if( imageId > 0) {
		    	image = imageService.getImage(imageId); 
		    	((DefaultImage)image).setContentType(mpf.getContentType());
		    	((DefaultImage)image).setInputStream(is);
		    	image.setSize((int) mpf.getSize());
		    	((DefaultImage)image).setName(mpf.getOriginalFilename());
		    }else {
		    	image = imageService.createImage(objectType, objectId, mpf.getOriginalFilename(), mpf.getContentType(), is, (int) mpf.getSize());
		    }
		    image.setUser(user);		    
		    imageService.saveImage(image); 
		    if( shared ) {
				try {
					imageService.getImageLink(image, true);
					ImageLink link = imageService.getImageLink(image);
					((DefaultImage)image).setImageLink( link );
				} catch (Exception ignore) { 
				} 
		    }
			images.add(image);
		}
		
		return new ItemList(images, images.size() ); 
    }
	 
	@RequestMapping(value = "/data/images/{imageId:[\\p{Digit}]+}/get.json", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public Image getImage (
		@PathVariable Long imageId, 
		@RequestParam(value = "fields", defaultValue = "none", required = false) String fields,
		NativeWebRequest request) throws NotFoundException { 
		
		Image image = 	imageService.getImage(imageId);
		try {
			ImageLink link = imageService.getImageLink(image);
			((DefaultImage)image).setImageLink( link );
		} catch (Exception ignore) { 
		}
		return image;
	}	 
	 
	@RequestMapping(value = "/data/images/{imageId:[\\p{Digit}]+}/delete.json", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public Result removeImage (
		@PathVariable Long imageId,
		@RequestBody DataSourceRequest dataSourceRequest, 
		NativeWebRequest request) throws NotFoundException, UnAuthorizedException {
		
		User me = SecurityHelper.getUser();
		
		Image image = 	imageService.getImage(imageId);
		if( isAllowed( image.getUser(),  me) ) {
			imageService.deleteImage(image);
		}else {
			throw new UnAuthorizedException();
		} 
		return Result.newResult();
	}	
	
	
	private boolean isAllowed(User owner, User me) {
		boolean isAllowed = false;
		if( SecurityHelper.isUserInRole("ROLE_ADMINISTRATOR,ROLE_SYSTEM,ROLE_DEVELOPER,ROLE_OPERATOR"))
			return true;
		if( owner.getUserId() > 0 && me.getUserId() > 0 &&  owner.getUserId() == me.getUserId() ) {
			isAllowed = true;
		}
		return true;
	}
}