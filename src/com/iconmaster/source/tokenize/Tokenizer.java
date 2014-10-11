package com.iconmaster.source.tokenize;

import com.iconmaster.source.element.Element;
import com.iconmaster.source.exception.SourceException;
import com.iconmaster.source.util.Range;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author iconmaster
 */
public class Tokenizer {
	public static ArrayList<Element> tokenize(String input) throws SourceException {
		return tokenize(input, true);
	}
	
	public static ArrayList<Element> tokenize(String input, boolean expand) throws SourceException {
		ArrayList<Token> a = new ArrayList<>();
		
		//add basic tokens
		int len = 0;
		while (!input.isEmpty()) {
			//System.out.println(input);
			boolean found = false;
			for (TokenRule rule : TokenRule.values()) {
				Matcher m = Pattern.compile(rule.match).matcher(input);
				if (m.find()) {
					String got = m.group();
					int olen = len;
					len+=got.length();
					if (expand) {
						got = rule.format(got);
					}
					input = m.replaceFirst("");
					if (got!=null) {
						a.add(new Token(new Range(olen,len),rule,got));
					}
					found = true;
					break;
				}
			}
			if (!found) {
				throw new SourceException(new Range(len,len+1),"Unknown symbol");
			}
		}
		//fix "one . sydrome" and turn them into symbols
		for (Token t : a) {
			if (t.type==TokenRule.NUMBER && t.string().equals(".")) {
				t.type=TokenRule.SYMBOL;
			}
		}
		//add compound/chain tokens
		ArrayList<Element> a2;
		a2 = new ArrayList<>();
		a2.addAll(a);
		//done tokenizing
		return a2;
	}
}
