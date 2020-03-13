package architecture.community.web.spring.controller.data;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;

import architecture.community.attachment.AttachmentService;
import architecture.community.comment.CommentService;
import architecture.community.exception.NotFoundException;
import architecture.community.model.Models;
import architecture.community.streams.DefaultStreamMessage;
import architecture.community.streams.StreamMessage;
import architecture.community.streams.StreamMessageNotFoundException;
import architecture.community.streams.StreamThread;
import architecture.community.streams.Streams;
import architecture.community.streams.StreamsNotFoundException;
import architecture.community.streams.StreamsService;
import architecture.community.user.User;
import architecture.community.user.UserAvatarService;
import architecture.community.user.UserManager;
import architecture.community.util.SecurityHelper;
import architecture.community.web.model.ItemList;
import architecture.community.web.model.Result;
import architecture.ee.service.ConfigService;

@Controller("community-streams-data-controller")
@RequestMapping("/data/streams")
public class StreamsDataController {
	
	private Logger log = LoggerFactory.getLogger(StreamsDataController.class);
	
	@Autowired(required=false)
	@Qualifier("configService")
	private ConfigService configService;
	
	@Autowired(required = false) 
	@Qualifier("userAvatarService")
	private UserAvatarService userAvatarService;
	
	@Autowired(required = false) 
	@Qualifier("userManager")
	private UserManager userManager;
	
	@Autowired(required = false) 
	@Qualifier("streamsService")
	private StreamsService streamsService;
	
	@Inject
	@Qualifier("commentService")
	private CommentService commentService;

	@Inject
	@Qualifier("attachmentService")
	private AttachmentService attachmentService; 

	
	
	public StreamsDataController() {
	}

	/**
	 * 
	 * /data/streams/{streamsId}/threads/list.json
	 * 
	 * @param streamsId
	 * @param request
	 * @return
	 * @throws StreamsNotFoundException
	 */
	@RequestMapping(value = "/list.json", method = { RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public ItemList listThread (
			NativeWebRequest request) throws StreamsNotFoundException {	 
		List<Streams> list = streamsService.getAllStreams(); 
		return new ItemList(list, list.size());
	}
	
	/**
	 * 
	 * /data/streams/{streamsId}/threads/list.json
	 * 
	 * @param streamsId
	 * @param request
	 * @return
	 * @throws StreamsNotFoundException
	 */
	@RequestMapping(value = "/{streamsId:[\\p{Digit}]+}/threads/list.json", method = { RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public ItemList listThread (@PathVariable Long streamsId, 
			@RequestParam(value = "skip", defaultValue = "0", required = false) int skip,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "pageSize", defaultValue = "0", required = false) int pageSize,
			NativeWebRequest request) throws StreamsNotFoundException {	
		
		log.debug(" skip: {}, page: {}, pageSize: {}", skip, page, pageSize ); 
		Streams streams = streamsService.getStreamsById(streamsId); 
		List<StreamThread> list; 
		int totalSize = streamsService.getStreamThreadCount(Models.STREAMS.getObjectType(), streams.getStreamId()); 
		if( pageSize == 0 && page == 0){
			list = streamsService.getStreamThreads(Models.STREAMS.getObjectType(), streams.getStreamId() );
		}else{
			list = streamsService.getStreamThreads(Models.STREAMS.getObjectType(),  streams.getStreamId(), skip, pageSize);
		} 
		return new ItemList(list, totalSize);
	}
	
	/**
	 * 
	 * /data/streams/{streamsId}/threads/list.json
	 * 
	 * @param streamsId
	 * @param request
	 * @return
	 * @throws StreamsNotFoundException
	 */
	@RequestMapping(value = "/me/threads/list.json", method = { RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public ItemList listThread (
			@RequestParam(value = "skip", defaultValue = "0", required = false) int skip,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "pageSize", defaultValue = "0", required = false) int pageSize,
			NativeWebRequest request) throws StreamsNotFoundException {	
		
		Streams streams = Utils.getStreamsByNameCreateIfNotExist(streamsService , Utils.ME_STREAM_NAME); 
		List<StreamThread> list; 
		int totalSize = streamsService.getStreamThreadCount(Models.STREAMS.getObjectType(), streams.getStreamId()); 
		if( pageSize == 0 && page == 0){
			list = streamsService.getStreamThreads(Models.STREAMS.getObjectType(), streams.getStreamId() );
		}else{
			list = streamsService.getStreamThreads(Models.STREAMS.getObjectType(),  streams.getStreamId(), skip, pageSize);
		} 
		return new ItemList(list, totalSize);
	}
	 

	@RequestMapping(value = "/me/threads/add.json", method = { RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public StreamThread addThread (@RequestBody DefaultStreamMessage newMessage,  NativeWebRequest request) throws StreamsNotFoundException {	
		User user = SecurityHelper.getUser();
		Streams streams = Utils.getStreamsByNameCreateIfNotExist(streamsService , Utils.ME_STREAM_NAME); 
		if (newMessage.getThreadId() < 1 && newMessage.getMessageId() < 1) {
			StreamMessage rootMessage = streamsService.createMessage(Models.STREAMS.getObjectType(), streams.getStreamId(), user);
			rootMessage.setSubject(newMessage.getSubject());
			rootMessage.setBody(newMessage.getBody());
			StreamThread thread = streamsService.createThread(rootMessage.getObjectType(), rootMessage.getObjectId(), rootMessage );
			streamsService.addThread(rootMessage.getObjectType(), rootMessage.getObjectId(), thread);
			return thread;
		}
		return null;
	}
	
	@RequestMapping(value = "/me/threads/{threadId:[\\p{Digit}]+}/delete.json", method = { RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public Result deleteThread (
			@PathVariable Long threadId,  NativeWebRequest request) throws NotFoundException {	
		User user = SecurityHelper.getUser();
		Result result = Result.newResult();
		if (threadId > 0L) {
			StreamThread thread = streamsService.getStreamThread(threadId);
			if( thread.getRootMessage().getUser().getUserId() == user.getUserId() ) {
				streamsService.deleteThread(thread);
				result.setCount(1);
			}
		}
		return result;
	}
	
	@Secured({ "ROLE_USER" })
	@RequestMapping(value = "/me/messages/add.json", method = { RequestMethod.POST })
	@ResponseBody
	public StreamMessage addMessage(@RequestBody DefaultStreamMessage newMessage, NativeWebRequest request) { 
		User user = SecurityHelper.getUser();
		Streams streams = Utils.getStreamsByNameCreateIfNotExist(streamsService , Utils.ME_STREAM_NAME); 
		if (newMessage.getThreadId() < 1 && newMessage.getMessageId() < 1) {
			StreamMessage rootMessage = streamsService.createMessage(Models.STREAMS.getObjectType(), streams.getStreamId(), user);
			rootMessage.setSubject(newMessage.getSubject());
			rootMessage.setBody(newMessage.getBody());
			StreamThread thread = streamsService.createThread(rootMessage.getObjectType(), rootMessage.getObjectId(), rootMessage );
			streamsService.addThread(rootMessage.getObjectType(), rootMessage.getObjectId(), thread);
			return thread.getRootMessage();
		}
		return newMessage;
	}
	

	@RequestMapping(value = "/messages/{messageId:[\\p{Digit}]+}/get.json", method = RequestMethod.POST)
	@ResponseBody
	public StreamMessage getMessageById(@PathVariable Long messageId, NativeWebRequest request) throws NotFoundException {
		if (messageId < 1) {
			throw new StreamMessageNotFoundException();
		}
		StreamMessage message = streamsService.getStreamMessage(messageId);
		StreamThread thread = streamsService.getStreamThread(message.getThreadId()); 
		return message;
	}

	@RequestMapping(value = "/messages/{messageId:[\\p{Digit}]+}/update.json", method = RequestMethod.POST)
	@ResponseBody
	public StreamMessage updateMessage(@RequestBody DefaultStreamMessage newMessage, NativeWebRequest request) throws NotFoundException { 
		User user = SecurityHelper.getUser();	 
		StreamMessage message = streamsService.getStreamMessage(newMessage.getMessageId());
		message.setSubject(newMessage.getSubject());
		message.setBody(newMessage.getBody());
		streamsService.updateMessage(message);
		
		return message;
	} 
	
 
}
