package com.iconmaster.source.prototype;

import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class Variable {
	String name;
	DataTypeDef type;
	ArrayList<String> directives;

	public Variable(String name) {
		this(name,null);
	}

	public Variable(String name, DataTypeDef type) {
		this.name = name;
		this.type = type;
	}

	@Override
	public String toString() {
		return name+"="+type;
	}
	
	
}
