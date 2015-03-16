package vls.chats;

import org.joda.time.DateTime;
import org.vertx.java.core.Handler;
import org.vertx.java.core.shareddata.ConcurrentSharedMap;

import base.bus.Message;
import base.bus.Message.MessageSpec;
import base.check.CheckError;
import base.check.CheckException;
import vls.chats.messages.MessageQueue;
import vls.chats.models.Manager;
import vls.chats.models.DialogueMessage;
import vls.chats.utils.HttpSession;
import vls.chats.utils.Strings2;


public class Command{
	private static AppEnvironment env;
	/**
	 * Mapping of (token, connector).
	 */	
	private static ConcurrentSharedMap<String, Connector> connectorsMap;
	/**
	 * Mapping of (username+"-"+roomId,manager).
	 */
	protected static ConcurrentSharedMap<String, Manager> managersMap;	
	/**
	 * Mapping of (token, timerID).
	 */
	private static ConcurrentSharedMap<String, Long> timeoutMap;
	/**
	 * Mapping of (chatRoom-roomId, chatRoom).
	 */
	private static ConcurrentSharedMap<String, ChatRoom> chatroomsMap;
	/**
	 * Mapping of (username+"-"+roomId, silence).
	 */
	private static ConcurrentSharedMap<String, Silence> blackListMap;	
	/**
	 * Mapping of (textHandlerID, token).
	 */
	private static ConcurrentSharedMap<String, String> textHandlerIDToTokenMap;
	
	
	Command(AppEnvironment enviroment){
		env = enviroment;
		connectorsMap = env.getVertx().sharedData()
				.getMap(env.getMapPrefix() + "connectors");
		managersMap = env.getVertx().sharedData()
				.getMap(env.getMapPrefix() + "managers");
		chatroomsMap = env.getVertx().sharedData()
				.getMap(env.getMapPrefix() + "chat-rooms");
		timeoutMap = env.getVertx().sharedData()
				.getMap(env.getMapPrefix() + "timeouts");
		blackListMap = env.getVertx().sharedData().getMap(env.getMapPrefix() + "blackListMap");
		textHandlerIDToTokenMap = env.getVertx().sharedData()
				.getMap(env.getMapPrefix() + "textHandlerID-to-token");
	}
	
	public static Command of(AppEnvironment env){
		return new Command(env);
	}
	
	public static interface CommandTemplate<BODY,REPLY> {
		REPLY execute(BODY cmd,String textHandlerID,String binaryHandlerID);
	}
	
	public static class onConnect<BODY,REPLY> implements CommandTemplate<_CommandApi.connect,_CommandApi.connect.Result>{
		@Override
		public _CommandApi.connect.Result execute(_CommandApi.connect cmd,String textHandlerID,String binaryHandlerID) {
			HttpSession session = getSession(cmd.cookie);
			
			ChatRoom chatRoom = chatroomsMap.get(cmd.roomId);	
			checks().RoomNoExist.check(chatRoom!=null);
			
			Connector connector = new Connector(cmd.roomId,session.get("username"),textHandlerID,binaryHandlerID,env.getVertx().eventBus());

			chatRoom.addConnector(connector);
			
			if(!Strings2.isNullOrWhiteSpace(session.get("username"))){
				Chatter chatter = new Chatter(session.get("username"),session.get("uid"), session.get("avatar"), session.get("level"));

				if(chatRoom.containsChatter(session.get("username"))){
					chatRoom.getChatter(session.get("username")).addConnector(connector);
				}
				else{
					chatRoom.addChatter(session.get("username"),chatter);
					chatRoom.getChatter(session.get("username")).addConnector(connector);
				}
				
				if(managersMap.containsKey(session.get("username") + "-" + cmd.roomId)){
					chatRoom.addManager(session.get("username"), chatter);
				}
			}
			
			connectorsMap.putIfAbsent(connector.getToken(), connector);
			timeoutMap.put(connector.getToken(), createTimer(connector.getToken(), env.getTimeout()));
			textHandlerIDToTokenMap.put(textHandlerID, connector.getToken());
		
			_CommandApi.connect.Result reply = new _CommandApi.connect.Result();
			reply.chatterCount = chatRoom.getConnectorSize();
			reply.status = "success";
			reply.token = connector.getToken();

			return reply;
		}
	}
	
	public static class onSocketInterrupt<BODY,REPLY> implements CommandTemplate<_CommandApi.socket_interrupt,_CommandApi.socket_interrupt.Result>{
		@Override
		public _CommandApi.socket_interrupt.Result execute(_CommandApi.socket_interrupt cmd,String textHandlerID,String binaryHandlerID){
			String token = textHandlerIDToTokenMap.get(textHandlerID);
			
			Connector connector = connectorsMap.get(token);
			if(connector!=null){
				ChatRoom chatRoom = chatroomsMap.get(connector.getRoomId());
				chatRoom.removeConnector(connector);
				connectorsMap.remove(token);
				
				if(!Strings2.isNullOrWhiteSpace(connector.getUsername())){
					Chatter chatter = chatRoom.getChatter(connector.getUsername());
					
					if(chatter.removeConnector(connector) <= 0){
						chatRoom.removeChatter(connector.getUsername());
					}
					
					if(chatRoom.getManagerMap().containsKey(connector.getUsername())){
						chatRoom.removeManager(connector.getUsername());
					}
				}				
				
				textHandlerIDToTokenMap.remove(connector.getTextHandlerID());
				env.getVertx().cancelTimer(timeoutMap.remove(connector.getToken()));	
			}			
			
			_CommandApi.socket_interrupt.Result reply = new _CommandApi.socket_interrupt.Result();
			return reply;
		}
	}
	
	public static class onPing<BODY,REPLY> implements CommandTemplate<_CommandApi.ping,_CommandApi.ping.Result>{
		@Override
		public _CommandApi.ping.Result execute(_CommandApi.ping cmd,String textHandlerID,String binaryHandlerID){
			Connector connector = connectorsMap.get(cmd.token);
			checks().InvalidToken.check(connector!=null);
			
			ChatRoom room = chatroomsMap.get(cmd.roomId);
			checks().RoomNoExist.check(room!=null);			
			
			_CommandApi.ping.Result reply = new _CommandApi.ping.Result();
			
			if(env.getVertx().cancelTimer(timeoutMap.get(cmd.token))){
				timeoutMap.put(cmd.token, createTimer(cmd.token, env.getTimeout()));
				reply.status = "success";
				reply.pong = env.getTimeout();
				reply.chatterCount = room.getConnectorSize();
				reply.managerCount = room.getManagersSize();
			}else{
				checks().InvalidToken.check(false);
			}
			return reply;		
		}
	}

	public static class onSend<BODY,REPLY> implements CommandTemplate<_CommandApi.dialogue,_CommandApi.dialogue.Result>{
		@Override
		public _CommandApi.dialogue.Result execute(_CommandApi.dialogue cmd,String textHandlerID,String binaryHandlerID){
			if(!Context.instance().appConfig().isVisitorChatable())
			{
				//FIXME
				Connector connector = connectorsMap.get(cmd.token);
				
				ChatRoom room = chatroomsMap.get(cmd.roomId);
				checks().RoomNoExist.check(room!=null);			
				
				String message = MessageFilter.filter(cmd.message);
				String nowString = DateTime.now().toString("yyyy-MM-dd HH:mm:ss");
				
				_CommandApi.dialogue.Result reply = new _CommandApi.dialogue.Result(message,nowString,null,cmd.roomId,"success");
				room.publish(			
						_CommandApi.Dialogue.getReplyMapper().encode(
								Message.message("chat.message_push", reply)
							),
							connector
						);
				return reply;
			}else{
				Connector connector = connectorsMap.get(cmd.token);
				checks().InvalidToken.check(connector != null);
				
				ChatRoom room = chatroomsMap.get(cmd.roomId);
				checks().RoomNoExist.check(room != null);
	
				HttpSession session = getSession(cmd.cookie);
				
				if(blackListMap.containsKey(session.get("username") + "-" + cmd.roomId)){
					if(blackListMap.get(session.get("username") + "-" + cmd.roomId).isSilencing()){
						checks().Silencing.check(false);
					}
					else{
						blackListMap.remove(session.get("username") + "-" + cmd.roomId);
					}
				}
	
				String message = MessageFilter.filter(cmd.message);
				DateTime now = DateTime.now();
				String nowString = now.toString("yyyy-MM-dd HH:mm:ss");
	
				DialogueMessage messageDto = new DialogueMessage()
												.message(message)
												.nickname(session.get("username"))
												.created(now.toDate());
				
				MessageQueue.instance().push(messageDto);
				
				_CommandApi.dialogue.Result reply = new _CommandApi.dialogue.Result(message,nowString,session.get("username"),cmd.roomId,"success");
				
				room.publish(			
						_CommandApi.Dialogue.getReplyMapper().encode(
								Message.message("chat.message_push", reply)
							),
							connector
						);		
				return reply;				
			}
		}
	}	
	
	public <BODY,REPLY> String receive(MessageSpec<BODY,REPLY> spec,String raw,CommandTemplate<BODY,REPLY> handle,String textHandlerID,String binaryHandlerID) {
		Message<BODY> m = spec.getRequestMapper().decode(raw);
		try{
			REPLY reply = (REPLY)handle.execute((BODY)m.getBody(),textHandlerID,binaryHandlerID);		
			return spec.getReplyMapper().encode(Message.message(m.getChannel(), reply));
			
		}catch(Throwable e){
			CheckError error=null;
			if( e instanceof CheckException){
    			error=((CheckException)e).getError();
    		}else{
    			//TODO
    			//error=Check.GeneralException.toCheckError(new GeneralException(){{info="Service temporarily unavailable";}}).withDetail(e);
    		}		
			return spec.getReplyMapper().encode(
    				new Message<REPLY>(/*channel=*/m.getChannel(),null,/*body=*/null,error));	
		}
	}
	
	public static CommandChecks checks(){
		return new CommandChecks();
	}
	
	public static HttpSession getSession(String cookieHeader){
		if(!Strings2.isNullOrWhiteSpace(cookieHeader)){
			HttpSession session = HttpSession.builder(Context.instance().appConfig().secretKey(), Context.instance().appConfig().cookieName()).buildFromCookiesHeader(cookieHeader);
			return session;
		}
		else{
			return HttpSession.builder(Context.instance().appConfig().secretKey(), Context.instance().appConfig().cookieName()).buildNewEmptySession();
		}
	}
	
	
	/* 
	 * Create a timer according to given token.
	 * 
	 * @param token
	 *            user's token
	 * @param timeout
	 *            timeout time.
	 * @return timer ID.
	 */
	public static long createTimer(final String token, long timeout) {
		return env.getVertx().setTimer(timeout,
						new Handler<Long>() {
							@Override
							public void handle(Long timerID) {
								final Connector connector = connectorsMap.get(token);

								connector.subscribe(
										_CommandApi.Pong.getRequestMapper().encode(
												Message.message("chat.pong", new _CommandApi.pong().setStatus("success"))
											)
										);
								
								Long newTimerID = env.getVertx().setTimer(env.getLatencyTimeout(),
										new Handler<Long>() {
											@Override
											public void handle(Long event) {
												System.out.println("pong time out");
												//TODO 
												//add connect timeout code 
												disconnectClient(connector);
											}
								});
								
								if (timeoutMap.replace(token, timerID, newTimerID)) {
									env.getVertx().cancelTimer(timerID);
								}
							}
						}
				);

	}
	
	private static void disconnectClient(Connector connector){
		ChatRoom chatRoom = chatroomsMap.get(connector.getRoomId());
		chatRoom.removeConnector(connector);
		
		connectorsMap.remove(connector.getToken());
		
		if(!Strings2.isNullOrWhiteSpace(connector.getUsername())){
			Chatter chatter = chatRoom.getChatter(connector.getUsername());
			
			if(chatter.removeConnector(connector) <= 0){
				chatRoom.removeChatter(connector.getUsername());
			}
			
			if(chatRoom.getManagerMap().containsKey(connector.getUsername())){
				chatRoom.removeManager(connector.getUsername());
			}
		}
		
		textHandlerIDToTokenMap.remove(connector.getTextHandlerID());
		env.getVertx().cancelTimer(timeoutMap.remove(connector.getToken()));	
	}
}

