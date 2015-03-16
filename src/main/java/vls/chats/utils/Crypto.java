package vls.chats.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;

/**
 * 完全替代play.libs.Crypto
 * 
 * 但不应该轻易修改此类的任何加密算法！！！
 * 
 * 
 * 可使用configuration.getProperty("application.secret")获取应用密钥作为secret参数
 */
public class Crypto {
	//chen56:copy from playframework
    static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

     /**
     * chen56:copy from playframework
     * Define a hash type enumeration for strong-typing
     */
    public enum HashType {
        MD5("MD5"),
        SHA1("SHA-1"),
        SHA256("SHA-256"),
        SHA512("SHA-512");
        private String algorithm;
        HashType(String algorithm) { this.algorithm = algorithm; }
        @Override public String toString() { return this.algorithm; }
    }

	public static class StringSign {
		private String secretKey;
		private int hashLength;
        
		private StringSign(){}
        
		public String sign(String message) {
			String sign = Crypto.sign(message, secretKey.getBytes(Charsets.UTF_8));
			return hashLength <= 0 ? sign : sign.substring(0, hashLength);
		}

		public boolean verify(String message, String sign) {
			if (Strings.isNullOrEmpty(message)) {
				return false;
			}
			return Objects.equal(sign, sign(message));
		}
	}

	/**
	 * chen56:copy from playframework
	 * 
	 * Create a password hash using specific hashing algorithm
	 * 
	 * @param input
	 *            The password
	 * @param hashType
	 *            The hashing algorithm
	 * @return The password hash
	 */
	public static String passwordHash(String input, HashType hashType)
	{
		try {
			MessageDigest m = MessageDigest.getInstance(hashType.toString());
			//chen56:use UTF-8
			byte[] out = m.digest(input.getBytes(Charsets.UTF_8));
			//old
//			byte[] out = m.digest(input.getBytes(Charsets.UTF_8));
			return BaseEncoding.base64().encode(out);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
    /**
     * chen56:copy from playframework
     * 
     * Sign a message with a key
     * @param message The message to sign
     * @param secretKey The secretKey to use
     * @return The signed message (in hexadecimal)
     */
    public static String sign(String message, String secretKey) {
    	return sign(message,secretKey.getBytes(Charsets.UTF_8));
    }
    /**
     * chen56:copy from playframework
     * 
     * Sign a message with a key
     * @param message The message to sign
     * @param key The key to use
     * @return The signed message (in hexadecimal)
     */
    public static String sign(String message, byte[] secretKey) {

        if (secretKey.length == 0) {
            return message;
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec signingKey = new SecretKeySpec(secretKey, "HmacSHA1");
            mac.init(signingKey);
            byte[] messageBytes = message.getBytes("utf-8");
            byte[] result = mac.doFinal(messageBytes);
            int len = result.length;
            char[] hexChars = new char[len * 2];


            for (int charIndex = 0, startIndex = 0; charIndex < hexChars.length;) {
                int bite = result[startIndex++] & 0xff;
                hexChars[charIndex++] = HEX_CHARS[bite >> 4];
                hexChars[charIndex++] = HEX_CHARS[bite & 0xf];
            }
            return new String(hexChars);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }
	public static StringSign newSign(String secretKey, int hashLength) {
		StringSign result = new StringSign();
		result.secretKey = secretKey;
		result.hashLength = hashLength;
		return result;
	}
	public static StringSign newSignWithUnlimitLength(String secretKey) {
		return newSign(secretKey, -1);
	}
	
    /**
	 * chen56:copy from playframework
     * Encrypt a String with the AES encryption standard. Private key must have a length of 16 bytes
     * @param value The String to encrypt
     * @param privateKey The key used to encrypt
     * @return An hexadecimal encrypted string
     */
    public static String encryptAES(String value, String privateKey) {
        try {
            byte[] raw = privateKey.getBytes(Charsets.UTF_8);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            return Codec2.byteToHexString(cipher.doFinal(value.getBytes(Charsets.UTF_8)));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    /**
	 * chen56:copy from playframework
     * Decrypt a String with the AES encryption standard. Private key must have a length of 16 bytes
     * @param value An hexadecimal encrypted string
     * @param privateKey The key used to encrypt
     * @return The decrypted String
     */
    public static String decryptAES(String value, String privateKey) {
        try {
            byte[] raw = privateKey.getBytes(Charsets.UTF_8);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            return new String(cipher.doFinal(Codec2.hexStringToByte(value)),Charsets.UTF_8);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}