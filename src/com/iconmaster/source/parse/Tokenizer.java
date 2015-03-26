package com.iconmaster.source.parse;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains the methods used to turn an input string into a list of
 * Source tokens.
 *
 * @see Token
 * @see Parser
 *
 * @author iconmaster
 */
public class Tokenizer {

	/**
	 * Converts a Source input string into a list of tokens. All tokens have no
	 * child nodes, and are of leaf token types.
	 *
	 * @param input The string input
	 * @return A flat list of Tokens.
	 */
	public static List<Token> tokenize(String input) {
		ArrayList<Token> a = new ArrayList<>();
		while (!input.isEmpty()) {
			boolean found = false;
			
			for (TokenType type : Token.simples) {
				Pattern p = Pattern.compile("^"+type.matches);
				Matcher m = p.matcher(input);
				if (m.find()) {
					found = true;
				}
			}
			
			if (!found) {
				//error
			}
		}
		return a;
	}
}
