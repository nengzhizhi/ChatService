package vls.chats.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;



public class Codec2 {
	public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

	public static String encodeURL(String data) {
		if(Strings.isNullOrEmpty(data)){
			return data;
		}

		try {
			return URLEncoder.encode(data, DEFAULT_CHARSET.displayName());
		} catch (UnsupportedEncodingException wontHappen) {
			throw new IllegalStateException(wontHappen);
		}
	}

	public static String decodeURL(String data) {
		if(Strings.isNullOrEmpty(data)){
			return data;
		}
		try {
			return URLDecoder.decode(data, DEFAULT_CHARSET.displayName());
		} catch (UnsupportedEncodingException wontHappen) {
			throw new IllegalStateException(wontHappen);
		}
	}

	public static String encodeBase64(String str){
		Preconditions.checkNotNull(str,"str参数应不为空");
		return new String(Base64.encodeBase64(str.getBytes(DEFAULT_CHARSET)),DEFAULT_CHARSET);
	}
	public static String encodeBase64(byte[] data){
		Preconditions.checkNotNull(data,"data参数应不为空");
		return new String(Base64.encodeBase64(data),DEFAULT_CHARSET);
	}
	public static String dencodeBase64(String str){
		Preconditions.checkNotNull(str,"str参数应不为空");
		return new String(Base64.decodeBase64(str.getBytes(DEFAULT_CHARSET)),DEFAULT_CHARSET);
	}
	public static String base64HmacSHA1(String data, String key) {
		try {
			Mac mac = Mac.getInstance("HmacSHA1");
			SecretKeySpec spec = new SecretKeySpec(key.getBytes(), "HmacSHA1");
			mac.init(spec);
			return encodeBase64(mac.doFinal(data.getBytes()));
		} catch (InvalidKeyException e) {
			throw new RuntimeException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

    /**
     * Build an hexadecimal MD5 hash for a String
     * @param value The String to hash
     * @return An hexadecimal Hash
     */
    public static String hexMD5(String value) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(value.getBytes("utf-8"));
            byte[] digest = messageDigest.digest();
            return byteToHexString(digest);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Build an hexadecimal SHA1 hash for a String
     * @param value The String to hash
     * @return An hexadecimal Hash
     */    
    public static String hexSHA1(String value) {
        try {
            MessageDigest md;
            md = MessageDigest.getInstance("SHA-1");
            md.update(value.getBytes("utf-8"));
            byte[] digest = md.digest();
            return byteToHexString(digest);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
 
    /**
     * Write a byte array as hexadecimal String.
     */
    public static String byteToHexString(byte[] bytes) {
        return String.valueOf(Hex.encodeHex(bytes));
    }

    /**
     * Transform an hexadecimal String to a byte array.
     */
    public static byte[] hexStringToByte(String hexString) {
        try {
            return Hex.decodeHex(hexString.toCharArray());
        } catch (DecoderException e) {
            throw new RuntimeException(e);
        }
    }

	
	public static String hexHMAC(String secret, String data) {
		String HMAC_SHA1_ALGORITHM = "HmacSHA256";
		try {
			SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(),HMAC_SHA1_ALGORITHM);
			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(signingKey);
			byte[] rawHmac = mac.doFinal(data.getBytes());
			String result = new String(Codec2.byteToHexString(rawHmac));
			return result;
		} catch (GeneralSecurityException e) {
 			throw new IllegalArgumentException(e);
		}
	}


}