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
	
	public static class UnaryOpMatcher implements ParseMatcher {
		TokenType type = TokenType.SYMBOL;
		String op;
		
		public UnaryOpMatcher(String op) {
			this.op = op;
		}

		public UnaryOpMatcher(TokenType type, String op) {
			this.op = op;
			this.type = type;
		}

		@Override
		public boolean valid(TokenType type, List<Token> tokens) {
			return tokens.size() >= 2 && op.equals(tokens.get(0).data) && this.type==tokens.get(0).type;
		}

		@Override
		public MatchResult transform(TokenType type, List<Token> tokens) {
			return new MatchResult(new Token(type, op, Range.from(tokens.get(0).range, tokens.get(1).range), tokens.get(1), null), 2);
		}
	}
	
		public static class BinOpMatcher implements ParseMatcher {
		TokenType type = TokenType.SYMBOL;
		String op;
		
		public BinOpMatcher(String op) {
			this.op = op;
		}

		public BinOpMatcher(TokenType type, String op) {
			this.op = op;
			this.type = type;
		}

		@Override
		public boolean valid(TokenType type, List<Token> tokens) {
			return tokens.size() >= 3 && op.equals(tokens.get(1).data);
		}

		@Override
		public MatchResult transform(TokenType type, List<Token> tokens) {
			return new MatchResult(new Token(type, op, Range.from(tokens.get(0).range, tokens.get(2).range), tokens.get(0), tokens.get(2)), 3);
		}
	}
	
	public boolean valid(TokenType type, List<Token> tokens);
	public MatchResult transform(TokenType type, List<Token> tokens);
}
