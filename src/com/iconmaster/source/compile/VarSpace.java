package com.iconmaster.source.compile;

import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.Variable;
import java.util.HashMap;

/**
 *
 * @author iconmaster
 */
public class VarSpace {
	HashMap<String,String> varsUsed = new HashMap<>();
	HashMap<String,Variable> fieldsUsed = new HashMap<>();
	HashMap<String,Function> funcsUsed = new HashMap<>();
	public VarSpace parent;
	
	HashMap<String,Boolean> consts = new HashMap<>();

	public VarSpace(VarSpace parent) {
		this.parent = parent;
	}

	public void putVar(String var) {
		varsUsed.put(var,var);
	}
	
	public String getVar(String name) {
		String var = varsUsed.get(name);
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
	
	public Boolean getConst(String name) {
		Boolean var = consts.get(name);
		if (var == null && parent != null) {
			return parent.getConst(name);
		}
		return var;
	}
	
	public void putConst(String name) {
		consts.put(name, false);
	}
	
	public Boolean putConstValue(String name) {
		Boolean var = consts.get(name);
		if (var==null) {
			return null;
		}
		consts.put(name, true);
		return var;
	}
}
