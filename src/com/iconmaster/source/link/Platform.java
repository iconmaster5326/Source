package com.iconmaster.source.link;

import com.iconmaster.source.prototype.Field;
import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.SourcePackage;
import java.util.HashMap;

/**
 *
 * @author iconmaster
 */
public abstract class Platform {
	public String name;
	public HashMap<String,SourcePackage> pkgs = new HashMap<>();
	
	protected void registerLibrary(SourcePackage pkg) {
		pkgs.put(pkg.getName(),pkg);
	}
	
	public abstract boolean canAssemble();
	public abstract boolean canRun();

	public abstract String assemble(SourcePackage pkg);
	public abstract void run(SourcePackage pkg);
	
	public String getCompileName(SourcePackage pkg, Function fn, String name) {
		return name;
	}

	public String getCompileName(SourcePackage pkg, Field fn, String name) {
		return name;
	}
}
