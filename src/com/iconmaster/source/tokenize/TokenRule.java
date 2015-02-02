package com.iconmaster.source.tokenize;

import com.iconmaster.source.element.IElementType;
import com.iconmaster.source.util.StringUtils;

/**
 *
 * @author iconmaster
 */
public enum TokenRule implements IElementType {
	COMMENT(null,"\\/\\/[^\n]*\n?"),
	SPACE(" ","[\\s]+"),
	RESWORD(null,"(local|function|and|or|not|for|in|as|return|break|struct|if|else|elseif|while|repeat|until|field|import|package|enum|true|false|iterator|new|extends|to|continue|type|class)\\b"),
	WORD("w","[\\w\\?&&[^\\d]][\\w\\?\\.]*"),
	NUMBER("n","[\\d\\.]+"),
	STRING("s","\"(\\\\.|[^\"])*\""),
	CHAR(null,"\'(\\\\.|[^\'])\'"),
	SEP(";",";+"),
	DIRECTIVE("r","@[\\S]*"),
	SYMBOL("y","([\\Q+-*/=<>~!&|%^\\E]+|\\(|\\)|\\[|\\]|\\{|\\}|,)");
	
	public final String match;
	public String alias;
	
	TokenRule(String alias, String match) {
		this.match = "^"+match;
		this.alias = alias;
	}
	
	TokenRule(String match) {
		this(null,match);
	}
	
	TokenRule() {
		this(null,"[^.]");
	}
	
	public String format(String input) {
		switch (this) {
			case SPACE:
			case COMMENT:
				return null;
			case CHAR:
			case STRING:
				return StringUtils.unescape(input.substring(1, input.length()-1));
			case DIRECTIVE:
				return input.substring(1);
			default:
				return input;
		}
	}
	
	@Override
	public String getAlias() {
		return alias;
	}
}
