/**
 * Copyright (C) 2014 Hongfu Inc. All rights reserved.
 */

package vls.chats.db;

import java.net.UnknownHostException;

import vls.chats.Context;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;

/**
 * 
 * @author Eric Gohn <br>
 *         Jan 7, 2014
 * 
 */
public class DBContext {
	private MongoClient client;

	public DBContext() {
		try {
			client = new MongoClient(Context.instance().appConfig().mongoUri());
		} catch (UnknownHostException e) {
			throw new DataException("Cannot connect to mongo db.");
		}
	}

	public void dispose() {
		if (client != null) {
			client.close();
			client = null;
		}
	}

	public DBCollection getCollection(String name) {
		return getDB().getCollection(name);
	}

	private DB getDB() {
		DB db = client.getDB(Context.instance().appConfig().mongoUri().getDatabase());
		db.setWriteConcern(WriteConcern.SAFE);
		return db;
	}
}