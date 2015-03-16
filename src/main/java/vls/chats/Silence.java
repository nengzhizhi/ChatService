package vls.chats;

import org.joda.time.DateTime;
import org.vertx.java.core.shareddata.Shareable;

public class Silence implements Shareable{
	private String username;
	private DateTime startTime;
	private int duration;
	
	public Silence(String username,DateTime startTime,int duration){
		this.username = username;
		this.startTime = startTime;
		this.duration = duration;
	}
	
	public boolean isSilencing(){
		DateTime endTime = startTime.plusSeconds(duration);
		return DateTime.now().isBefore(endTime);
	}
	
	public String getUsername(){
		return username;
	}
}
