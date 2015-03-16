/**
 * Copyright (C) 2013 Hongfu Inc. All rights reserved.
 */

//package gir.chats.integration.java;
//
//import static org.vertx.testtools.VertxAssert.assertNotNull;
//import static org.vertx.testtools.VertxAssert.assertTrue;
//import static org.vertx.testtools.VertxAssert.testComplete;
//
//import java.util.Objects;
//
//import org.junit.Ignore;
//import org.junit.Test;
//import org.vertx.java.core.AsyncResult;
//import org.vertx.java.core.AsyncResultHandler;
//import org.vertx.java.core.Handler;
//import org.vertx.java.core.buffer.Buffer;
//import org.vertx.java.core.http.WebSocket;
//import org.vertx.java.core.json.JsonObject;
//import org.vertx.testtools.TestVerticle;
//
//import wns.chats.Context;
//
//@Ignore
//public class ChatTest extends TestVerticle {
//	@Override
//	public void start() {
//		// Make sure we call initialize() - this sets up the assert stuff so
//		// assert functionality works correctly
//		initialize();
//		// Deploy the module - the System property `vertx.modulename` will
//		// contain the name of the module so you
//		// don't have to hardecode it in your tests
//		System.out.println(System.getProperty("vertx.modulename"));
//		container.deployModule(System.getProperty("vertx.modulename"), new AsyncResultHandler<String>() {
//			@Override
//			public void handle(AsyncResult<String> asyncResult) {
//				// Deployment is asynchronous and this this handler will be
//				// called when it's complete (or failed)
//				assertTrue(asyncResult.succeeded());
//				assertNotNull("deploymentID should not be null", asyncResult.result());
//				// If deployed correctly then start the tests!
//				startTests();
//			}
//		});
//	}
//
//	@Test
//	public void testSocket() {
//		class ChatMessageHandler implements Handler<Buffer> {
//
//			private WebSocket webSocket;
//			private String token;
//
//			public ChatMessageHandler(WebSocket webSocket) {
//				this.webSocket = webSocket;
//			}
//
//			@Override
//			public void handle(Buffer buffer) {
//				JsonObject result = new JsonObject(buffer.toString());
//				container.logger().info(buffer.toString());
//				String action = result.getString("action");
//				if (Objects.equals(action, "connect")) {
//					token = result.getString("token");
//					join();
//				} else if (Objects.equals(action, "join")) {
//					sendToMyself();
//				} else if (Objects.equals(action, "send")) {
//					part();
//				}
//				else {
//					testComplete();
//				}
//			}
//
//			private void join() {
//				JsonObject json = new JsonObject();
//				json.putString("action", "join");
//				json.putString("token", token);
//				json.putString("room", "eric's room");
//				webSocket.writeTextFrame(json.toString());
//			}
//
//			private void part() {
//				JsonObject json = new JsonObject()
//						.putString("action", "part")
//						.putString("token", token)
//						.putString("room", "eric's room");
//				webSocket.writeTextFrame(json.toString());
//			}
//
//			private void send() {
//				JsonObject json = new JsonObject()
//						.putString("action", "send")
//						.putString("token", token)
//						.putString("room", "eric's room")
//						.putString("message", "God damn it! So much work.");
//				webSocket.writeTextFrame(json.toString());
//			}
//
//			private void sendToMyself() {
//				JsonObject json = new JsonObject()
//						.putString("action", "send")
//						.putString("token", token)
//						.putString("nickname", "Eric Gohn")
//						.putString("message", "God damn it! So much work. - message from my self ^-^!");
//				webSocket.writeTextFrame(json.toString());
//			}
//
//		}
//
//		class ChatClientHandler implements Handler<WebSocket> {
//
//			@Override
//			public void handle(final WebSocket webSocket) {
//				webSocket.dataHandler(new ChatMessageHandler(webSocket));
//
//				JsonObject json = new JsonObject()
//						.putString("action", "connect")
//						.putString("nickname", "Eric Gohn");
//				webSocket.writeTextFrame(json.toString());
//			}
//		}
//
//		this.vertx.createHttpClient()
//				.setHost("localhost")
//				.setPort(Context.instance().appConfig().webPort())
//				.connectWebsocket("/", new ChatClientHandler());
//	}
//}
