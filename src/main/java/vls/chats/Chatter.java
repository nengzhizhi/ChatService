/**
 * Copyright (C) 2014 Hongfu Inc. All rights reserved.
 */

package vls.chats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.vertx.java.core.shareddata.Shareable;

public class Chatter implements Shareable{
	private String username;
	private String userId;
	private String avatar;
	private	String level;
	private List<Connector> connectors = Collections.synchronizedList(new ArrayList<Connector>());
	
	
	public Chatter(String username,String userId,String avatar,String level){
		this.username = username;
		this.userId = userId;
		this.avatar = avatar;
		this.level = level;
	}
	
	public String getUsername(){
		return this.username;
	}
	
	public String getUserId(){
		return this.userId;
	}
	
	public String getAvatar(){
		return this.avatar;
	}
	
	public String getLevel(){
		return this.level;
	}
	
	public List<Connector> getConnectors(){
		synchronized (connectors) {
			return connectors;
		}
	}
	
	public void addConnector(Connector connector){
		synchronized (connectors) {
			if(!containsConnector(connector)){
				connectors.add(connector);
			}
		}
	}
	
	public int removeConnector(Connector connector){
		synchronized (connectors) {
			connectors.remove(connector);
			return connectors.size();
		}
	}
	
	public boolean containsConnector(Connector connector){
		synchronized (connectors) {
			return connectors.contains(connector);
		}		
	}
	
	public int getConnectorsCount(){
		synchronized (connectors) {
			return connectors.size();
		}		
	}
}