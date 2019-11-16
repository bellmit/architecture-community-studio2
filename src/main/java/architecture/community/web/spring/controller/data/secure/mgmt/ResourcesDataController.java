package architecture.community.web.spring.controller.data.secure.mgmt;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.support.ServletContextResourceLoader;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import architecture.community.exception.NotFoundException;
import architecture.community.model.json.JsonDateDeserializer;
import architecture.community.model.json.JsonDateSerializer;
import architecture.community.util.CommunityConstants;
import architecture.community.util.DateUtils;
import architecture.community.web.model.Result;
import architecture.community.web.util.ServletUtils;
import architecture.ee.service.ConfigService;
import architecture.ee.service.Repository;

@Controller("community-mgmt-resources-secure-data-controller")
@RequestMapping("/data/secure/mgmt")
public class ResourcesDataController {
	
	private Logger log = LoggerFactory.getLogger(getClass());

	private static final String[] FILE_EXTENSIONS = new String [] {".ftl", ".jsp", ".xml", ".html", ".groovy"} ;
	
	@Autowired
	@Qualifier("configService")
	private ConfigService configService;

	@Autowired
	private ServletContext servletContext;
	
	@Autowired
	@Qualifier("repository")
	private Repository repository;

	@Autowired
	private ResourceLoader loader ;
	
	public ResourcesDataController() {
		
	}  
	
	protected ResourceLoader getResourceLoader () {
		if( loader == null )
			loader = new ServletContextResourceLoader(servletContext);
		return loader;
	}
	
	
	private ResourceType getResourceType(String name ) {
		return ResourceType.valueOf(name.toUpperCase());
	}

	@Secured({ "ROLE_ADMINISTRATOR", "ROLE_SYSTEM", "ROLE_DEVELOPER"})
	@RequestMapping(value = "/resources/0/get.json", method = { RequestMethod.POST, RequestMethod.GET })
    @ResponseBody
    public FileInfo getContentByPath(
    		@PathVariable String type, 
    		@RequestParam(value = "path", defaultValue = "", required = false) String path, 
    		NativeWebRequest request) throws NotFoundException, IOException {  
		 
		
		Resource resouce = loader.getResource(path); 
		File targetFile = resouce.getFile(); 
		FileInfo fileInfo = new FileInfo(targetFile); 
		fileInfo.setFileContent(targetFile.isDirectory() ? "" : FileUtils.readFileToString(targetFile, "UTF-8"));
		return fileInfo;
    } 	
	
	@Secured({ "ROLE_ADMINISTRATOR", "ROLE_SYSTEM", "ROLE_DEVELOPER"})
	@RequestMapping(value = "/resources/{type}/create.json", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public Result newFolder(
			@PathVariable String type, 
			@RequestParam(value = "path", defaultValue = "", required = false) String path,
			@RequestParam(value = "filename", defaultValue = "false", required = true) String filename,
			@RequestParam(value = "directory", defaultValue = "false", required = false) boolean directory,
			NativeWebRequest request)
		throws NotFoundException {
		
		Result result = Result.newResult() ;
		log.debug("create new folder for '{}', '{}', '{}'", type,  path, filename);
		if (!isValid(type)) {
			throw new IllegalArgumentException();
		} 
		
		ResourceType resourceType = getResourceType(type);
		Resource root = getResourceByType( resourceType , null); 
		 
		try { 
			File fileToUse = root.getFile(); 
			if (StringUtils.isEmpty(path)) { 
				fileToUse = root.getFile();
			}else { 
				fileToUse = new File(root.getFile(), path);
			}
			File newFile = new File( fileToUse, filename);
			if( directory )
				newFile.mkdir();
			else
				newFile.createNewFile();
			
		} catch (IOException e) {
			result.setError(e);
			log.error(e.getMessage());
		}
		
		return result;
	}
	
	@Secured({ "ROLE_ADMINISTRATOR", "ROLE_SYSTEM", "ROLE_DEVELOPER"})
	@RequestMapping(value = "/resources/{type}/list.json", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public List<FileInfo> getResources(
			@PathVariable String type, 
			@RequestParam(value = "path", defaultValue = "", required = false) String path, 
			@RequestParam(value = "recursive", defaultValue = "true", required = false) Boolean recursive, 
			NativeWebRequest request)
		throws NotFoundException {
		
		log.debug("get resources by type '{}' ({})", type,  path);
		if (!isValid(type)) {
			throw new IllegalArgumentException();
		} 
		ResourceType resourceType = getResourceType(type);
		Resource root = getResourceByType( resourceType , null); 
		List<FileInfo> list = new ArrayList<FileInfo>();  
		log.debug("selected resources : {}", root );
		try { 
			File fileToUse = root.getFile(); 
			if (StringUtils.isEmpty(path)) { 
				fileToUse = root.getFile();
			}else { 
				fileToUse = new File(root.getFile(), path);
			}
			
			File[] files = fileToUse.listFiles(new FileFilter() {  
					private IOCase caseSensitivity = IOCase.SENSITIVE; 
					public boolean accept(File file) {  
						final String name = file.getName();
						if( file.isDirectory() ) {
							if( name.startsWith("."))
								return false;
							return true;
						}
				        for (final String suffix : FILE_EXTENSIONS ) {
				            if (caseSensitivity.checkEndsWith(name, suffix)) {
				                return true;
				            }
				        }
				        return false;
					} 
			}); 
			for (File f : files ) { 
				list.add(new FileInfo(root.getFile(), f));
			}
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return list;
	}
	
	@Secured({ "ROLE_ADMINISTRATOR", "ROLE_SYSTEM", "ROLE_DEVELOPER"})
	@RequestMapping(value = "/resources/{type}/get.json", method = { RequestMethod.POST, RequestMethod.GET })
    @ResponseBody
    public FileInfo getContent(
    		@PathVariable String type, 
    		@RequestParam(value = "path", defaultValue = "", required = false) String path, 
    		NativeWebRequest request) throws NotFoundException, IOException {  
		
		ResourceType resourceType = getResourceType(type);
		File targetFile = getResourceByType(resourceType, path).getFile();
		FileInfo fileInfo = new FileInfo(targetFile); 
		fileInfo.setFileContent(targetFile.isDirectory() ? "" : FileUtils.readFileToString(targetFile, "UTF-8")); 
		return fileInfo;
    } 
	/**
	 * 파일 내용을 업데이트 한다. 동일경로에 파일이름 + .[yyyyMMddHHmmss] 형식으로 백업을 생성한 다음 저장한다.
	 * 
	 * @param type template or script 
	 * @param file 파일 경로 및 내용을 포함하는 객제 
	 * @param request
	 * @return
	 * @throws NotFoundException
	 * @throws IOException
	 */
	@Secured({ "ROLE_ADMINISTRATOR", "ROLE_SYSTEM", "ROLE_DEVELOPER"})
	@RequestMapping(value = "/resources/{type}/save-or-update.json", method = { RequestMethod.POST })
    @ResponseBody
    public FileInfo saveOrUpdate(
    		@PathVariable String type, 
    		@RequestParam(value = "backup", defaultValue = "true", required = false) Boolean backup, 
    		@RequestBody FileInfo file, 
    		NativeWebRequest request) throws NotFoundException, IOException {  
		
		ResourceType resourceType = getResourceType(type);
		File target =  getResourceByType(resourceType, file.path ).getFile();
		// backup to filename + .yyyymmddhhmmss .
		if( backup ) {
			File backupFile = new File(target.getParentFile() , target.getName() + "." + DateUtils.toString(new Date()) );  
			FileUtils.copyFile(target, backupFile); 
		}
		FileUtils.writeStringToFile(target, file.fileContent, ServletUtils.DEFAULT_HTML_ENCODING , false);  
		FileInfo fileInfo = new FileInfo(target); 
		fileInfo.setFileContent(target.isDirectory() ? "" : FileUtils.readFileToString(target, "UTF-8"));  
		return fileInfo;
    } 	 
	
	
	
	
    protected String getResourcePathByType(ResourceType type) { 
		String path = null;
		if( ResourceType.TEMPLATE ==  type ) {
			StringBuilder sb = new StringBuilder().append(File.separatorChar).append("WEB-INF").append(File.separatorChar).append("template").append(File.separatorChar).append("ftl");
			path = configService.getApplicationProperty(CommunityConstants.VIEW_RENDER_FREEMARKER_TEMPLATE_LOCATION_PROP_NAME, sb.toString());
		}else if (ResourceType.SQL ==  type) {
			StringBuilder sb = new StringBuilder().append(File.separatorChar).append("WEB-INF").append(File.separatorChar).append("sql");
			path = configService.getApplicationProperty(CommunityConstants.RESOURCES_SQL_LOCATION_PROP_NAME, sb.toString());
		}else if (ResourceType.SCRIPT ==  type) {
			StringBuilder sb = new StringBuilder().append(File.separatorChar).append("WEB-INF").append(File.separatorChar).append("groovy-script");
			path = configService.getApplicationProperty(CommunityConstants.RESOURCES_GROOVY_LOCATION_PROP_NAME, sb.toString());
		}else if (ResourceType.JSP ==  type) {
			StringBuilder sb = new StringBuilder().append(File.separatorChar).append("WEB-INF").append(File.separatorChar).append("jsp");
			path = configService.getApplicationProperty(CommunityConstants.VIEW_RENDER_JSP_LOCATION_PROP_NAME, sb.toString());
		}else if (ResourceType.DECORATOR ==  type) {	
			StringBuilder sb = new StringBuilder().append(File.separatorChar).append("decorators");
			path = configService.getApplicationProperty(CommunityConstants.RESOURCES_DECORATOR_LOCATION_PROP_NAME, sb.toString());
		} 
		return path;
    }
    
	protected Resource getResourceByType(ResourceType type, String filename ) {
		String path = getResourcePathByType(type);  
		StringBuilder sb = new StringBuilder(path);
		if(StringUtils.isNotEmpty(filename)) {
			String filenameToUse = StringUtils.removeStart(filename, "/");
			filenameToUse = StringUtils.removeStart(filename, "\\");
			sb.append(File.separatorChar).append(filenameToUse); 
		}
		return loader.getResource(sb.toString()); 
	} 
 
	private boolean isValid(String type) {  
		if( ResourceType.valueOf(type.toUpperCase()) != null )
			return true;
		return false;
	}  

	public static class FileInfo { 
		private boolean directory;
		private String path;
		private String relativePath;
		private String absolutePath;
		private String name;
		private long size;
		private Date lastModifiedDate;
		private String fileContent; 
		
		public FileInfo() {
			this.directory = false;
		}
		
		public FileInfo(File file) {
			this.lastModifiedDate = new Date(file.lastModified());
			this.name = file.getName();
			this.path = file.getPath();
			this.absolutePath = file.getAbsolutePath();
			this.directory = file.isDirectory();
			if (this.directory) {
				this.size = FileUtils.sizeOfDirectory(file);
			} else {
				this.size = FileUtils.sizeOf(file);
			}
		}

		public FileInfo(File root, File file) {
			this.lastModifiedDate = new Date(file.lastModified());
			this.name = file.getName();
			this.path = StringUtils.removeStart(file.getAbsolutePath(), root.getAbsolutePath());
			this.absolutePath = file.getAbsolutePath();
			this.directory = file.isDirectory();  
			if (this.directory) {
				this.size = FileUtils.sizeOfDirectory(file);
			} else {
				this.size = FileUtils.sizeOf(file);
			}
		}

		/**
		 * @return fileContent
		 */
		public String getFileContent() {
			return fileContent;
		}

		/**
		 * @param fileContent
		 *            설정할 fileContent
		 */
		public void setFileContent(String fileContent) {
			this.fileContent = fileContent;
		}

		/**
		 * @return directory
		 */
		public boolean isDirectory() {
			return directory;
		}

		/**
		 * @param directory
		 *            설정할 directory
		 */
		public void setDirectory(boolean directory) {
			this.directory = directory;
		}

		/**
		 * @return path
		 */
		public String getPath() {
			return path;
		}

		/**
		 * @param path
		 *            설정할 path
		 */
		public void setPath(String path) {
			this.path = path;
		}

		/**
		 * @return absolutePath
		 */
		@JsonIgnore
		public String getAbsolutePath() {
			return absolutePath;
		}

		/**
		 * @param absolutePath
		 *            설정할 absolutePath
		 */
		public void setAbsolutePath(String absolutePath) {
			this.absolutePath = absolutePath;
		}

		/**
		 * @return name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @param name
		 *            설정할 name
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * @return size
		 */
		public long getSize() {
			return size;
		}

		/**
		 * @param size
		 *            설정할 size
		 */
		public void setSize(long size) {
			this.size = size;
		}

		/**
		 * @return lastModifiedDate
		 */
		@JsonSerialize(using = JsonDateSerializer.class)
		public Date getLastModifiedDate() {
			return lastModifiedDate;
		} 
		
		/**
		 * @param lastModifiedDate
		 *            설정할 lastModifiedDate
		 */
		@JsonDeserialize(using = JsonDateDeserializer.class)
		public void setLastModifiedDate(Date lastModifiedDate) {
			this.lastModifiedDate = lastModifiedDate;
		} 
	}

}
