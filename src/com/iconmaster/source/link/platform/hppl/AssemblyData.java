package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.prototype.SourcePackage;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class AssemblyData {
	public SourcePackage pkg;
	
	public ArrayList<HPPLFunction> funcs = new ArrayList<>();
	public ArrayList<HPPLField> fields = new ArrayList<>();
	public ArrayList<HPPLVariable> vars = new ArrayList<>();
	
	public AssemblyData(SourcePackage pkg) {
		this.pkg = pkg;
	}
}
