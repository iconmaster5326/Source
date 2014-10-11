package com.iconmaster.source.compile;

import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.Variable;
import java.util.HashMap;

/**
 *
 * @author iconmaster
 */
public class VarSpace {
	HashMap<String,Variable> varsUsed = new HashMap<>();
	HashMap<String,Variable> fieldsUsed = new HashMap<>();
	HashMap<String,Function> funcsUsed = new HashMap<>();
	public VarSpace parent;

	public VarSpace(VarSpace parent) {
		this.parent = parent;
	}

	public void putVar(Variable var) {
		varsUsed.put(var.getName(),var);
	}
	
	public Variable getVar(String name) {
		Variable var = varsUsed.get(name);
		if (var == null && parent != null) {
			return parent.getVar(name);
		}
		return var;
	}
	
	public void putField(Variable var) {
		fieldsUsed.put(var.getName(),var);
	}
	
	public Variable getField(String name) {
		Variable var = fieldsUsed.get(name);
		if (var == null && parent != null) {
			return parent.getField(name);
		}
		return var;
	}
	
	public void putFunc(Function var) {
		funcsUsed.put(var.getName(),var);
	}
	
	public Function getFunc(String name) {
		Function var = funcsUsed.get(name);
		if (var == null && parent != null) {
			return parent.getFunc(name);
		}
		return var;
	}
}
