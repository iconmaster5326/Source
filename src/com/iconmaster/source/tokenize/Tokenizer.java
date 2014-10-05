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
		if (expand) {
			a2 = makeCompounds(a);
		} else {
			a2 = new ArrayList<>();
			a2.addAll(a);
		}
		//done tokenizing
		return a2;
	}
	
	public static int matchCompound(int i, ArrayList<Token> a,ArrayList<Element> a2, CompoundTokenRule rule) throws SourceException {
		int depth = 1;
		Range initRange = a.get(i).range;
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
				a2.add(new Token(Range.from(initRange, a.get(j).range),rule,es));
				return j;
			}
		}
		throw new SourceException(initRange,"Unexpected EOF");
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
