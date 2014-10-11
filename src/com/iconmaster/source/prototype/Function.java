package com.iconmaster.source.prototype;

import com.iconmaster.source.compile.Operation;
import com.iconmaster.source.compile.VarSpace;
import com.iconmaster.source.element.Element;
import com.iconmaster.source.util.IDirectable;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class Function implements IDirectable {
	protected String name;
	protected ArrayList<Variable> args;
	protected ArrayList<DataType> returns;
	protected ArrayList<String> directives = new ArrayList<>();
	protected ArrayList<Element> rawCode;
	protected boolean library = false;
	
	protected boolean compiled = false;
	protected ArrayList<Operation> code;
	
	public VarSpace varspace = new VarSpace(null);

	public Function(String name, ArrayList<Variable> args, ArrayList<DataType> returns) {
		this.name = name;
		this.args = args;
		this.returns = returns;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(name+args+" as "+returns);
		if (code!=null) {
			sb.append(". CODE:");
			
			for (Operation op : code) {
				sb.append("\n\t");
				sb.append(op.toString());
			}
		}
		return sb.toString();
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

	public void setCompiled(ArrayList<Operation> code) {
		this.compiled = true;
		this.code = code;
	}
	
	public ArrayList<Element> rawData() {
		return rawCode;
	}

	public boolean isCompiled() {
		return compiled;
	}
	
	public String getName() {
		return name;
	}

	public ArrayList<Operation> getCode() {
		return code;
	}
	
	@Override
	public ArrayList<String> getDirectives() {
		return directives;
	}

	public ArrayList<Variable> getArguments() {
		return args;
	}

	public boolean isLibrary() {
		return library;
	}
}
