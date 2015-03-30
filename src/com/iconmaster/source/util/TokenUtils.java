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
		while (input.type==type) {
			input = input.l;
			if (input.r!=null) {
				a.add(input.r);
			}
		}
		a.add(input);
		return a;
	}
}
