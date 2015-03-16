package vls.chats.models;

import com.mongodb.BasicDBObject;

public class Room {
	public static final String DOC_NAME = "room";
	private String id;
	private String name;
	private String type;
	
	public static Room fromDBObject(BasicDBObject obj){
		return new Room()
			.id(obj.getObjectId("_id").toString())
			.name(obj.getString("name"))
			.type(obj.getString("type"));
	}
	
	public Room id(String id){
		this.id = id;
		return this;
	}
	
	public Room name(String name){
		this.name = name;
		return this;
	}
	
	public Room type(String type){
		this.type = type;
		return this;
	}
	
	public String getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	public String getType(){
		return type;
	}
}
