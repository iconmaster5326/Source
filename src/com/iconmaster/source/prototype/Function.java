package com.iconmaster.source.prototype;

import com.iconmaster.source.element.Element;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class Function {
	String name;
	ArrayList<Variable> args;
	ArrayList<DataType> returns;
	ArrayList<String> directives;
	ArrayList<Element> rawCode;
	boolean library = false;

	public Function(String name, ArrayList<Variable> args, ArrayList<DataType> returns) {
		this.name = name;
		this.args = args;
		this.returns = returns;
	}

	@Override
	public String toString() {
		return name+args+" as "+returns;
	}

	public static Function libraryFunc(String name, String[] args, String[] argTypes, String[] rets) {
		ArrayList<Variable> varList = new ArrayList<>();
		ArrayList<DataType> retList = new ArrayList<>();
		int i=0;
		for (String arg : args) {
			varList.add(new Variable(arg,new DataType(argTypes[i])));
			i++;
		}
		for (String ret : rets) {
			retList.add(new DataType(ret));
		}
		Function fn = new Function(name,varList,retList);
		fn.library = true;
		return fn;
	}
}
