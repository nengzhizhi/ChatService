/**
 * Copyright (C) 2014 Hongfu Inc. All rights reserved.
 */

package vls.chats;

import java.util.HashMap;
import java.util.List;

import com.google.common.base.Strings;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class MessageFilter {
	private static HashMap keysMap;
	private static int MatchType = 1; // 1:最小长度匹配 2：最大长度匹配

	public static String filter(String txt) {
		int l = txt.length();
		for (int i = 0; i < l;) {
			int len = checkKeyWords(txt, i, MatchType);
			if (len > 0) {
				String str = txt.substring(i, i + len);
				i += len;
				txt = txt.replace(str, Strings.repeat("*", len));
			} else {
				i++;
			}
		}
		return txt;
	}

	/**
	 * 检查一个字符串从begin位置起开始是否有keyword符合， 如果有符合的keyword值，返回值为匹配keyword的长度，否则返回零
	 * flag 1:最小长度匹配 2：最大长度匹配
	 */
	private static int checkKeyWords(String txt, int begin, int flag) {
		HashMap nowhash = keysMap();
		int maxMatchRes = 0;
		int res = 0;
		int l = txt.length();
		char word = 0;
		for (int i = begin; i < l; i++) {
			word = txt.charAt(i);
			Object wordMap = nowhash.get(word);
			if (wordMap != null) {
				res++;
				nowhash = (HashMap) wordMap;
				if (((String) nowhash.get("isEnd")).equals("1")) {
					if (flag == 1) {
						wordMap = null;
						nowhash = null;
						txt = null;
						return res;
					} else {
						maxMatchRes = res;
					}
				}
			} else {
				txt = null;
				nowhash = null;
				return maxMatchRes;
			}
		}
		txt = null;
		nowhash = null;
		return maxMatchRes;
	}

	private static HashMap genKeywords(List<String> keywords) {
		HashMap result = new HashMap();
		for (int i = 0; i < keywords.size(); i++) {
			String key = keywords.get(i).trim();
			HashMap nowhash = null;
			nowhash = result;
			for (int j = 0; j < key.length(); j++) {
				char word = key.charAt(j);
				Object wordMap = nowhash.get(word);
				if (wordMap != null) {
					nowhash = (HashMap) wordMap;
				} else {
					HashMap<String, String> newWordHash = new HashMap<String, String>();
					newWordHash.put("isEnd", "0");
					nowhash.put(word, newWordHash);
					nowhash = newWordHash;
				}
				if (j == key.length() - 1) {
					nowhash.put("isEnd", "1");
				}
			}

		}
		return result;
	}

	private static HashMap keysMap() {
		if (keysMap == null) {
			keysMap = genKeywords(Context.instance().getBadWordList());
		}
		return keysMap;
	}
}
