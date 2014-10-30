package com.iconmaster.source.compile;

import com.iconmaster.source.element.Element;
import com.iconmaster.source.prototype.SourcePackage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author iconmaster
 */
public class ScopeFrame {
	
	public static class Variable {
		public String name;
		public String realName;
	}
	
	public SourcePackage pkg;
	public ScopeFrame parent;
	public HashMap<String,String> names = new HashMap<>();
	public HashMap<String,Variable> vars = new HashMap<>();
	public HashSet<String> defined = new HashSet<>();
	public HashMap<String,DataType> types = new HashMap<>();
	
	public HashSet<String> inline = new HashSet<>();
	public HashMap<String,Element> inlineCode = new HashMap<>();

	public ScopeFrame(SourcePackage pkg, ScopeFrame parent) {
		this.pkg = pkg;
		this.parent = parent;
	}

	public ScopeFrame(SourcePackage pkg) {
		this(pkg,null);
	}
	
	public Variable putVariable(String name, boolean map) {
		Variable v = new Variable();
		v.realName = name;
		if (map) {
			v.name = pkg.nameProvider.getTempName();
			names.put(v.realName, v.name);
		} else {
			v.name = v.realName;
		}
		putDefined(name);
		vars.put(name, v);
		return v;
	}
	
	public Variable getVariable(String realName) {
		Variable v = vars.getOrDefault(realName, vars.get(names.get(realName)));
		if (v==null && parent!=null) {
			return parent.getVariable(realName);
		}
		return v;
	}
	
	public String getVariableName(String name) {
		return getVariable(name).name;
	}
	
	public Variable newVariable() {
		return putVariable(pkg.nameProvider.getTempName(), false);
	}
	
	public String newVarName() {
		return newVariable().name;
	}
	
	public String[] getAllVars() {
		ArrayList<String> a = new ArrayList<>();
		for (String var : defined) {
			a.add(var);
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
}
