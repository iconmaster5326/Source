package com.iconmaster.source.parse;

import com.iconmaster.source.exception.SourceError;
import com.iconmaster.source.util.Range;
import com.iconmaster.source.util.Result;
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
	public static Result<List<Token>> tokenize(String input) {
		ArrayList<Token> a = new ArrayList<>();
		int i = 0;
		while (!input.isEmpty()) {
			boolean found = false;
			
			for (TokenType type : TokenType.values()) {
				if (!type.simple) break;
				
				Pattern p = Pattern.compile("^"+type.matches);
				Matcher m = p.matcher(input);
				if (m.find()) {
					found = true;
					String got = m.group();
					if (got.equals(".")) { //silly hack for NUMBER -> DOT
						type = TokenType.DOT;
					}
					Range rn = new Range(i,i+got.length());
					i += got.length();
					input = input.substring(got.length());
					got = type.getData(got);
					if (got!=null) {
						Token t = new Token(type, got, rn);
						a.add(t);
					}
				}
			}
			
			if (!found) {
				return new Result<List<Token>>(new SourceError(SourceError.ErrorType.UNKNOWN_SYMBOL, new Range(i,i+1), "Unknown symbol '"+input.charAt(0)+"'"));
			}
		}
		return new Result<>(a);
	}
}
