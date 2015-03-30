package com.iconmaster.source.parse;

import com.iconmaster.source.exception.SourceError;
import com.iconmaster.source.util.Range;
import com.iconmaster.source.util.Result;
import java.util.ArrayList;
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
			return tokens.size() >= 2 && op.equals(tokens.get(0).data) && this.type == tokens.get(0).type;
		}

		@Override
		public Result<MatchResult> transform(TokenType type, List<Token> tokens) {
			return new Result<>(new MatchResult(new Token(type, op, Range.from(tokens.get(0).range, tokens.get(1).range), tokens.get(1), null), 2));
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
			return tokens.size() >= 3 && tokens.get(1).type == this.type && op.equals(tokens.get(1).data);
		}

		@Override
		public Result<MatchResult> transform(TokenType type, List<Token> tokens) {
			return new Result<>(new MatchResult(new Token(type, op, Range.from(tokens.get(0).range, tokens.get(2).range), tokens.get(0), tokens.get(2)), 3));
		}
	}
	
	public static class BlockMatcher implements ParseMatcher {

		TokenType type1;
		TokenType type2;
		boolean allowStatements;

		public BlockMatcher(TokenType type1, TokenType type2, boolean allowStatements) {
			this.type1 = type1;
			this.type2 = type2;
			this.allowStatements = allowStatements;
		}

		@Override
		public boolean valid(TokenType type, List<Token> tokens) {
			return !tokens.isEmpty() && tokens.get(0).type==type1;
		}

		@Override
		public Result<MatchResult> transform(TokenType type, List<Token> tokens) {
			ArrayList<Token> ts = new ArrayList<>();
			int depth = 1;
			for (int i=1;i<tokens.size();i++) {
				if (tokens.get(i).type==type1) {
					depth++;
				} else if (tokens.get(i).type==type2) {
					depth--;
				}
				
				if (depth==0) {
					Result<Token> tr = Parser.parse(ts);
					if (tr.failed) {
						return (Result) tr;
					} else if (!allowStatements && tr.item!=null && tr.item.type==TokenType.STATEMENT) {
						return new Result<MatchResult>(new SourceError(SourceError.ErrorType.ILLEGAL_PARENS,tr.item.range, "Improper parenthesis format"));
					} else {
						return new Result<>(new MatchResult(new Token(type, null, Range.from(tokens.get(0).range, tokens.get(i).range), tr.item, null), ts.size()+2));
					}
				} else {
					ts.add(tokens.get(i));
				}
			}
			return new Result<MatchResult>(new SourceError(SourceError.ErrorType.UNEXPECTED_EOF, tokens.get(0).range, "Unexpected EOF"));
		}
	}

	public boolean valid(TokenType type, List<Token> tokens);
	public Result<MatchResult> transform(TokenType type, List<Token> tokens);
}
