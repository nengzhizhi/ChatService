/**
 * Copyright (C) 2014 Hongfu Inc. All rights reserved.
 */
package vls.chats;

import java.util.UUID;

import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.shareddata.Shareable;

public class Connector implements Shareable{
	private String roomId;
	private String username;
	private String token;
	private String textHandlerID;
	private EventBus eventBus;
	
	/**
	 * Reserve this field for future.
	 */
	@SuppressWarnings("unused")
	private String binaryHandlerID;
	
	public Connector(String roomId,String username,String textHandlerID, String binaryHandlerID, EventBus eventBus){
		this.roomId = roomId;
		this.username = username;
		this.textHandlerID = textHandlerID;
		this.binaryHandlerID = binaryHandlerID;
		this.eventBus = eventBus;
		this.token = UUID.randomUUID().toString();
	}
	
	public String getRoomId() {
		return roomId;
	}
	
	public String getToken() {
		return token;
	}	
	
	public String getTextHandlerID(){
		return textHandlerID;
	}
	
	public void subscribe(String message){
		eventBus.send(this.textHandlerID, message);
	}
	
	public String getUsername(){
		return username;
	}
}
