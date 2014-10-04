package com.iconmaster.source.parse;

import com.iconmaster.source.element.Element;
import com.iconmaster.source.element.IElementType;
import com.iconmaster.source.element.ISpecialRule;
import com.iconmaster.source.element.Rule;
import com.iconmaster.source.exception.SourceException;
import com.iconmaster.source.tokenize.CompoundTokenRule;
import com.iconmaster.source.tokenize.TokenRule;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author iconmaster
 */
public class Parser {
	private static final HashMap<String,IElementType> aliases = new HashMap<>();
	
	public static void addAlias(IElementType type) {
		aliases.put(type.getAlias(),type);
	}
	
	static {
		for (IElementType rule : Rule.values()) {
			addAlias(rule);
		}
		for (IElementType rule : TokenRule.values()) {
			addAlias(rule);
		}
		for (IElementType rule : CompoundTokenRule.values()) {
			addAlias(rule);
		}
	}
	
	public static ArrayList<Element> parse(ArrayList<Element> a) throws SourceException {
		System.out.println("Initial tree: "+a);
		for (Rule rule : Rule.values()) {
			for (int i=0;i<a.size();i++) {
				ISpecialRule.RuleResult m = rule.rule.match(a, i);
				if (m!=null) {
					for (int j=0;j<m.del;j++) {
						a.remove(i);
					}
					if (m.ret!=null) {
						a.add(i, m.ret);
					}
					System.out.print("matched rule "+rule.toString()+": ");
					System.out.println(a);
					i=-1;
				}
			}
		}
		//Make sure there is no invalid tokens left lying around
		check(a);
		return a;
	}

	public static IElementType getAlias(String toMatch) {
		return aliases.get(toMatch);
	}
	
	public static void check(ArrayList<Element> a) throws SourceException {
		for (Element v : a) {
			if (v.type instanceof TokenRule) {
				
			}
		}
	}
}
