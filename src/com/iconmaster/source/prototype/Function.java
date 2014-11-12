package com.iconmaster.source.prototype;

import com.iconmaster.source.compile.DataType;
import com.iconmaster.source.compile.Operation;
import com.iconmaster.source.element.Element;
import com.iconmaster.source.util.IDirectable;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class Function implements IDirectable {
	protected String name;
	protected ArrayList<Field> args;
	private Element rawReturns;
	protected ArrayList<String> directives = new ArrayList<>();
	protected ArrayList<Element> rawCode;
	protected boolean library = false;
	
	protected boolean compiled = false;
	protected ArrayList<Operation> code;
	private DataType returns;
	
	public OnCompile onCompile;
	public OnRun onRun;
	public String pkgName;
	public String compileName;
	
	public int order = 0;
	
	public ArrayList<Field> rawParams;

	public Function(String name, ArrayList<Field> args, Element returns) {
		this.name = name;
		this.args = args;
		this.rawReturns = returns;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getFullName()+args+" as "+returns);
		if (code!=null) {
			sb.append(". CODE:");
			
			for (Operation op : code) {
				sb.append("\n\t");
				sb.append(op.toString());
			}
		}
		return sb.toString();
	}
	
	public static Function libraryFunction(String name, String[] args, Object[] argTypes, Object ret) {
		ArrayList<Field> argList = new ArrayList<>();
		ArrayList<DataType> retList = new ArrayList<>();
		
		int i = 0;
		for (String arg : args) {
			Element e = null;
			Field f = new Field(arg, null);
			if (i<argTypes.length) {
				DataType dt = null;
				if (argTypes[i] instanceof TypeDef) {
					dt = new DataType((TypeDef)argTypes[i],false);
				} else if (argTypes[i] instanceof DataType) {
					dt = (DataType) argTypes[i];
				}
				f.setType(dt);
			}
			argList.add(f);
			i++;
		}
		

		Function fn = new Function(name, argList, null);
		DataType dt = null;
		if (ret instanceof TypeDef) {
			dt = new DataType((TypeDef)ret,false);
		} else if (ret instanceof DataType) {
			dt = (DataType) ret;
		}
		fn.setReturnType(dt);
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
	
	public String getFullName() {
		return ((pkgName==null || pkgName.isEmpty())?"":(pkgName+"."))+name+(order==0?"":("%"+order));
	}

	public ArrayList<Operation> getCode() {
		return code;
	}
	
	@Override
	public ArrayList<String> getDirectives() {
		return directives;
	}

	public ArrayList<Field> getArguments() {
		return args;
	}

	public boolean isLibrary() {
		return library;
	}
	
	public String compileFunction(SourcePackage pkg, Object... args) {
		if (this.onCompile==null) {
			return null;
		} else {
			return this.onCompile.compile(pkg,args);
		}
	}


	public Element getReturn() {
		return rawReturns;
	}

	public DataType getReturnType() {
		return returns;
	}

	public void setReturnType(DataType returns) {
		this.returns = returns;
	}

	public static interface OnCompile {
		public String compile(SourcePackage pkg, Object... args);
	}
	
	public static interface OnRun {
		public Object run(SourcePackage pkg, Object... args);
	}
}
