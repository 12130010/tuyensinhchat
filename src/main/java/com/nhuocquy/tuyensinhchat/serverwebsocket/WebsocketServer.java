package com.nhuocquy.tuyensinhchat.serverwebsocket;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.collect.Maps;
import com.nhuocquy.model.Conversation;
import com.nhuocquy.model.Friend;
import com.nhuocquy.model.MessageChat;


@ServerEndpoint("/chat")
public class WebsocketServer {
//	private static final Set<Session> sessions = Collections
//			.synchronizedSet(new HashSet<Session>());
	private static final Map<String, Session> mapSession = new HashMap<String, Session>();
	public final static String IDS = "id";
	private String id;
	private Map<Long, Session> myMapSession = new HashMap<Long, Session>();
	private Conversation conversation;
	private MessageChat messageChat;
	private ObjectMapper objectMapper = new ObjectMapper();
	private String json;
	
	@OnOpen
	public void onOpen(Session session) {
		Map<String, String> queryParams = getQueryMap(session.getQueryString());
		 id = queryParams.get(IDS);
		if(id != null){
			mapSession.put(id, session);
		}
		System.out.println("Open: " + id);
		System.out.println(mapSession.size());
	}

	@OnMessage
	public void onMessage(String message, Session session) {
		System.out.println(message);
		try {
			conversation = objectMapper.readValue(message, Conversation.class);
			messageChat = conversation.getListMes().get(0);
			messageChat.setIdConversation(conversation.getIdCon());
			messageChat.setDate(new Date());
			json = objectMapper.writeValueAsString(messageChat);
			for (Friend f : conversation.getFriends()) {
				session = myMapSession.get(f.getIdFriend());
				if(session == null){
					session = mapSession.get(f.getIdFriend());
					myMapSession.put(f.getIdFriend(), session);
				}
				if(session == null){
					//TODO luu tin nhan chÆ°a Ä‘á»�c
				}else{
					session.getBasicRemote().sendText(json);
				}
			}
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@OnClose
	public void onClose(Session session) {
		if(id != null){
			mapSession.remove(id);
		}
			
	}
	public static Map<String, String> getQueryMap(String query) {
		Map<String, String> map = Maps.newHashMap();
		if (query != null) {
			String[] params = query.split("&");
			for (String param : params) {
				String[] nameval = param.split("=");
				map.put(nameval[0], nameval[1]);
			}
		}
		return map;
	}

}
