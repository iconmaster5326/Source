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
		ArrayList<Token> a = new ArrayList<>();
		
		//add basic tokens
		while (!input.isEmpty()) {
			//System.out.println(input);
			boolean found = false;
			int len = 0;
			for (TokenRule rule : TokenRule.values()) {
				Matcher m = Pattern.compile(rule.match).matcher(input);
				if (m.find()) {
					String got = m.group();
					int olen = len;
					len+=got.length();
					got = rule.format(got);
					input = m.replaceFirst("");
					if (got!=null) {
						a.add(new Token(new Range(olen,len),rule,got));
					}
					found = true;
					break;
				}
			}
			if (!found) {
				throw new SourceException(new Range(len,len),"Syntax error");
			}
		}
		//fix "one . sydrome" and turn them into symbols
		for (Token t : a) {
			if (t.type==TokenRule.NUMBER && t.string().equals(".")) {
				t.type=TokenRule.SYMBOL;
			}
		}
		//add compound/chain tokens
		ArrayList<Element> a2 = makeCompounds(a);
		//done tokenizing
		return a2;
	}
	
	public static int matchCompound(int i, ArrayList<Token> a,ArrayList<Element> a2, CompoundTokenRule rule) throws SourceException {
		int depth = 1;
		for (int j=i+1;j<a.size();j++) {
			Token t2 = a.get(j);
			if (t2.type==TokenRule.SYMBOL && t2.string().equals(rule.begin)) {
				depth++;
			} else if (t2.type==TokenRule.SYMBOL && t2.string().equals(rule.end)) {
				depth--;
			}

			if (depth==0) {
				ArrayList<Element> es = new ArrayList<>();
				for (int k=i+1;k<j;k++) {
					es.add(a.get(k));
				}
				es = makeCompounds(es);
				a2.add(new Token(Range.from(a.get(i).range, a.get(j).range),rule,es));
				return j;
			}
		}
		throw new SourceException(new Range(0,0),"Unexpected EOF");
	}

	public static ArrayList<Element> makeCompounds(ArrayList a) throws SourceException {
		ArrayList<Element> a2 = new ArrayList<>();
		for (int i=0;i<a.size();i++) {
			Object t = a.get(i);
			boolean matched = false;
			if (t instanceof Token) {
				for (CompoundTokenRule rule : CompoundTokenRule.values()) {
					if (((Token)t).type==TokenRule.SYMBOL && ((Token)t).string().equals(rule.begin)) {
						i = matchCompound(i,a,a2,rule);
						matched = true;
					}
				}
			}
			if (!matched) {
				a2.add((Element)t);
			}
		}
		return a2;
	}
}
