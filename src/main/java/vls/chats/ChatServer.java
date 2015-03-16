/**
 * Copyright (C) 2014 Hongfu Inc. All rights reserved.
 */

package vls.chats;

import org.vertx.java.platform.Verticle;



public class ChatServer extends Verticle{
	@Override
	public void start() {
		final int chatInterval = Context.instance().appConfig().chatInterval();
		final long timeout = Context.instance().appConfig().timeout();
		final long latencyTimeout = Context.instance().appConfig().latencyTimeout();
		final String mapPrefix = Context.instance().appConfig().mapPrefix();
		//final int socketPort = Context.instance().appConfig().socketPort();
		//final int restPort = Context.instance().appConfig().restPort();
		
		AppEnvironment env = new AppEnvironment(this.vertx,this.container.logger(),timeout,latencyTimeout,chatInterval,mapPrefix);
		
		vertx.createHttpServer().websocketHandler(new ChatHandler(env)).listen(40001);
		vertx.createHttpServer().requestHandler(new RestHandler(env).httpRouteMatcher).listen(40000);
		vertx.createHttpServer().requestHandler(new TempHandler(env).httpRouteMatcher).listen(39999);
	}
}