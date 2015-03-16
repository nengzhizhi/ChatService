package vls.chats;

import base.bus.Message;
import base.bus.Message.MessageSpec;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/*
 * 定义websocket协议通信的消息格式
 */

public class _CommandApi{
	public static final MessageSpec<connect,connect.Result> Connect = 
			Message.newRequestSpec("chat.connect", connect.class, connect.Result.class);

	public static final MessageSpec<ping,ping.Result> Ping = 
			Message.newRequestSpec("chat.ping", ping.class, ping.Result.class);	

	public static final MessageSpec<pong,pong.Result> Pong = 
			Message.newRequestSpec("chat.pong", pong.class, pong.Result.class);		
		
	public static final MessageSpec<consume,consume.Result> Consume =
			Message.newRequestSpec("chat.consume", consume.class, consume.Result.class); 
	
	public static final MessageSpec<dialogue,dialogue.Result> Dialogue =
			Message.newRequestSpec("chat.dialogue", dialogue.class, dialogue.Result.class); 
	
	public static final MessageSpec<socket_interrupt,socket_interrupt.Result> Socket_Interrupt = 
			Message.newRequestSpec("chat.socket_interrupt", socket_interrupt.class, socket_interrupt.Result.class);	
	
	public static final MessageSpec<marquee,marquee.Result> Marquee = 
			Message.newRequestSpec("chat.marquee", marquee.class, marquee.Result.class);	
	
	public static final MessageSpec<showBuyCard,showBuyCard.Result> ShowBuyCard = 
			Message.newRequestSpec("chat.showBuyCard", showBuyCard.class, showBuyCard.Result.class);

	public static final MessageSpec<showUseCard,showUseCard.Result> ShowUseCard = 
			Message.newRequestSpec("chat.showUseCard", showUseCard.class, showUseCard.Result.class);	
	
	public static final MessageSpec<showEndCard,showEndCard.Result> ShowEndCard = 
			Message.newRequestSpec("chat.showEndCard", showEndCard.class, showEndCard.Result.class);		
	
	public static class connect{
		public String roomId;
		public String cookie;

		public static class Result{
			public String status;
			public String token;
			public int chatterCount;
		}
	}
	
	public static class ping{
		public String token;
		public String roomId;
		
		public static class Result{
			public String status;
			public long pong;
			public int chatterCount;
			public int managerCount;
		}
	}
	
	public static class pong{
		public String status;
		
		public pong setStatus(String status){
			this.status = status;
			return this;
		}
		
		public static class Result{}		
	}
	


	public static class consume{
		public String type;
		public String roomId;
		public String username;
		public String uid;
		public String productId;
		public String actorId;
		public String count;
		public String cookie;
		
		public consume(){}
		public consume(String type , String roomId , String username , String uid , String productId , String actorId , String count){
			this.type = type;
			this.roomId = roomId;
			this.username = username;
			this.uid = uid;
			this.productId = productId;
			this.actorId = actorId;
			this.count = count;
		}
		
		public static class Result{}
	}
	
	
	public static class dialogue{
		public String roomId;
		public String token;
		public String message;
		public String cookie;
		
		public static class Result{		
			public String message;
			public String time;
			public String username;
			public String roomId;
			public String status;
			
			public Result(){}
			public Result(String message, String nowString, String username, String roomId,String status){
				this.message = message;
				this.time = nowString;
				this.username = username;
				this.roomId = roomId;
				this.status = status;
			}
		}
	}	
	
	public static class socket_interrupt{
		public String textHandlerID;
		public static class Result{
		}
	}
	
	public static class showBuyCard{
		public String interactionId;
		public JsonObject card;
		public int countdown;
		public JsonArray actors;
		
		public showBuyCard(){}
		public showBuyCard(String interactionId, JsonObject card, int countdown, JsonArray actors){
			this.interactionId = interactionId;
			this.card = card;
			this.countdown = countdown;
			this.actors = actors;
		}
		
		public static class Result{
			
		}
	}
	
	public static class showUseCard{
		public static class Result{
		}		
	}
	
	public static class showEndCard{
		public JsonObject card;
		public JsonObject endInfo;
		public int countdown;

		public JsonObject getCard() {
			return card;
		}

		public void setCard(JsonObject card) {
			this.card = card;
		}		
		
		public int getCountdown() {
			return countdown;
		}


		public void setCountdown(int countdown) {
			this.countdown = countdown;
		}


		public JsonObject getEndInfo() {
			return endInfo;
		}


		public void setEndInfo(JsonObject endInfo) {
			this.endInfo = endInfo;
		}


		public static class Result{
		}		
	}
	
	
	public static class marquee{
		public String roomId;
		public String type;
		public String message;
		
		public marquee(){}
		public marquee(String roomId, String type, String message){
			this.roomId = roomId;
			this.type = type;
			this.message = message;
		}
		
		public static class Result{
		}
	}
}	