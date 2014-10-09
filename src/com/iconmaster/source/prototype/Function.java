package com.iconmaster.source.prototype;

import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class Function {
	String name;
	ArrayList<Variable> args;
	ArrayList<DataTypeDef> returns;
	ArrayList<String> directives;

	public Function(String name, ArrayList<Variable> args, ArrayList<DataTypeDef> returns) {
		this.name = name;
		this.args = args;
		this.returns = returns;
		this.directives = directives;
	}

	@Override
	public String toString() {
		return name+args+" as "+returns;
	}

	
}
