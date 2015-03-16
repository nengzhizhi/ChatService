/**
 * Copyright (C) 2014 Hongfu Inc. All rights reserved.
 */

package vls.chats.models;

import org.vertx.java.core.shareddata.Shareable;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class Record  implements Shareable{
	public static final String DOC_NAME = "record";
	private String id;
	private String roomId;
	private long span;
	private String type;
	private String data;

	public static Record fromDBObject(BasicDBObject obj){
		return new Record()
		.id(obj.getObjectId("_id").toString())
		.roomId(obj.getString("roomId"))
		.span(obj.getLong("span"))
		.data(obj.getString("data"))
		.type(obj.getString("type"));
	}

	public Record id(String id){
		this.id = id;
		return this;
	}	
	
	public Record roomId(String room){
		this.roomId = room;
		return this;
	}
	
	public Record span(long span){
		this.span = span;
		return this;
	}	

	public Record type(String type){
		this.type = type;
		return this;
	}	
	
	public Record data(String data){
		this.data = data;
		return this;
	}		

	public String getRoomId(){
		return this.roomId;
	}
	
	public long getSpan(){
		return this.span;
	}	
	
	public String getData(){
		return this.data;
	}		

	public String getType(){
		return this.type;
	}		
	
	public String getId(){
		return this.id;
	}		
	
	public DBObject toDBObject() {
		return new BasicDBObject()
			.append("roomId",roomId)
			.append("span",span)
			.append("type",type)
			.append("data",data);
	}
}