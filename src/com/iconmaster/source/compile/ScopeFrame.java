package com.iconmaster.source.compile;

import com.iconmaster.source.element.Element;
import com.iconmaster.source.prototype.SourcePackage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author iconmaster
 */
public class ScopeFrame {
	public SourcePackage pkg;
	public ScopeFrame parent;
	public HashSet<String> vars = new HashSet<>();
	public HashSet<String> defined = new HashSet<>();
	public HashMap<String,DataType> types = new HashMap<>();
	public HashMap<String,DataType> params = new HashMap<>();
	
	public HashSet<String> inline = new HashSet<>();
	public HashMap<String,Element> inlineCode = new HashMap<>();

	public ScopeFrame(SourcePackage pkg, ScopeFrame parent) {
		this.pkg = pkg;
		this.parent = parent;
	}

	public ScopeFrame(SourcePackage pkg) {
		this(pkg,null);
	}
	
	public ScopeFrame(CompileData cd) {
		this.pkg = cd.pkg;
		this.parent = cd.frame;
	}
	
	public void putVariable(String name) {
		putDefined(name);
		vars.add(name);
	}
	
	public boolean getVariable(String name) {
		boolean b = vars.contains(name);
		if (b) {
			return true;
		} else if (parent==null) {
			return false;
		} else {
			return parent.getVariable(name);
		}
	}
	
	public String newVarName() {
		String name = pkg.nameProvider.getTempName();
		putVariable(name);
		return name;
	}
	
	public String[] getAllVars() {
		ArrayList<String> a = new ArrayList<>();
		for (String var : defined) {
			a.add(var);
		}
		for (String var : inline) {
			a.add(var);
		}
		if (parent!=null) {
			a.addAll(Arrays.asList(parent.getAllVars()));
		}
		return a.toArray(new String[0]);
	}
	
	public String[] getTypeStrings() {
		ArrayList<String> a = new ArrayList<>();
		for (String var : defined) {
			a.add(types.getOrDefault(var,new DataType(true)).toString());
		}
		return a.toArray(new String[0]);
	}
	
	public void putDefined(String name) {
		defined.add(name);
	}
	
	public boolean isDefined(String name) {
		return defined.contains(name)?true:(parent==null?false:parent.isDefined(name));
	}
	
	public void putInline(String name) {
		inline.add(name);
	}
	
	public void putInline(String name, Element expr) {
		putInline(name);
		inlineCode.put(name, expr);
	}
	
	public boolean isInlined(String name) {
		return inline.contains(name)?true:(parent==null?false:parent.isInlined(name));
	}
	
	public Element getInline(String name) {
		if (inlineCode.containsKey(name)) {
			return inlineCode.get(name);
		}
		if (parent==null) {
			return null;
		}
		return parent.getInline(name);
	}
	
	public void setVarType(String name, DataType type) {
		types.put(name, type);
	}
	
	public DataType getVarType(String name) {
		return types.containsKey(name)?types.get(name):(parent==null?null:parent.getVarType(name));
	}
	
	public void setParam(String name, DataType type) {
		params.put(name, type==null?new DataType():type);
	}
	
	public DataType getParam(String name) {
		return params.containsKey(name)?params.get(name):(parent==null?null:parent.getVarType(name));
	}
}
