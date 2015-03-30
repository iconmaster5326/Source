package com.iconmaster.source.parse;

import com.iconmaster.source.exception.SourceError;
import com.iconmaster.source.util.Result;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains methods for parsing lists of Source tokens.
 *
 * @author iconmaster
 */
public class Parser {

	/**
	 * Converts a list of tokens to an abstract syntax tree.
	 *
	 * @param tokens A list of tokens, preferably produced by
	 * Tokenizer.tokenize().
	 * @return The binary AST representing a Source program.
	 *
	 * @see Tokenizer
	 * @see Token
	 */
	public static Result<Token> parse(List<Token> tokens) {
		tokens = new ArrayList<>(tokens);
		for (TokenType type : TokenType.values()) {
			if (!type.simple) {
				for (int pos=0;pos<tokens.size();pos++) {
					List<Token> tl = tokens.subList(pos, tokens.size());
					if (type.matcher.valid(type, tl)) {
						Result<ParseMatcher.MatchResult> res = type.matcher.transform(type, tl);
						if (res!=null) {
							if (res.failed) {
								return (Result) res;
							} else {
								for (int i=0;i<res.item.replace;i++) {
									tokens.remove(pos);
								}
								if (res.item.t!=null) {
									tokens.add(pos,res.item.t);
								}
							}
							pos = -1;
						}
					}
				}
			}
		}
		
		if (tokens.isEmpty()) {
			return new Result<>((Token)null);
		}
		if (tokens.size()>1) { //we should never be here. We will be at some point, hough; I just know it.
			Token t = tokens.get(0);
			return new Result<Token>(new SourceError(SourceError.ErrorType.GENERAL, t.range, "Unknown token pattern: "+tokens));
		}
		return new Result<>(tokens.get(0));
	}
}
