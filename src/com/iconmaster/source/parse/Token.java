package com.iconmaster.source.parse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Token other = (Token) obj;
		if (!Objects.equals(this.data, other.data)) {
			return false;
		}
		if (this.type != other.type) {
			return false;
		}
		if (!Objects.equals(this.l, other.l)) {
			return false;
		}
		return Objects.equals(this.r, other.r);
	}

	@Override
	public String toString() {
		return "Token{" + "data=" + data + ", type=" + type + ", l=" + l + ", r=" + r + '}';
	}
}
