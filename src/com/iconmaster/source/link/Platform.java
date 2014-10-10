package com.iconmaster.source.link;

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

	public abstract String assemble(SourcePackage pkg);
}
