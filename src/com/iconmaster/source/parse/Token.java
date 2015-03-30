package com.iconmaster.source.parse;

import com.iconmaster.source.util.Range;

/**
 * A Source token. Forms the Source abstract syntax tree. Created via methods in
 * Tokenizer and Parser.
 *
 * @author iconmaster
 */
public class Token {
	public Token(TokenType type, String data, Range range) {
		this.data = data;
		this.type = type;
		this.range = range;
	}
	
	public String data;
	public TokenType type;
	public Range range;
	public Token l;
	public Token r;

	public Token(TokenType type, String data, Range range, Token l, Token r) {
		this(type, data, range);
		this.l = l;
		this.r = r;
	}
	
	@Override
	public String toString() {
		return "Token{" + "data='" + data + "', type=" + type + ", l=" + l + ", r=" + r + '}';
	}
}
