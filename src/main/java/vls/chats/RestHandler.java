package vls.chats;

import java.lang.reflect.Array;
import java.util.List;

import io.netty.handler.codec.http.HttpHeaders;

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
import vls.chats.models.Manager;
import vls.chats.utils.HttpSession;
import vls.chats.utils.Strings2;



public class RestHandler {
	public RouteMatcher httpRouteMatcher;
	public AppEnvironment env;
	private Repository repo;
	/**
	 * 
	 * Mapping of (token, chatter).
	 */
	protected ConcurrentSharedMap<String, Connector> connectorsMap;
	/**
	 * Mapping of (username+"-"+roomId,manager).
	 */
	protected ConcurrentSharedMap<String, Manager> managersMap;
	/**
	 * Mapping of (token, timerID).
	 */
	protected ConcurrentSharedMap<String, Long> timeoutMap;
	/**
	 * Mapping of (chatRoom-id, chatRoom).
	 */
	protected ConcurrentSharedMap<String, ChatRoom> chatroomsMap;
	/**
	 * Mapping of (username+"-"+roomId, silence).
	 */
	protected ConcurrentSharedMap<String, Silence> blackListMap;
	/**
	 * Mapping of (textHandlerID, token).
	 */
	protected ConcurrentSharedMap<String, String> textHandlerIDToTokenMap;
	
	
	public RestHandler(final AppEnvironment env){
		repo = new Repository(env);
		repo.readRooms();
		repo.readManagers();
		
		this.env = env;
		this.connectorsMap = env.getVertx().sharedData().getMap(env.getMapPrefix() + "connectors");
		this.managersMap = env.getVertx().sharedData().getMap(env.getMapPrefix() + "managers");
		this.chatroomsMap = env.getVertx().sharedData().getMap(env.getMapPrefix() + "chat-rooms");
		this.timeoutMap = env.getVertx().sharedData().getMap(env.getMapPrefix() + "timeouts");
		this.blackListMap = env.getVertx().sharedData().getMap(env.getMapPrefix() + "blackListMap");
		this.textHandlerIDToTokenMap = env.getVertx().sharedData().getMap(env.getMapPrefix() + "textHandlerID-to-token");
		
		httpRouteMatcher = new RouteMatcher()
								.post("/admin/chat/broadcast", new onBroadcast())
								.post("/admin/chat/singlecast", new onSinglecast())
								.post("/admin/chat/setManager", new onSetManager())
								.post("/admin/chat/getManagers", new onGetManagers())
								.post("/admin/chat/getConnectors", new onGetConnectors())
								.post("/admin/chat/getRooms", new onGetRooms())
								.post("/admin/chat/showBuyCard", new onShowBuyCard())
								.post("/vls/chat/getOnlineManagers", new onGetOnlineManagers())
								.post("/vls/chat/getChatters", new onGetChatters())
								.post("/vls/chat/silence",new onSilence())
								.get("/vls/chat/getPlayList/.*", new onGetPlayList())
								.get("/vls/chat/getFlvPath/.*", new onGetFlvPath())
								.get("/crossdomain.xml", new onGetCrossDomainFile())
								//temp interface
								.get("/vls/chat/getRecords", new onGetRecords());
		
	}
	
	public static CommandChecks checks(){
		return new CommandChecks();
	}
	
	public class onBroadcast implements Handler<HttpServerRequest>{
		@Override
		public void handle(final HttpServerRequest request) {
			request.expectMultiPart(true);
			request.endHandler(new VoidHandler() {
				@Override
			    public void handle() {
					try{
						String roomId = request.formAttributes().get("roomId");
						String broadcastData = request.formAttributes().get("broadcastData");
						String ignore = request.formAttributes().get("ignore");

						System.out.println("roomId = "+ roomId);
						System.out.println("broadcastData = "+ broadcastData);
						
						ChatRoom chatroom = chatroomsMap.get(roomId);
						checks().RoomNoExist.check(chatroom != null);
						chatroom.publish2(broadcastData,ignore);
						
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
	
	public class onSinglecast implements Handler<HttpServerRequest>{
		@Override
		public void handle(final HttpServerRequest request) {
			request.expectMultiPart(true);
			request.endHandler(new VoidHandler() {
				@Override
			    public void handle() {
					try{
						String roomId = request.formAttributes().get("roomId");
						String username = request.formAttributes().get("username");
						String singlecastData = request.formAttributes().get("singlecastData");

						System.out.println("singlecastData = "+ singlecastData);
						System.out.println("username = "+ username);
						
						ChatRoom chatroom = chatroomsMap.get(roomId);
						checks().RoomNoExist.check(chatroom != null);
						
						Chatter chatter = chatroom.getChatter(username);
						checks().ChatterNoExist.check(chatter != null);
						
						List<Connector> connectors = chatter.getConnectors();
						
						for(int i = 0; i< connectors.size(); i++){
							connectors.get(i).subscribe(singlecastData);
						}
						System.out.println(connectors.size());
				
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
	
	public class onSetManager implements Handler<HttpServerRequest>{
		@Override
		public void handle(final HttpServerRequest request) {
			request.expectMultiPart(true);
			request.endHandler(new VoidHandler() {
				@Override
			    public void handle() {
					try{
						String roomId = request.formAttributes().get("roomId");
						String username = request.formAttributes().get("username");
						String uid = request.formAttributes().get("uid");
						
						ChatRoom chatroom = chatroomsMap.get(roomId);
						checks().RoomNoExist.check(chatroom!=null);
						checks().InvalidInput.check(!Strings2.isNullOrWhiteSpace(username));
						checks().InvalidInput.check(!Strings2.isNullOrWhiteSpace(uid));
						
						repo.writeManager(roomId, username, uid);
						
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
	
	public class onGetManagers implements Handler<HttpServerRequest>{
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
											
						
						request.response().end("{\"data\":" + new JsonObject().putArray("managers",new Repository(env).getManagers(roomId)).toString()+"}");
					}
					catch(Throwable e){
						e.printStackTrace();
						request.response().end("{\"data\":"+new JsonObject().putString("status","fail").toString()+"}");
					}
				}
			});
		}
	}	
	
	public class onGetOnlineManagers implements Handler<HttpServerRequest>{
		@Override
		public void handle(final HttpServerRequest request) {
			request.expectMultiPart(true);
			request.endHandler(new VoidHandler() {
				@Override
			    public void handle() {
					try{
						String roomId = request.formAttributes().get("roomId");
						
						ChatRoom chatroom = chatroomsMap.get(roomId);
						checks().RoomNoExist.check(chatroom!=null);
						
						JsonArray managerArray = chatroom.getManagers();
						request.response().end("{\"data\":"+new JsonObject().putArray("managers", managerArray).toString()+"}");						
						
					}
					catch(Throwable e){
						request.response().end("{\"data\":"+new JsonObject().putString("status","fail").toString()+"}");
					}
				}
			});
		}
	}
	
	public class onSilence implements Handler<HttpServerRequest>{
		@Override
		public void handle(final HttpServerRequest request) {
			request.expectMultiPart(true);
			request.endHandler(new VoidHandler() {
				@Override
			    public void handle() {
					try{
						String cookieValue = request.headers().get(HttpHeaders.Names.COOKIE);
						System.out.println(cookieValue);
						HttpSession session = getSessionFromCookieValue(cookieValue);
						
						if(Strings2.isNullOrWhiteSpace(session.get("username"))){
							checks().NotLogin.check(false);
						}
						else{
							String roomId = request.formAttributes().get("roomId");
							String target = request.formAttributes().get("target");
							int duration = Integer.parseInt(request.formAttributes().get("duration"));							
							
							ChatRoom chatroom = chatroomsMap.get(roomId);
							checks().RoomNoExist.check(chatroom!=null);
							checks().NotRoomManager.check(managersMap.containsKey(session.get("username")+"-"+roomId));
							
							Silence silence = new Silence(target,DateTime.now(),duration);
							blackListMap.put(target+"-"+roomId, silence);
							
							request.response().end("{\"data\":"+new JsonObject().putString("status","success").toString()+"}");
						}
					}
					catch(Throwable e){
						e.printStackTrace();
						request.response().end("{\"data\":"+new JsonObject().putString("status","fail").toString()+"}");
					}
				}
			});
		}
	}	
	
	public class onGetChatters implements Handler<HttpServerRequest>{
		@Override
		public void handle(final HttpServerRequest request) {
			request.expectMultiPart(true);
			request.endHandler(new VoidHandler() {
				@Override
			    public void handle() {
					try{
						String roomId = request.formAttributes().get("roomId");
						int start = Integer.parseInt(request.formAttributes().get("start"));
						int count = Integer.parseInt(request.formAttributes().get("count"));
						
						ChatRoom chatRoom = chatroomsMap.get(roomId);
						checks().RoomNoExist.check(chatRoom!=null);
						
						request.response().end("{\"data\":"+new JsonObject().putArray("chatters", chatRoom.getChatters(start, count)).toString()+"}");
					}
					catch(Throwable e){
						e.printStackTrace();
						request.response().end("{\"data\":"+new JsonObject().putString("status","fail").toString()+"}");
					}
				}
			});
		}
	}
	
	public class onShowBuyCard implements Handler<HttpServerRequest>{
		@Override
		public void handle(final HttpServerRequest request) {
			request.expectMultiPart(true);
			request.endHandler(new VoidHandler() {
				@Override
			    public void handle() {
					try{	
						final String roomId = request.formAttributes().get("roomId");
						final String productId = request.formAttributes().get("productId");
						final ChatRoom chatRoom = chatroomsMap.get(roomId);
						checks().RoomNoExist.check(chatRoom!=null);	
						
						HttpClient client = env.getVertx().createHttpClient().setHost("vls.whonow.cn").setPort(8086);
						HttpClientRequest clientRequest = client.post("/app/product/createInteraction", new Handler<HttpClientResponse>() {
							public void handle(HttpClientResponse resp) {
								resp.dataHandler(new Handler<Buffer>(){
									@Override
									public void handle(Buffer buffer) {
										final JsonObject reply = new JsonObject(buffer.toString());
										
										final InteractiveTool interaction = new InteractiveTool(
																				env, 
																				chatRoom, 
																				reply.getString("id"), 
																				reply.getObject("card"),
																				reply.getArray("actors")
																			);
										
										//interaction.StartBuyCardStage();
										interaction.StartEndCardStage();
										request.response().end("{\"data\":"+new JsonObject().putString("status","success").toString()+"}");
									}
								});
							}
						});
						
						clientRequest.setChunked(true);
						clientRequest.write("roomId=" + roomId +"&productId=" + productId);
						clientRequest.end();						
					}
					catch(Throwable e){
						e.printStackTrace();
						request.response().end("{\"data\":"+new JsonObject().putString("status","fail").toString()+"}");
					}
				}
			});
		}
	}	
	
	public class onGetConnectors implements Handler<HttpServerRequest>{
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
						
						JsonArray result = new JsonArray();
						Object[] connectorArray = connectorsMap.values().toArray();
						
						for(int i = 0;i<connectorsMap.size();i++){
							JsonObject c = new JsonObject();
							c.putString("token", ((Connector)connectorArray[i]).getToken());
							c.putString("username", ((Connector)connectorArray[i]).getUsername());
							result.add(c);
						}
						request.response().end("{\"data\":"+new JsonObject().putArray("connectors", result).toString()+"}");
					}
					catch(Throwable e){
						e.printStackTrace();
						request.response().end("{\"data\":"+new JsonObject().putString("status","fail").toString()+"}");
					}
				}
			});
		}
	}

	public class onGetRooms implements Handler<HttpServerRequest>{
		@Override
		public void handle(final HttpServerRequest request) {
			request.expectMultiPart(true);
			request.endHandler(new VoidHandler() {
				@Override
			    public void handle() {
					try{
						request.response().end("{\"data\":"+new JsonObject().putArray("rooms", repo.getRooms()).toString()+"}");
					}
					catch(Throwable e){
						e.printStackTrace();
						request.response().end("{\"data\":"+new JsonObject().putString("status","fail").toString()+"}");
					}
				}
			});
		}
	}
	
	public class onGetPlayList implements Handler<HttpServerRequest>{
		@Override
		public void handle(final HttpServerRequest request) {

			request.endHandler(new VoidHandler() {
	            public void handle() {
					String params[] = request.uri().split("/vls/chat/getPlayList/");						
					String youku_getPlayList = "/player/getPlayList/" + params[1];
					
					
					HttpClient client = env.getVertx().createHttpClient().setHost("v.youku.com");
					client.getNow(youku_getPlayList, new Handler<HttpClientResponse>() {
						public void handle(HttpClientResponse resp) {
							
							final Buffer body = new Buffer(0);
							
							resp.dataHandler(new Handler<Buffer>(){
								@Override
								public void handle(Buffer buffer) {
									body.appendBuffer(buffer);
								}
							});
							
							resp.endHandler(new VoidHandler() {
								public void handle() {
									request.response().end(body.toString());
								}
							});
						}
					});
					
	            }
	        });
		}
	}
	
	public class onGetFlvPath implements Handler<HttpServerRequest>{
		@Override
		public void handle(final HttpServerRequest request) {

			request.endHandler(new VoidHandler() {
	            public void handle() {
					String params[] = request.uri().split("/vls/chat/getFlvPath/");						
					String youku_getPlayList = "/player/getFlvPath/" + params[1];
					
					
					HttpClient client = env.getVertx().createHttpClient().setHost("k.youku.com");
					client.getNow(youku_getPlayList, new Handler<HttpClientResponse>() {
						public void handle(HttpClientResponse resp) {
							
							final Buffer body = new Buffer(0);
							
							resp.dataHandler(new Handler<Buffer>(){
								@Override
								public void handle(Buffer buffer) {
									body.appendBuffer(buffer);
								}
							});
							
							resp.endHandler(new VoidHandler() {
								public void handle() {
									request.response().end(body.toString());
								}
							});
						}
					});
					
	            }
	        });
		}
	}

	public class onGetCrossDomainFile implements Handler<HttpServerRequest>{
		@Override
		public void handle(final HttpServerRequest request) {
			request.endHandler(new VoidHandler() {
	            public void handle() {
	            	request.response().sendFile("web/crossdomain.xml");
	            }
			});
		}
	}
	
	public class onGetRecords implements Handler<HttpServerRequest>{
		@Override
		public void handle(final HttpServerRequest request) {
			request.endHandler(new VoidHandler() {
	            public void handle() {
	            	String roomId = "544e007ab99a88bc8b6c401e";
	            	JsonArray records = repo.readRecordsJsonArray(roomId);
					
					request.response().end("{\"data\":" + new JsonObject().putArray("records",records).toString()+"}");
	            }
			});
		}
	}	
	
	
	public HttpSession getSessionFromCookieValue(String cookieValue){
		if(!Strings2.isNullOrWhiteSpace(cookieValue)){
			HttpSession session = HttpSession.builder(Context.instance().appConfig().secretKey(), Context.instance().appConfig().cookieName()).buildFromCookieValue(cookieValue);
			return session;
		}
		else{
			return HttpSession.builder(Context.instance().appConfig().secretKey(), Context.instance().appConfig().cookieName()).buildNewEmptySession();
		}
	}	
}
