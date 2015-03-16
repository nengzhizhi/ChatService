/**
 * Copyright (C) 2013 Hongfu Inc. All rights reserved.
 */

package vls.chats.models;

import java.util.Date;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


public class DialogueMessage {
	public static final String DOC_NAME = "message";
	private ObjectId _id;
	private String message;
	private String nickname;
	private String userID;
	private String avatar;
	private Date created;
	private String roomId;

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public static DialogueMessage fromDBObject(BasicDBObject obj) {
		return new DialogueMessage()
				.created(obj.getDate("created"))
				.nickname(obj.getString("nickname"))
				.message(obj.getString("message"))
				.avatar(obj.getString("avatar"))
				.userID(obj.getString("userID"))
				.roomId(obj.getString("roomId"));
	}

	public DialogueMessage avatar(String avatar) {
		this.avatar = avatar;
		return this;
	}

	public DialogueMessage created(Date created) {
		this.created = created;
		return this;
	}
	
	public DialogueMessage roomId(String roomId){
		this.roomId = roomId;
		return this;		
	}

	public String getAvatar() {
		return avatar;
	}

	public Date getCreated() {
		return created;
	}

	public ObjectId getId() {
		return _id;
	}

	public String getMessage() {
		return message;
	}

	public String getNickname() {
		return nickname;
	}

	public String getUserID() {
		return userID;
	}

	public DialogueMessage message(String message) {
		this.message = message;
		return this;
	}

	public DialogueMessage nickname(String nickname) {
		this.nickname = nickname;
		return this;
	}

	public DBObject toDBObject() {
		return new BasicDBObject()
				.append("created", created)
				.append("nickname", nickname)
				.append("message", message)
				.append("avatar", avatar)
				.append("userID", userID)
				.append("roomId", roomId);
	}

	public DialogueMessage userID(String userID) {
		this.userID = userID;
		return this;
	}
}
