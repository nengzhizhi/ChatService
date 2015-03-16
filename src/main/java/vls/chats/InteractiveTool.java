/**
 * Copyright (C) 2014 Hongfu Inc. All rights reserved.
 */

package vls.chats;

import org.vertx.java.core.shareddata.Shareable;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.Handler;

import base.bus.Message;

public class InteractiveTool implements Shareable{
	public AppEnvironment env;
	private String stage;
	private JsonArray actors;
	private JsonObject card;
	private String interactionId;
	private ChatRoom chatRoom;

	public InteractiveTool(final AppEnvironment env, ChatRoom chatRoom, String interactionId, JsonObject card, JsonArray actors){
		this.env = env;
		this.chatRoom = chatRoom;
		this.interactionId = interactionId;
		this.card = card;
		this.actors = actors;
		this.stage = "buyCardStage";
	}

	public void StartBuyCardStage(){
		/*
		 * 为互动中途进入房间的观众每秒钟发送一次showBuyCard请求
		 */		
		//for(int i=card.getInteger("buyCardSecond");i>0;i--){
		for(int i=10;i>0;i--){
			final int left = i;
			env.getVertx().setTimer(
				i*1000,
				new Handler<Long>() {
					@Override
					public void handle(Long event) {
						_CommandApi.showBuyCard command = new _CommandApi.showBuyCard(
																	interactionId,
																	card,
																	left,
																	actors
																);
							
						String commandStr = _CommandApi.ShowBuyCard.getRequestMapper().encode(
												Message.message("chat.showBuyCard", command)
											);

						//System.out.println(commandStr);
						chatRoom.publish(commandStr, null);
					}
				}
			);		
		}
	}

	public void StartUseCardStage(){
		this.stage = "useCardStage";
	}

	public void StartEndCardStage(){
		this.stage = "endCardStage";
	
		JsonObject winner = new JsonObject()
							.putString("id", "123")
							.putString("name", "angla")
							.putString("avatar", "http://vgame.tv/tongliya.png")
							.putString("score", "30");
		
		JsonObject reply = new JsonObject()
							.putString("type", "OneMoreTime")
							.putString("name", "再来一次卡")
							.putObject("winner", winner);
		
		_CommandApi.showEndCard command = new _CommandApi.showEndCard();
		command.setEndInfo(reply);
		command.setCard(card);
		command.setCountdown(20);

		String commandStr = _CommandApi.ShowEndCard.getRequestMapper().encode(
				Message.message("chat.showEndCard", command)
		);
		
		//System.out.println(commandStr);
		
		chatRoom.publish(commandStr, null);		
		
		
//		HttpClient client = env.getVertx().createHttpClient().setHost("vls.whonow.cn").setPort(8086);
//		HttpClientRequest clientRequest = client.post("/app/product/getInteractionResult", new Handler<HttpClientResponse>() {
//			public void handle(HttpClientResponse resp) {
//				resp.dataHandler(new Handler<Buffer>(){
//					@Override
//					public void handle(Buffer buffer) {
//						final JsonObject reply = new JsonObject(buffer.toString());
//						
//						_CommandApi.showEndCard command = new _CommandApi.showEndCard();
//						command.setEndInfo(reply);
//						command.setCard(card);
//						command.setCountdown(20);
//
//						String commandStr = _CommandApi.ShowEndCard.getRequestMapper().encode(
//								Message.message("product.showEndCard", command)
//						);
//								
//						chatRoom.publish(commandStr, null);
//					}
//				});
//			}
//		});
//		
//		clientRequest.setChunked(true);
//		clientRequest.write("interactionId=" + this.interactionId);
//		clientRequest.end();
	}
}