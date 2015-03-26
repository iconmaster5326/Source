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
	
	public String data;
	public TokenType type;
	public Token l;
	public Token r;
}
