package com.nhuocquy.tuyensinhchat.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.websocket.Session;

public class GroupChat {
	String id;
	List<Session> listSessions;

	public GroupChat(String id) {
		this.id = id;
		listSessions = new ArrayList<Session>();
	}

	public boolean add(Session s) {
		return listSessions.add(s);
	}

	public void remove(Session s) {
		listSessions.remove(s);
	}

	public void sendToAll(String mes) {
		for (Session s : listSessions) {
			try {
				s.getBasicRemote().sendText(mes);
			} catch (IOException e) {
				listSessions.remove(s);
				e.printStackTrace();
			}
		}
	}

	@Override
	public String toString() {
		return "GroupChat [id=" + id + ", listSessions=" + listSessions + "]";
	}
	
}
