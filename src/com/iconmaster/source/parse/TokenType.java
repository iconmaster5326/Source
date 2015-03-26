package com.iconmaster.source.parse;

/**
 * The list of valid Source tokens. Contains both the tokens used in Tokenizer
 * and the AST elements used in Parser.
 *
 * @author iconmaster
 */
public enum TokenType {

	WORD("\\w+");

	public boolean simple;
	public String matches;

	private TokenType( String matches) {
		simple = true;
		this.matches = matches;
		
		Token.simples.add(this);
	}

	/**
	 * Returns, based on what was matched, the string to store in the data field
	 * of a token. Used only in simple tokens.
	 *
	 * @param data
	 * @return
	 */
	public String getData(String data) {
		return data;
	}
}
