/**
 * Copyright (C) 2014 Hongfu Inc. All rights reserved.
 */

package vls.chats.models;

import org.vertx.java.core.shareddata.Shareable;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class Manager  implements Shareable{
	public static final String DOC_NAME = "manager";
	private String roomId;
	private String uid;
	private String username;

	public static Manager fromDBObject(BasicDBObject obj){
		return new Manager()
		.roomId(obj.getString("roomId"))
		.uid(obj.getString("uid"))
		.username(obj.getString("username"));
	}

	public Manager roomId(String room){
		this.roomId = room;
		return this;
	}
	
	public Manager username(String username){
		this.username = username;
		return this;
	}	
	
	public Manager uid(String uid){
		this.uid = uid;
		return this;
	}		
	
	public String getRoomId(){
		return this.roomId;
	}

	public String getUid() {
		return this.uid;
	}

	public String getUsername() {
		return this.username;
	}		
		
	
	public DBObject toDBObject() {
		return new BasicDBObject()
			.append("roomId",roomId)
			.append("uid",uid)
			.append("username",username);
	}
}