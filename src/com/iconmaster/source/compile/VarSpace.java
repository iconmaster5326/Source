package com.iconmaster.source.compile;

import com.iconmaster.source.prototype.Variable;
import java.util.HashMap;

/**
 *
 * @author iconmaster
 */
public class VarSpace {
	
	HashMap<String,Variable> vars = new HashMap<>();
	public VarSpace parent;

	public VarSpace(VarSpace parent) {
		this.parent = parent;
	}

	public void put(Variable var) {
		vars.put(var.getName(),var);
	}
	
	public Variable get(String name) {
		Variable var = vars.get(name);
		if (var == null && parent != null) {
			return parent.get(name);
		}
		return var;
	}
}
