package com.iconmaster.source.parse;

import com.iconmaster.source.util.Range;
import java.util.List;

/**
 *
 * @author iconmaster
 */
public interface ParseMatcher {
	public static class MatchResult {
		public Token t;
		public int replace;

		public MatchResult(Token t, int replace) {
			this.t = t;
			this.replace = replace;
		}
	}
	
	public static class BinOpMatcher implements ParseMatcher {
		String op;

		public BinOpMatcher(String op) {
			this.op = op;
		}

		@Override
		public boolean valid(TokenType type, List<Token> tokens) {
			return tokens.size()>=3 && op.equals(tokens.get(1).data);
		}

		@Override
		public MatchResult transform(TokenType type, List<Token> tokens) {
			return new MatchResult(new Token(type, op, Range.from(tokens.get(0).range, tokens.get(2).range), tokens.get(0), tokens.get(2)),3);
		}
		
	}
	
	public boolean valid(TokenType type, List<Token> tokens);
	public MatchResult transform(TokenType type, List<Token> tokens);
}
