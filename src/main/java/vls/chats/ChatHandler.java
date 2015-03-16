/**
 * Copyright (C) 2014 Hongfu Inc. All rights reserved.
 */

package vls.chats;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.DecodeException;


public class ChatHandler implements Handler<ServerWebSocket> {
	private final Pattern chatUrlPattern = Pattern.compile("/chat" + "/(\\w*)");
	private AppEnvironment env;
	private Logger logger = Logger.getLogger(ChatHandler.class);

	public ChatHandler(AppEnvironment env){
		this.env = env;
	}

	
	private String getChannel(String buffer){
		try{
			String channel = new JsonObject(buffer.toString()).getString("c");
			return channel;
		}
		catch(DecodeException e){
			// TODO 
			// 输入错误
			return null;
		}
	}
	
	@Override
	public void handle(final ServerWebSocket webSocket){
		webSocket.exceptionHandler(new Handler<Throwable>() {
			@Override
			public void handle(Throwable throwable){
				logger.fatal("An unexpected exception occurs in web socket.", throwable);
			}
		});

		final Matcher m = chatUrlPattern.matcher(webSocket.path());
		if (!m.matches()) {
			webSocket.reject();
			return;
		}		
		
		webSocket.dataHandler(new Handler<Buffer>(){
			@Override
			public void handle(Buffer buffer) {
					String result = null;
					String channel = getChannel(buffer.toString());
					System.out.println(buffer.toString());
					
					switch(channel){
						case "chat.connect":
							result = Command.of(env).receive(
										_CommandApi.Connect, 
										buffer.toString(), 
										new Command.onConnect<_CommandApi.connect,_CommandApi.connect.Result>(),
										webSocket.textHandlerID(),
										webSocket.binaryHandlerID()
									);
							break;
						case "chat.send":
							result = Command.of(env).receive(
									_CommandApi.Dialogue, 
									buffer.toString(), 
									new Command.onSend<_CommandApi.dialogue,_CommandApi.dialogue.Result>(),
									webSocket.textHandlerID(),
									webSocket.binaryHandlerID()
								);							
							break;
						case "chat.ping":
							result = Command.of(env).receive(
										_CommandApi.Ping, 
										buffer.toString(), 
										new Command.onPing<_CommandApi.ping,_CommandApi.ping.Result>(),
										webSocket.textHandlerID(),
										webSocket.binaryHandlerID()
									);
							break;
					}
					System.out.println(result);
					env.getVertx().eventBus().send(webSocket.textHandlerID(), result);				
			}
		});

		webSocket.endHandler(new Handler<Void>() {
			@Override
			public void handle(Void event) {
				String buffer = "{\"c\":\"chat.socket_interrupt\",\"data\":{\"textHandlerID\":\""+webSocket.textHandlerID()+"\"}}";
	
				Command.of(env).receive(
						_CommandApi.Socket_Interrupt,
						buffer, 
						new Command.onSocketInterrupt<_CommandApi.socket_interrupt,_CommandApi.socket_interrupt.Result>(),
						webSocket.textHandlerID(),
						webSocket.binaryHandlerID()
					);				
			}
		});
	}
}