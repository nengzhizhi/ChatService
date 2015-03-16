/**
 * Copyright (C) 2014 Hongfu Inc. All rights reserved.
 */

package vls.chats;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.shareddata.Shareable;

import vls.chats.utils.Strings2;


/**
 * Class represents chatRoom in the system. <br>
 * A chatRoom is a place where {@link Chatter}s can talk in.
 */

public class ChatRoom implements Shareable {
	private Map<String,Chatter> chattersMap = Collections.synchronizedMap(new HashMap<String,Chatter>());
	private Map<String,Chatter> managersMap = Collections.synchronizedMap(new HashMap<String,Chatter>());
	private Set<Connector> connectors = Collections.synchronizedSet(new HashSet<Connector>());
	private String roomId;

	public String getRoomId() {
		return roomId;
	}
	
	public ChatRoom(String roomId) {
		this.roomId = roomId;
	}

	public Map<String,Chatter> getChatterMap(){
		return chattersMap;
	}
	
	public Map<String,Chatter> getManagerMap(){
		return managersMap;
	}
	
	public void addConnector(Connector connector){
		synchronized (connectors) {
			connectors.add(connector);
		}		
	}
	
	public boolean containsConnector(Connector connector) {
		synchronized (connectors) {
			return connectors.contains(connector);
		}	
	}
	
	public void removeConnector(Connector connector) {
		synchronized (connectors) {
			connectors.remove(connector);
		}
	}
	
	
	public int getConnectorSize(){
		synchronized (connectors){
			return connectors.size();
		}		
	}
//------------------------------------------------------------------------------	
	
	
	public void addChatter(String username,Chatter chatter) {
		synchronized (chattersMap) {
			chattersMap.put(username, chatter);
		}
	}	
	
	public boolean containsChatter(String username) {
		synchronized (chattersMap) {
			return chattersMap.containsKey(username);
		}
	}
	
	public Chatter getChatter(String username){
		synchronized (chattersMap) {
			return chattersMap.get(username);
		}
	}
	
	public JsonArray getChatters(int start,int count){
		if(count<=0||start>=chattersMap.size()){
			return new JsonArray();
		}
		
		synchronized (chattersMap) {
			Object[] chatterArray = chattersMap.values().toArray();
			java.util.Arrays.sort(chatterArray,new ChatterComparator());
			JsonArray result = new JsonArray();
			for(int i=start;i<start+count&&i<chatterArray.length;i++){
				result.addObject(new JsonObject()
										.putString("username",((Chatter)chatterArray[i]).getUsername())
										.putString("uid",((Chatter)chatterArray[i]).getUserId())
										.putString("avatar",((Chatter)chatterArray[i]).getAvatar())
										.putString("level",((Chatter)chatterArray[i]).getLevel())
						);
			}
			return result;
		}
	}
	
	public class ChatterComparator implements Comparator<Object>{
		public int compare(Object o1,Object o2){
			Chatter c1 = (Chatter)o1;
			Chatter c2 = (Chatter)o2;
			float float_c1,float_c2;
			
			if(Strings2.isNullOrWhiteSpace(c1.getLevel())){
				float_c1 = 0;
			}
			else{
				float_c1 = Float.parseFloat(c1.getLevel());
			}

			if(Strings2.isNullOrWhiteSpace(c2.getLevel())){
				float_c2 = 0;
			}
			else{
				float_c2 = Float.parseFloat(c2.getLevel());
			}
			
			if(float_c2 - float_c1 > 0){
				return 1;
			}
			else if(float_c2 - float_c1 < 0){
				return -1;
			}
			else{
				return 0;
			}
		}
	}

	public int getChattersSize(){
		synchronized (managersMap){
			return chattersMap.size();
		}
	}

	public void removeChatter(String username) {
		synchronized (chattersMap) {
			chattersMap.remove(username);
		}
	}	

//--------------------------------------------------------------------------------------	
	
	public void addManager(String username,Chatter chatter){
		managersMap.put(username,chatter);
	}
	
	public boolean containsManager(String username) {
		synchronized (managersMap) {
			return managersMap.containsKey(username);
		}
	}	

	public JsonArray getManagers(){
		synchronized (managersMap) {
			Object[] managerArray = managersMap.values().toArray();
			JsonArray result = new JsonArray();
			for(int i=managerArray.length-1;i>-1;i--){
				result.addObject(new JsonObject()
							.putString("username",((Chatter)managerArray[i]).getUsername())
							.putString("uid",((Chatter)managerArray[i]).getUserId())
							.putString("avatar",((Chatter)managerArray[i]).getAvatar())
							.putString("level",((Chatter)managerArray[i]).getLevel())
						);
			}
			return result;
		}
	}	
	
	public int getManagersSize(){
		synchronized (managersMap){
			return managersMap.size();
		}
	}
	
	public void removeManager(String username) {
		synchronized (managersMap) {
			managersMap.remove(username);
		}
	}		
	
	/**
	 * Publish message to all connectors in this room.
	 * 
	 * @param message
	 *            The message to be published.
	 * @param ignored
	 *            The chatter who will ignore this message.
	 */
	
	public void publish(String message, Connector ignored){
		synchronized (connectors) {
			for (Connector connector : connectors) {
				if (connector.equals(ignored))
					continue;
				connector.subscribe(message);
			}
		}		
	}
	
	
	public void publish2(String message, String ignoredUsername){
		Chatter ignorer = chattersMap.get(ignoredUsername);
		List<Connector> ignoreConnectors = null;
		if(ignorer != null){
			ignoreConnectors = ignorer.getConnectors();
		}
		
		synchronized (connectors) {
			for (Connector connector : connectors) {
				if(ignoreConnectors == null){
					connector.subscribe(message);
				}
				else if(!ignoreConnectors.contains(connector)){
					connector.subscribe(message);
				}
			}
		}
	}
	
}
