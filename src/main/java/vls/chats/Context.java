/**
 * Copyright (C) 2014 Hongfu Inc. All rights reserved.
 */

package vls.chats;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.mongodb.MongoClientURI;

public class Context {
	public class AppConfig {
		private static final long DEFAULT_TIMEOUT = 120000;
		private static final long DEFAULT_LATENCY_TIMEOUT = 3000;
		private static final int DEFAULT_CHAT_INTERVAL = 5;

		private static final int DEFAULT_MESSAGE_BLOCK_SIZE = 100;
		// 1 minute
		private static final long DEFAULT_INACTIVE_TIME = 600000;
		// 10 minute
		private static final long DEFAULT_SCHEDULE_INTERVAL = 60000;
		
		private String secretKey		= null;
		private String cookieName		= null;
		private MongoClientURI mongoUri = null;
		
		private int messageBlockSize	= -1;
		private long inactiveTime = -1;
		private long scheduleInterval = -1;
		
		public int socketPort(){
			return Integer.parseInt(getSysProperties().getProperty("socket-port"));
		}

		public int restPort(){
			return Integer.parseInt(getSysProperties().getProperty("rest-port"));
		}
		
		public int chatInterval(){
			int val =  Integer.parseInt(getSysProperties().getProperty("chat-interval"));
			return val > 0 ? val : DEFAULT_CHAT_INTERVAL;
		}
		
		public long latencyTimeout(){
			long val = Long.parseLong(getSysProperties().getProperty("latency-timeout"));
			return val > 0 ? val : DEFAULT_LATENCY_TIMEOUT;
		}
		
		public long timeout(){
			long val = Long.parseLong(getSysProperties().getProperty("timeout"));
			return val > 0 ? val : DEFAULT_TIMEOUT;
		}		
		
		public String mapPrefix() {
			return getSysProperties().getProperty("map-prefix");
		}
		
		public String secretKey(){
			if(secretKey == null){
				secretKey = getSysProperties().getProperty("secret-key");
			}
			return secretKey;
		}
		
		public String cookieName(){
			if(cookieName == null){
				cookieName = getSysProperties().getProperty("cookie-name");
			}
			return cookieName;
		}
		
		public MongoClientURI mongoUri(){
			if(mongoUri == null){
				mongoUri = new MongoClientURI(getSysProperties().getProperty("mongo-uri"));
			}
			return mongoUri;			
		}
		
		public boolean isVisitorChatable(){
			return true;
		}
		
		public int messageBlockSize() {
			if (this.messageBlockSize == -1) {
				int val = Integer.parseInt(getSysProperties().getProperty("message-block-size"));
				this.messageBlockSize = val > 0 ? val
						: DEFAULT_MESSAGE_BLOCK_SIZE;
			}
			return messageBlockSize;
		}

		public long inactiveTime() {
			if (this.inactiveTime == -1) {
				long val = Long.parseLong(getSysProperties().getProperty("inactive-time"));
				this.inactiveTime = val > 0 ? val
						: DEFAULT_INACTIVE_TIME;
			}
			return inactiveTime;
		}		
		
		public long scheduleInterval() {
			if (this.scheduleInterval == -1) {
				long val = Long.parseLong(getSysProperties().getProperty("schedule-interval"));
				this.scheduleInterval = val > 0 ? val
						: DEFAULT_SCHEDULE_INTERVAL;
			}
			return scheduleInterval;
		}		
	}

	private static Context instance = new Context();
	private Logger logger = Logger.getLogger(Context.class);	
	private static final String BAD_WORDS_FILE_NAME = "badwords.txt";
	private static final String CONF_PROPS_FILE_NAME = "conf.properties";
	private final ClassLoader platformClassLoader;
	private List<String> badWordList;
	private Properties properties;
	private AppConfig config;

	private Context() {
		ClassLoader tccl = Thread.currentThread().getContextClassLoader();
		this.platformClassLoader = tccl != null ? tccl : getClass()
				.getClassLoader();
		this.config = new AppConfig();
	}

	public static Context instance() {
		if(instance == null){
			instance = new Context();
		}
		return instance;
	}

	public AppConfig appConfig() {
		return config;
	}
	
	public List<String> getBadWordList() {
		if (badWordList == null) {
			badWordList = new ArrayList<String>();
			InputStream is = platformClassLoader
					.getResourceAsStream(BAD_WORDS_FILE_NAME);
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(is));
				String tempString = null;
				while ((tempString = reader.readLine()) != null) {
					badWordList.add(tempString);
				}
				reader.close();
			} catch (IOException e) {
				logger.error("An error occured while reading bad word list.", e);
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e1) {
						logger.error("An error occured while closing reader.",e1);
					}
				}
			}
		}
		return badWordList;
	}
	
	private Properties getSysProperties() {
		if (properties == null) {
			properties = new Properties();
			InputStream is = platformClassLoader.getResourceAsStream(CONF_PROPS_FILE_NAME);
			try {
				properties.load(is);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return properties;
	}	
}