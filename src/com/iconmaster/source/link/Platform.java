package com.iconmaster.source.link;

import com.iconmaster.source.prototype.SourcePackage;
import java.util.HashMap;

/**
 *
 * @author iconmaster
 */
public class Platform {
	public String name;
	public HashMap<String,SourcePackage> pkgs;
	
	protected void registerLibrary(SourcePackage pkg) {
		pkgs.put(pkg.getName(),pkg);
	}
}
