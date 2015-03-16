package vls.chats.utils;

import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;

import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import vls.chats.utils.Crypto;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

/**
* 移植出playframework 1的play.mvc.Session
*/
public class WnsSession {
	private static Pattern sessionParser = Pattern
			.compile("\u0000([^:]*):([^\u0000]*)\u0000");
	private static final String AT_KEY = "___AT";
	private static final String ID_KEY = "___ID";
	private static final String TS_KEY = "___TS";
	private String secretKey;
	private static Logger log = Logger.getLogger(WnsSession.class);
	private Map<String, String> data = new LinkedHashMap<String, String>(); // ThreadLocal access
	boolean changed = false;

	public WnsSession(String secretKey) {
		this.secretKey = secretKey;
	}

	/**
	 * String secretKey=config.getProperty("application.secret")
	 * String cookieHeader=headers.getHeader(io.netty.handler.codec.http.HttpHeaders.Names.COOKIE);
	 * WnsSession cookies = decode(secretKey,"PLAY_SESSION",cookieHeader);
	 * 
	 * @param secretKey 应传入Play.secretKey参数
	 * 
	 */
	public static WnsSession decode(String secretKey, String cookieNameOfSession,String cookieHeader) {
		WnsSession result = new WnsSession(secretKey);
 		try {
			String sessionCookieValue="";
			{
				Set<Cookie> cookies = CookieDecoder.decode(cookieHeader);
 				for (Cookie cookie : cookies) {
					if(Objects.equal(cookie.getName(),cookieNameOfSession)){
						sessionCookieValue=cookie.getValue();
						break;
					}
				}

				if(Strings.isNullOrEmpty(sessionCookieValue)){
					return result;
				}
			}
			
  			int firstDashIndex = sessionCookieValue.indexOf("-");
  			if(firstDashIndex<0){
  				return result;
  			}
  			
			String sign = sessionCookieValue.substring(0, firstDashIndex);
			String data = sessionCookieValue.substring(firstDashIndex + 1);
			if (sign.equals(Crypto.sign(data, secretKey))) {
				String sessionData = URLDecoder.decode(data, "utf-8");
				Matcher matcher = sessionParser.matcher(sessionData);
				while (matcher.find()) {
					result.put(matcher.group(1), matcher.group(2));
				}
			}
		} catch (Exception e) {
			log.error("error session:" + cookieHeader, e);
		}
		return result;
	}

	/**
	 * 测试环境的header:
	 * _gir-request-timespan:10
	 * Cache-Control:no-cache
	 * Content-Length:0
	 * Content-Type:text/plain; charset=utf-8
	 * Location:http://192.168.1.200:9992/hfers/admin/index
	 * Server:Play! Framework;1.2.5;dev
	 * Set-Cookie:PLAY_FLASH=;Expires=Thu, 12-Dec-2013 10:54:40 GMT;Path=/
	 * Set-Cookie:PLAY_ERRORS=;Expires=Thu, 12-Dec-2013 10:54:40 GMT;Path=/
	 * Set-Cookie:PLAY_SESSION=1d93ceb48e0a590cc74bdc15ab414f172908ee6f-%00aid%3A50c592bc6a4340abce4cc858%00;Path=/
	 * 
	 * 使用方式：
	 * httpResponse.addHeader(io.netty.handler.codec.http.HttpHeaders.Names.SET_COOKIE, session.encode());
	 * 
	 * @return 
	 */
	public String encode() {
		throw new RuntimeException("encode() not impl");
	}

	public String getId() {
		if (!data.containsKey(ID_KEY)) {
			data.put(ID_KEY, UUID.randomUUID().toString());
		}
		return data.get(ID_KEY);
	}

	public Map<String, String> all() {
		return data;
	}

	public String getAuthenticityToken() {
		if (!data.containsKey(AT_KEY)) {
			data.put(AT_KEY,
					Crypto.sign(UUID.randomUUID().toString(), secretKey));
		}
		return data.get(AT_KEY);
	}

	void change() {
		changed = true;
	}

 	public void put(String key, String value) {
		if (key.contains(":")) {
			throw new IllegalArgumentException(
					"Character ':' is invalid in a session key.");
		}
		change();
		if (value == null) {
			data.remove(key);
		} else {
			data.put(key, value);
		}
	}

	public void put(String key, Object value) {
		change();
		if (value == null) {
			put(key, (String) null);
		}
		put(key, value + "");
	}

	public String get(String key) {
		return data.get(key);
	}

	public boolean remove(String key) {
		change();
		return data.remove(key) != null;
	}

	public void remove(String... keys) {
		for (String key : keys) {
			remove(key);
		}
	}

	public void clear() {
		change();
		data.clear();
	}

	/**
	 * Returns true if the session is empty,
	 * e.g. does not contain anything else than the timestamp
	 */
	public boolean isEmpty() {
		for (String key : data.keySet()) {
			if (!TS_KEY.equals(key)) {
				return false;
			}
		}
		return true;
	}

	public boolean contains(String key) {
		return data.containsKey(key);
	}

	@Override
	public String toString() {
		return data.toString();
	}
}