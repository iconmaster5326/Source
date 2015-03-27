package com.iconmaster.source.parse;

import com.iconmaster.source.util.Range;
import com.iconmaster.source.util.Result;
import java.util.List;

/**
 * The list of valid Source tokens. Contains both the tokens used in Tokenizer
 * and the AST elements used in Parser.
 *
 * @author iconmaster
 */
public enum TokenType {

	COMMENT("\\/\\/[^\n]*\n?"),
	SPACE("[\\s;]+"),
	STRING("\"(\\\\.|[^\"])*\""),
	NUMBER("[\\d\\.]+(?:[eE][\\+\\-]?\\d+)?"),
	WORD("[\\w_][\\w\\d_]*"),
	CHAR("\'(\\\\.|[^\'])\'"),
	DIRECTIVE("@[\\S]*"),
	COMMA(","),
	LPAREN("\\("),
	RPAREN("\\)"),
	LBRACE("\\{"),
	RBRACE("\\}"),
	LBRACKET("\\["),
	RBRACKET("\\]"),
	DOT("\\."),
	SYMBOL("[\\Q+-*/=<>~:!&|%$^\\E]+"),
	
	PAREN(new ParseMatcher.BlockMatcher(LPAREN, RPAREN, false)),
	INDEX(new ParseMatcher.BlockMatcher(LBRACKET, RBRACKET, false)),
	CODE(new ParseMatcher.BlockMatcher(LBRACE, RBRACE, true)),
	LINK(new ParseMatcher.BinOpMatcher(TokenType.DOT, ".")),
	POW(new ParseMatcher.BinOpMatcher("^")),
	NEG(new ParseMatcher.UnaryOpMatcher("-")),
	MUL(new ParseMatcher.BinOpMatcher("*")),
	DIV(new ParseMatcher.BinOpMatcher("/")),
	MOD(new ParseMatcher.BinOpMatcher("%")),
	ADD(new ParseMatcher.BinOpMatcher("+")),
	SUB(new ParseMatcher() {

		@Override
		public boolean valid(TokenType type, List<Token> tokens) {
			return tokens.size()>=2 && tokens.get(1).type==TokenType.NEG;
		}

		@Override
		public Result<MatchResult> transform(TokenType type, List<Token> tokens) {
			return new Result<>(new MatchResult(new Token(type, null, Range.from(tokens.get(0).range, tokens.get(1).range), tokens.get(0), tokens.get(1).l), 2));
		}
	}),
	SLL(new ParseMatcher.BinOpMatcher("<<")),
	SRL(new ParseMatcher.BinOpMatcher(">>")),
	SRA(new ParseMatcher.BinOpMatcher(">>>")),
	NOT(new ParseMatcher.UnaryOpMatcher(TokenType.WORD, "not")),
	BIT_NOT(new ParseMatcher.UnaryOpMatcher("!")),
	LT(new ParseMatcher.BinOpMatcher("<")),
	LE(new ParseMatcher.BinOpMatcher("<=")),
	GT(new ParseMatcher.BinOpMatcher(">")),
	GE(new ParseMatcher.BinOpMatcher(">=")),
	EQ(new ParseMatcher.BinOpMatcher("==")),
	NEQ(new ParseMatcher.BinOpMatcher("==")),
	BIT_AND(new ParseMatcher.BinOpMatcher("&")),
	BIT_OR(new ParseMatcher.BinOpMatcher("|")),
	AND(new ParseMatcher.BinOpMatcher(TokenType.WORD, "and")),
	OR(new ParseMatcher.BinOpMatcher(TokenType.WORD, "or")),
	TUPLE(new ParseMatcher.BinOpMatcher(TokenType.COMMA, ",")),
	ASSIGN(new ParseMatcher.BinOpMatcher("=")),
	LOCAL(new ParseMatcher() {

		@Override
		public boolean valid(TokenType type, List<Token> tokens) {
			return tokens.size()>=2 && tokens.get(0).type==TokenType.WORD && "local".equals(tokens.get(0).data);
		}

		@Override
		public Result<ParseMatcher.MatchResult> transform(TokenType type, List<Token> tokens) {
			return new Result<>(new ParseMatcher.MatchResult(new Token(type, null, Range.from(tokens.get(0).range, tokens.get(1).range), tokens.get(1), null), 2));
		}
	}),
	STATEMENT(new ParseMatcher() {

		@Override
		public boolean valid(TokenType type, List<Token> tokens) {
			return tokens.size()>=2;
		}

		@Override
		public Result<ParseMatcher.MatchResult> transform(TokenType type, List<Token> tokens) {
			return new Result<>(new ParseMatcher.MatchResult(new Token(type, null, Range.from(tokens.get(0).range, tokens.get(1).range), tokens.get(0), tokens.get(1)), 2));
		}
	});

	public boolean simple;
	public String matches;
	public ParseMatcher matcher;

	private TokenType(String matches) {
		simple = true;
		this.matches = matches;
	}

	private TokenType(ParseMatcher matcher) {
		simple = false;
		this.matcher = matcher;
	}

	/**
	 * Returns, based on what was matched, the string to store in the data field
	 * of a token. Used only in simple tokens.
	 *
	 * @param data
	 * @return
	 */
	public String getData(String data) {
		switch (this) {
			case SPACE:
			case COMMENT:
				return null;
			case STRING:
				return data.substring(1, data.length() - 1);
			case DIRECTIVE:
				return data.substring(1);
			default:
				return data;
		}
	}
}
