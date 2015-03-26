package com.iconmaster.source.parse;

import java.util.ArrayList;
import java.util.List;

/**
 * A Source token. Forms the Source abstract syntax tree. Created via methods in
 * Tokenizer and Parser.
 *
 * @author iconmaster
 */
public class Token {
	public static List<TokenType> simples = new ArrayList<>();

	public Token(TokenType type, String data) {
		this.data = data;
		this.type = type;
	}
	
	public String data;
	public TokenType type;
	public Token l;
	public Token r;
	
	@Override
	public String toString() {
		return "Token{" + "data='" + data + "', type=" + type + ", l=" + l + ", r=" + r + '}';
	}
}
