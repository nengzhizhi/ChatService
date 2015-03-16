/**
 * Copyright (C) 2014 Hongfu Inc. All rights reserved.
 */

package vls.chats;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.logging.Logger;

//environment is difference with configure 

public class AppEnvironment{
	private final Vertx vertx;
	private final Logger logger;
	private final long timeout;
	private final long latencyTimeout;
	private final int chatInterval;
	private final String mapPrefix;

	public AppEnvironment(Vertx vertx,Logger logger,long timeout,long latencyTimeout,int chatInterval,String mapPrefix){
		this.vertx = vertx;
		this.logger = logger;
		this.timeout = timeout;
		this.latencyTimeout = latencyTimeout;
		this.chatInterval = chatInterval;
		this.mapPrefix = mapPrefix;
	}

	public Logger getLogger() {
		return logger;
	}

	public Vertx getVertx() {
		return vertx;
	}
	
	public long getTimeout() {
		return timeout;
	}
	
	public long getLatencyTimeout(){
		return latencyTimeout;
	}
	
	public int getChatInterval(){
		return chatInterval;
	}
	
	public String getMapPrefix(){
		return mapPrefix;
	}
}