package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.prototype.SourcePackage;
import com.iconmaster.source.util.Directives;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class AssemblyData {
	public SourcePackage pkg;
	public boolean minify;
	
	public ArrayList<HPPLFunction> funcs = new ArrayList<>();
	public ArrayList<HPPLField> fields = new ArrayList<>();
	public ArrayList<HPPLVariable> vars = new ArrayList<>();
	
	public AssemblyData(SourcePackage pkg) {
		this.pkg = pkg;
		
		minify = !Directives.has(pkg, "!minify");
	}
}
