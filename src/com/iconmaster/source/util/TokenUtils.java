package com.iconmaster.source.util;

import com.iconmaster.source.parse.Token;
import com.iconmaster.source.parse.TokenType;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author iconmaster
 */
public class TokenUtils {
	public static List<Token> getTokens(Token input, TokenType type) {
		ArrayList<Token> a = new ArrayList<>();
		if (input==null) {
			return a;
		}
		while (input.type==type) {
			if (input.r!=null) {
				a.add(0,input.r);
			}
			input = input.l;
		}
		a.add(0,input);
		return a;
	}
	
	public static String condense(List<String> li, String sep) {
		String ret = "";
		if (!li.isEmpty()) {
			for (String s : li) {
				ret += sep;
				ret += s;
			}
			ret = ret.substring(sep.length());
		}
		return ret;
	}
}
