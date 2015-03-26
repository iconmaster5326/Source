package com.iconmaster.source.parse;

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
	
	MUL(new ParseMatcher.BinOpMatcher("*")),
	DIV(new ParseMatcher.BinOpMatcher("/")),
	ADD(new ParseMatcher.BinOpMatcher("+")),
	SUB(new ParseMatcher.BinOpMatcher("-"));

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
