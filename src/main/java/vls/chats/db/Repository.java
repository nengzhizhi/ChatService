/**
 * Copyright (C) 2014 Hongfu Inc. All rights reserved.
 */

package vls.chats.db;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.shareddata.ConcurrentSharedMap;

import vls.chats.AppEnvironment;
import vls.chats.ChatRoom;
import vls.chats.models.Manager;
import vls.chats.models.Room;
import vls.chats.models.Record;
import vls.chats.models.DialogueMessage;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.MongoException;


public class Repository {
	private DBContext context;
	private AppEnvironment env;

	public Repository() {
		this.context = new DBContext();
	}
	
	public Repository(AppEnvironment env) {
		this.context = new DBContext();
		this.env = env;
	}
	
	public void addMessage(DialogueMessage message) {
		context.getCollection(DialogueMessage.DOC_NAME).insert(message.toDBObject());
	}

	public void addMessages(List<DialogueMessage> messages) {
		List<DBObject> list = new ArrayList<DBObject>();
		for (DialogueMessage message : messages) {
			list.add(message.toDBObject());
		}
		context.getCollection(DialogueMessage.DOC_NAME).insert(list);
	}

	public void readManagers(){
		DBCursor cursor = null;
		try{
			cursor = context.getCollection(Manager.DOC_NAME).find().sort(new BasicDBObject("_id",-1));;
			
			ConcurrentSharedMap<String, Manager> managersMap
				= env.getVertx().sharedData().getMap(env.getMapPrefix() + "managers");
			
			while(cursor.hasNext()){
				BasicDBObject obj = (BasicDBObject) cursor.next();
				Manager manager = Manager.fromDBObject(obj);
				managersMap.put(manager.getUsername()+"-"+manager.getRoomId(), manager);
			}
		}
		catch (MongoException e) {
			throw new DataException("Error occured while finding manager list", e);
		} finally {
			if (cursor != null)
				cursor.close();
		}
	}
	
	public JsonArray getManagers(String roomId){
		DBCursor cursor = null;
		try{
			cursor = context.getCollection(Manager.DOC_NAME).find(
					new BasicDBObject().append("roomId",roomId)
				);	
			
			JsonArray managerArray = new JsonArray();
			while(cursor.hasNext()){
				BasicDBObject obj = (BasicDBObject) cursor.next();
				Manager manager = Manager.fromDBObject(obj);
				JsonObject managerObject = new JsonObject();
				managerObject.putString("roomId", manager.getRoomId())
						  .putString("username", manager.getUsername())
						  .putString("uid", manager.getUid());
				managerArray.add(managerObject);
			}
			return managerArray;
		}
		catch (MongoException e) {
			throw new DataException("Error occured while creating room", e);
		} finally {
			if (cursor != null)
				cursor.close();
		}			
	}	
	
	public boolean writeManager(String roomId,String username,String uid){
		DBCursor cursor = null;
		try{
			cursor = context.getCollection(Manager.DOC_NAME).find(
						new BasicDBObject()
							.append("roomId",roomId)
							.append("username",username)
					);
			
			if(!cursor.hasNext()){
				context.getCollection(Manager.DOC_NAME).insert(
						new BasicDBObject()
							.append("roomId",roomId)
							.append("uid",uid)
							.append("username",username)
						);
				return true;
			}
			else{
				return false;
			}
		}
		catch (MongoException e) {
			throw new DataException("Error occured while finding manager list", e);
		} finally {
			if (cursor != null)
				cursor.close();
		}		
	}
	
	public void readRooms(){
		DBCursor cursor = null;
		try{
			cursor = context.getCollection(Room.DOC_NAME)
							.find()
							.sort(new BasicDBObject("_id",-1));
			
			ConcurrentSharedMap<String, ChatRoom> chatroomsMap
				= env.getVertx().sharedData().getMap(env.getMapPrefix() + "chat-rooms");
			
			while(cursor.hasNext()){
				BasicDBObject obj = (BasicDBObject) cursor.next();
				Room room = Room.fromDBObject(obj);
				chatroomsMap.put(room.getId(), new ChatRoom(room.getId()));
			}
		}
		catch (MongoException e) {
			throw new DataException("Error occured while finding manager list", e);
		} finally {
			if (cursor != null)
				cursor.close();
		}
	}		
	
	
	public JsonArray getRooms(){
		DBCursor cursor = null;
		try{
			cursor = context.getCollection(Room.DOC_NAME).find(
					new BasicDBObject()
				);	
			
			JsonArray roomArray = new JsonArray();
			while(cursor.hasNext()){
				BasicDBObject obj = (BasicDBObject) cursor.next();
				Room room = Room.fromDBObject(obj);
				JsonObject roomObject = new JsonObject();
				roomObject.putString("roomId", room.getId())
						  .putString("roomName", room.getName())
						  .putString("roomType", room.getType());
				roomArray.add(roomObject);
			}
			return roomArray;
		}
		catch (MongoException e) {
			throw new DataException("Error occured while creating room", e);
		} finally {
			if (cursor != null)
				cursor.close();
		}			
	}
	
	public void insertRecord(String roomId,long span,String type,String data){
		DBCursor cursor = null;
		try{
			context.getCollection(Record.DOC_NAME).insert(
					new BasicDBObject()
						.append("roomId",roomId)
						.append("span",span)
						.append("type",type)
						.append("data",data)
					);
		}
		catch (MongoException e) {
			throw new DataException("Error occured while writing message", e);
		} finally {
			if (cursor != null)
				cursor.close();
		}
	}

	public void deleteRecord(String roomId,String recordId){
		DBCursor cursor = null;
		try{
			context.getCollection(Record.DOC_NAME).remove(
						new BasicDBObject().append("_id", new ObjectId(recordId))
					);
		}
		catch (MongoException e) {
			throw new DataException("Error occured while writing message", e);
		} finally {
			if (cursor != null)
				cursor.close();
		}
	}	
	
	public List<Record> readRecords(String roomId){
		DBCursor cursor = null;
		try{
			cursor = context.getCollection(Record.DOC_NAME)
							.find(new BasicDBObject().append("roomId",roomId))
							.sort(new BasicDBObject("span",1));;
			
			//JsonArray recordArray = new JsonArray();
			List<Record> list = new ArrayList<Record>();
			while(cursor.hasNext()){
				BasicDBObject obj = (BasicDBObject) cursor.next();
				Record record = Record.fromDBObject(obj);
				list.add(record);
			}
			return list;
		}
		catch (MongoException e) {
			throw new DataException("Error occured while reading message list", e);
		} finally {
			if (cursor != null)
				cursor.close();
		}
	}
	
	public JsonArray readRecordsJsonArray(String roomId){
		DBCursor cursor = null;
		try{
			cursor = context.getCollection(Record.DOC_NAME)
							.find(new BasicDBObject().append("roomId",roomId))
							.sort(new BasicDBObject("span",1));;
			
			JsonArray recordArray = new JsonArray();
			while(cursor.hasNext()){
				BasicDBObject obj = (BasicDBObject) cursor.next();
				Record record = Record.fromDBObject(obj);
				JsonObject recordObject = new JsonObject();
				recordObject.putString("id", record.getId());
				recordObject.putString("roomId", record.getRoomId());
				recordObject.putNumber("span", record.getSpan());
				recordObject.putString("type", record.getType());
				recordObject.putString("data", record.getData());
				recordArray.add(recordObject);
			}
			return recordArray;
		}
		catch (MongoException e) {
			throw new DataException("Error occured while reading message list", e);
		} finally {
			if (cursor != null)
				cursor.close();
		}
	}	
	
//------------------------------------------------------------------------------------------------------------------------------------	
	
}