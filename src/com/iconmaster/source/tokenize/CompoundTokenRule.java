package com.iconmaster.source.tokenize;

import com.iconmaster.source.element.IElementType;

/**
 *
 * @author iconmaster
 */
public enum CompoundTokenRule implements IElementType {
	PAREN("p","(",")"),
	INDEX("i","[","]"),
	BLOCK("c","{","}");
	
	public String begin;
	public String end;
	public String alias;
	
	CompoundTokenRule(String alias,String begin, String end) {
		this.begin = begin;
		this.end = end;
		this.alias = alias;
	}
	
	@Override
	public String getAlias() {
		return alias;
	}
}
