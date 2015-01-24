package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.prototype.SourcePackage;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author iconmaster
 */
public class AssemblyData {
	public HashMap<String,String> varNames = new HashMap<>();
	public ArrayList<String> vars = new ArrayList<>();
	
	public SourcePackage pkg;
	
	public AssemblyData(SourcePackage pkg) {
		this.pkg = pkg;
	}
	
	public String newVar(String var) {
		String newName = HPPLNaming.getNewName();
		vars.add(newName);
		varNames.put(var, newName);
		return newName;
	}
	
	public String directMap(String var) {
		vars.add(var);
		varNames.put(var, var);
		return var;
	}
	
	public boolean isVar(String var) {
		return vars.contains(var);
	}
	
	public boolean isMapped(String var) {
		return varNames.containsKey(var);
	}
	
	public String getMapping(String var) {
		return varNames.get(var);
	}
}
