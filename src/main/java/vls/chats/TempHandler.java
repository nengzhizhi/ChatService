package vls.chats;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.vertx.java.core.Handler;
import org.vertx.java.core.VoidHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.shareddata.ConcurrentSharedMap;

import base.bus.Message;
import vls.chats.db.Repository;
import vls.chats.models.Record;
import vls.chats.utils.HttpSession;
import vls.chats.utils.Strings2;

public class TempHandler {
	public RouteMatcher httpRouteMatcher;
	
	public AppEnvironment env;
	private Repository repo;
	
	/**
	 * Mapping of (chatRoom-id, chatRoom).
	 */
	protected ConcurrentSharedMap<String, ChatRoom> chatroomsMap;
	protected Map<String, Replay> replaysMap;

	public TempHandler(final AppEnvironment env){
		repo = new Repository(env);
		
		this.env = env;
		this.chatroomsMap = env.getVertx().sharedData().getMap(env.getMapPrefix() + "chat-rooms");
		this.replaysMap = new HashMap<String, Replay>();
		
		httpRouteMatcher = new RouteMatcher()
								.post("/admin/chat/getRecords", new onGetRecords())
								.post("/admin/chat/insertRecord", new onInsertRecord())
								.post("/admin/chat/startReplay", new onStartReplay())
								.post("/admin/chat/stopReplay", new onStopReplay())
								.post("/admin/chat/deleteRecords", new onDeleteRecords())
								.post("/admin/chat/marquee", new onMarquee());
	}	
	
	private class Replay {
		public long handle;
		public ChatRoom chatRoom;
		public String roomId;
		public int index;
		public List<Record> records;
		
		private long clock;
		
		public Replay(ChatRoom chatRoom,String roomId){
			this.chatRoom = chatRoom;
			this.roomId = roomId;
			this.records = repo.readRecords(this.roomId);
			this.index = 0;
			this.clock = 0;
		}
		
		public void start(){
			handle = env.getVertx().setPeriodic(100, new Handler<Long>() {
				@Override
				public void handle(Long timerID) {
					while(index < records.size() && records.get(index).getSpan() <= clock){
						if (records.get(index).getType().equals("message")) {
							Message<_CommandApi.dialogue.Result> m = _CommandApi.Dialogue.getReplyMapper().decode(
																		records.get(index).getData());
							
							String data = _CommandApi.Dialogue.getReplyMapper().encode(
									Message.message("chat.message_push", new _CommandApi.dialogue.Result(
											m.getBody().message,
											DateTime.now().toString("yyyy-MM-dd HH:mm:ss"),
											m.getBody().username,
											m.getBody().roomId,
											"success"
										)));
							chatRoom.publish( data , null);
						}
						else if (records.get(index).getType().equals("buyGift") || records.get(index).getType().equals("buyTool")) {
							Message<_CommandApi.consume> m = _CommandApi.Consume.getRequestMapper().decode(
																records.get(index).getData());
							
							String data = "roomId=" + m.getBody().roomId +
										  "&productId=" + m.getBody().productId +
										  "&actorId=" + m.getBody().actorId +
										  "&count=" + m.getBody().count;
							
							HttpClient client = env.getVertx().createHttpClient().setHost("vls.whonow.cn").setPort(8086);
							HttpClientRequest clientRequest = client.post("/app/product/buyGift", new Handler<HttpClientResponse>() {
								public void handle(HttpClientResponse resp) {
									resp.dataHandler(new Handler<Buffer>(){
										@Override
										public void handle(Buffer buffer) {
											//System.out.println("Buffer = " + buffer.toString());
										}
									});
								}
							});
							String secretKey = "wVcvIJpPLLW4UuKjlE5prGLrlZDrzPyRQfXg5hlBOYwtVFwQSkInEXwKw4OPiRFW";
							HttpSession session = HttpSession.builder(secretKey, "VLS_SESSION").buildNewEmptySession();				
							session.put("username", m.getBody().username);
							session.put("uid", m.getBody().uid);
							
							clientRequest.headers().add("cookie", "VLS_SESSION="+session.toCookie().getValue());
							clientRequest.setChunked(true);
							//FIXME
							clientRequest.write(data+"&price=20");
							clientRequest.end();						
						}
						index = index + 1;
					}

					clock = clock + 100;
				}
			});
		}
		
		public void stop(){
			env.getVertx().cancelTimer(handle);
		}
	}
	
	public static CommandChecks checks(){
		return new CommandChecks();
	}
	
	public class onStartReplay implements Handler<HttpServerRequest>{
		@Override
		public void handle(final HttpServerRequest request) {
			request.expectMultiPart(true);
			request.endHandler(new VoidHandler() {
				@Override
			    public void handle() {
					try{
						String roomId = request.formAttributes().get("roomId");						
						ChatRoom chatRoom = chatroomsMap.get(roomId);
						checks().RoomNoExist.check(chatRoom!=null);
						
						Replay replay = new Replay(chatRoom,roomId);
						replaysMap.putIfAbsent(roomId, replay);
						replay.start();
						
						request.response().end("{\"data\":"+new JsonObject().putString("status","success").toString()+"}");
					}
					catch(Throwable e){
						e.printStackTrace();
						request.response().end("{\"data\":"+new JsonObject().putString("status","fail").toString()+"}");
					}
				}
			});
		}
	}
	
	public class onStopReplay implements Handler<HttpServerRequest>{
		@Override
		public void handle(final HttpServerRequest request) {
			request.expectMultiPart(true);
			request.endHandler(new VoidHandler() {
				@Override
			    public void handle() {
					try{
						String roomId = request.formAttributes().get("roomId");						
						ChatRoom chatRoom = chatroomsMap.get(roomId);
						checks().RoomNoExist.check(chatRoom != null);
						
						replaysMap.get(roomId).stop();
						
						request.response().end("{\"data\":" + new JsonObject().putString("status","success").toString() + "}");
					}
					catch(Throwable e){
						e.printStackTrace();
						request.response().end("{\"data\":" + new JsonObject().putString("status","fail").toString() + "}");
					}
				}
			});
		}
	}	
	
	public class onInsertRecord implements Handler<HttpServerRequest>{
		@Override
		public void handle(final HttpServerRequest request) {
			request.expectMultiPart(true);
			request.endHandler(new VoidHandler() {
				@Override
			    public void handle() {
					try{
						String roomId = request.formAttributes().get("roomId");
						long span = Long.parseLong(request.formAttributes().get("span"));
						String type = request.formAttributes().get("type");
						
						ChatRoom chatroom = chatroomsMap.get(roomId);
						checks().RoomNoExist.check(chatroom!=null);
						checks().InvalidInput.check(!Strings2.isNullOrWhiteSpace(type));					

						if(type.equals("message")){
							String message = request.formAttributes().get("message");
							String username = request.formAttributes().get("username");
							String nowString = DateTime.now().toString("yyyy-MM-dd HH:mm:ss");
							String data = _CommandApi.Dialogue.getReplyMapper().encode(
												Message.message(
													"chat.message_push", 
													new _CommandApi.dialogue.Result(
														message,
														nowString,
														username,
														roomId,
														"success"
													)
												)
										);
							
							repo.insertRecord(roomId, span, type, data);
						}
						else if (type.equals("buyTool") || type.equals("buyGift")){
							String username = request.formAttributes().get("username");
							String uid = request.formAttributes().get("uid");
							String productId = request.formAttributes().get("productId");
							String actorId = request.formAttributes().get("actorId");
							String count = request.formAttributes().get("count");												
							
							String data = _CommandApi.Consume.getRequestMapper().encode(
											Message.message(
												"chat."+type,
												new _CommandApi.consume(
														type,
														roomId,
														username,
														uid,
														productId,
														actorId,
														count
												)
											)
										);
							repo.insertRecord(roomId, span, type, data);						
						}					
						request.response().end("{\"data\":" + new JsonObject().putString("status","success").toString() + "}");
					}
					catch(Throwable e){
						e.printStackTrace();
						request.response().end("{\"data\":" + new JsonObject().putString("status","fail").toString() + "}");
					}
				}
			});
		}
	}
	
	public class onGetRecords implements Handler<HttpServerRequest>{
		@Override
		public void handle(final HttpServerRequest request) {
			request.expectMultiPart(true);
			request.endHandler(new VoidHandler() {
				@Override
			    public void handle() {
					try{
						String roomId = request.formAttributes().get("roomId");
						
						ChatRoom chatroom = chatroomsMap.get(roomId);
						checks().RoomNoExist.check(chatroom != null);
						
						JsonArray records = repo.readRecordsJsonArray(roomId);
						
						request.response().end("{\"data\":" + new JsonObject().putArray("records",records).toString()+"}");
					}
					catch(Throwable e){
						e.printStackTrace();
						request.response().end("{\"data\":" + new JsonObject().putString("status","fail").toString()+"}");
					}
				}
			});
		}
	}
	
	public class onDeleteRecords implements Handler<HttpServerRequest>{
		@Override
		public void handle(final HttpServerRequest request) {
			request.expectMultiPart(true);
			request.endHandler(new VoidHandler() {
				@Override
			    public void handle() {
					try{
						String roomId = request.formAttributes().get("roomId");
						
						ChatRoom chatroom = chatroomsMap.get(roomId);
						checks().RoomNoExist.check(chatroom != null);
						
						int index = 0;
						String recordId = request.formAttributes().get("recordIds[" + index + "]");
						while(!Strings2.isNullOrWhiteSpace(recordId)){
							repo.deleteRecord(roomId,recordId);
							index ++ ;
							recordId = request.formAttributes().get("recordIds[" + index + "]");
						}
						
						request.response().end("{\"data\":" + new JsonObject().putString("status","success").toString()+"}");
					}
					catch(Throwable e){
						e.printStackTrace();
						request.response().end("{\"data\":" + new JsonObject().putString("status","fail").toString()+"}");
					}
				}
			});
		}
	}
	
	public class onMarquee implements Handler<HttpServerRequest>{
		@Override
		public void handle(final HttpServerRequest request) {
			request.expectMultiPart(true);
			request.endHandler(new VoidHandler() {
				@Override
			    public void handle() {
					try{
						String roomId = request.formAttributes().get("roomId");
						String type = request.formAttributes().get("type");
						String message = request.formAttributes().get("message");
						
						ChatRoom chatroom = chatroomsMap.get(roomId);
						checks().RoomNoExist.check(chatroom != null);
						
						String data = _CommandApi.Marquee.getRequestMapper().encode(
								Message.message(
									"chat.marquee",
									new _CommandApi.marquee(
											roomId,
											type,
											message
									)
								)
							);
						chatroom.publish(data, null);
						System.out.println("跑马灯！");
						
						request.response().end("{\"data\":" + new JsonObject().putString("status","success").toString()+"}");
					}
					catch(Throwable e){
						e.printStackTrace();
						request.response().end("{\"data\":" + new JsonObject().putString("status","fail").toString()+"}");
					}
				}
			});
		}
	}
}