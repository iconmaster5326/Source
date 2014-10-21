package com.iconmaster.source.tokenize;

import com.iconmaster.source.element.IElementType;

/**
 *
 * @author iconmaster
 */
public enum TokenRule implements IElementType {
	COMMENT(null,"\\/\\/[^\n]*\n"),
	SPACE(" ","[\\s]+"),
	RESWORD(null,"(local|function|and|or|not|for|in|as|return|break|struct|if|else|elseif|while|repeat|until|field|import|package|enum|true|false|iterator|this|extends)\\b"),
	WORD("w","[\\w\\?&&[^\\d]][\\w\\?\\.]*"),
	NUMBER("n","[\\d\\.]+"),
	STRING("s","\"[^\"]*\""),
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
			case STRING:
				return input.substring(1, input.length()-1).replace("\n", " ");
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
