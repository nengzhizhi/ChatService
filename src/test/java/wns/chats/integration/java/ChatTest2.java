package wns.chats.integration.java;

import static org.vertx.testtools.VertxAssert.assertNotNull;
import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;

import org.junit.Ignore;
import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.WebSocket;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import vls.chats.utils.HttpSession;
import vls.chats.utils.HttpSession.Codec;
import vls.chats.utils.WnsSession;

import java.util.Map;
import java.util.Objects;
@Ignore
public class ChatTest2 extends TestVerticle{
//	@Override
//	public void start(){
//		initialize();
//		System.out.println(System.getProperty("com.hongfu001.vls~vls.chats~3.2.1.0-SNAPSHOT"));
//		container.deployModule(System.getProperty("com.hongfu001.vls~vls.chats~3.2.1.0-SNAPSHOT"), new AsyncResultHandler<String>() {
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
	
	@Test
	public void testSession() {
		System.out.println("test session");
		String secretKey = "wVcvIJpPLLW4UuKjlE5prGLrlZDrzPyRQfXg5hlBOYwtVFwQSkInEXwKw4OPiRFW";

		HttpSession session = HttpSession.builder(secretKey, "VLS_SESSION").buildNewEmptySession();
		session.put("username", "nengzhizhi1");
	}
	
	@Test
	public void testWebSocket() {
		class ChatMessageHandler implements Handler<Buffer> {
						private WebSocket webSocket;
						private String token;
			
						public ChatMessageHandler(WebSocket webSocket) {
							this.webSocket = webSocket;
						}
			
						@Override
						public void handle(Buffer buffer) {
							JsonObject result = new JsonObject(buffer.toString());
							System.out.println(buffer.toString());
//							container.logger().info(buffer.toString());
//							String action = result.getString("action");
//							if (Objects.equals(action, "connect")) {
//								token = result.getString("token");
//								join();
//							} else if (Objects.equals(action, "join")) {
//								sendToMyself();
//							} else if (Objects.equals(action, "send")) {
//								part();
//							}
//							else {
//								testComplete();
//							}
						}
			
					}		
		
		class ChatClientHandler implements Handler<WebSocket> {

			@Override
			public void handle(final WebSocket webSocket) {
				webSocket.dataHandler(new ChatMessageHandler(webSocket));
				
				String secretKey = "wVcvIJpPLLW4UuKjlE5prGLrlZDrzPyRQfXg5hlBOYwtVFwQSkInEXwKw4OPiRFW";
				HttpSession session = HttpSession.builder(secretKey, "VLS_SESSION").buildNewEmptySession();				
				session.put("username", "test");
				session.put("avatar", "nengzhizhi");
				session.put("level", "22");
				session.put("uid", "nengzhizhi");
				
				
				System.out.print(session.toCookie().getValue());
				JsonObject json = new JsonObject()
						.putString("roomId", "1001")
						.putString("cookie", session.toCookie().getValue());

				JsonObject request = new JsonObject()
						.putString("c", "chat.connect")
						.putObject("data", json);
				
				webSocket.writeTextFrame(request.toString());
				//TODO
				//解决.putString("data", json.toString());时服务器抛出的异常
				//TODO
				//解决远程主机关闭链接时抛出的异常
				
			}
		}		
		
		
		this.vertx.createHttpClient()
				.setHost("localhost")
				.setPort(40001)
				.connectWebsocket("/", new ChatClientHandler());
	}
}
