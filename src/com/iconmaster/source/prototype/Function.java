package com.iconmaster.source.prototype;

import com.iconmaster.source.element.Element;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class Function {
	protected String name;
	protected ArrayList<Variable> args;
	protected ArrayList<DataType> returns;
	protected ArrayList<String> directives;
	protected ArrayList<Element> rawCode;
	protected boolean library = false;
	protected boolean compiled = false;

	public Function(String name, ArrayList<Variable> args, ArrayList<DataType> returns) {
		this.name = name;
		this.args = args;
		this.returns = returns;
	}

	@Override
	public String toString() {
		return name+args+" as "+returns;
	}
	
	public static Function libraryFunction(String name, String[] args, String[] argTypes, String[] rets) {
		ArrayList<Variable> argList = new ArrayList<>();
		ArrayList<DataType> retList = new ArrayList<>();
		
		int i = 0;
		for (String arg : args) {
			argList.add(new Variable(arg, new DataType(i>=argTypes.length?"?":argTypes[i])));
			i++;
		}
		
		for (String ret : rets) {
			retList.add(new DataType(ret));
		}
				
		Function fn = new Function(name, argList, retList);
		fn.library = true;
		fn.compiled = true;
		return fn;
	}
}
