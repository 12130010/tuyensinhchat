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
import com.nhuocquy.dao.DAOConversation;
import com.nhuocquy.dao.exception.DAOException;
import com.nhuocquy.model.Conversation;
import com.nhuocquy.model.Friend;
import com.nhuocquy.model.MessageChat;

@ServerEndpoint("/chat")
public class WebsocketServer {
	// private static final Set<Session> sessions = Collections
	// .synchronizedSet(new HashSet<Session>());
	public static final String MESSAGE_ADD_FRIEND = (char) 0 + "addfriend";
	private static final Map<String, Session> mapSession = new HashMap<String, Session>();
	public final static String IDS = "id";
	private String id;
	private Map<Long, Session> myMapSession = new HashMap<Long, Session>();
	private Conversation conversation;
	private MessageChat messageChat;
	private ObjectMapper objectMapper = new ObjectMapper();
	private String json;
	private DAOConversation daoConversation = new DAOConversation(
			Conversation.class);
	// hint
	public static final String MES_HINT_ON = (char) 0 + "hintOn";
	public static final String MES_HINT_OFF = (char) 0 + "hintOff";

	@OnOpen
	public void onOpen(Session session) {
		Map<String, String> queryParams = getQueryMap(session.getQueryString());
		id = queryParams.get(IDS);
		if (id != null) {
			mapSession.put(id, session);
		}
		System.out.println("Open: " + id + " : " + mapSession.get(id));
		System.out.println(mapSession.size());
	}

	@OnMessage
	public void onMessage(String message, Session session) {
		System.out.println(message);
		if(message.contains(MESSAGE_ADD_FRIEND))
			sendAddfriendNotify(message, session);
		else
			sendMessageChat(message, session);
		
	}
	private void sendAddfriendNotify(String message, Session session){
		long idacc = Long.parseLong(message.substring(message.indexOf('+')+1, message.indexOf(':')));
		System.out.println("tim đc idacc la: " + idacc);
		session = myMapSession.get(idacc);
		if (session == null) {
			session = mapSession.get(String.valueOf(idacc));
			myMapSession.put(idacc, session);
		}
		if (session == null) {
		} else {
			try {
				session.getBasicRemote().sendText(MESSAGE_ADD_FRIEND + ":" +message.substring(message.indexOf(':')+1));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private void sendMessageChat(String message, Session session){
		try {
			conversation = objectMapper.readValue(message, Conversation.class);
			messageChat = conversation.getListMes().get(0);
			messageChat.setIdConversation(conversation.getIdCon());
			messageChat.setDate(new Date());
			if (!messageChat.getText().contains(MES_HINT_ON)
					&& !messageChat.getText().contains(MES_HINT_OFF)) {
				daoConversation.saveMessageChatInConversation(conversation);
			}
			json = objectMapper.writeValueAsString(messageChat);
			for (Friend f : conversation.getFriends()) {
				session = myMapSession.get(f.getIdFriend());
				if (session == null) {
					session = mapSession.get(String.valueOf(f.getIdFriend()));
					myMapSession.put(f.getIdFriend(), session);
				}
				if (session == null) {
					// TODO luu tin nhan chÆ°a Ä‘á»�c
				} else {
					session.getBasicRemote().sendText(json);
				}
			}
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DAOException e) {
			e.printStackTrace();
		}
	}
	@OnClose
	public void onClose(Session session) {
		if (id != null) {
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
