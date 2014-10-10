package com.iconmaster.source.link;

import com.iconmaster.source.exception.SourceException;
import com.iconmaster.source.link.platform.hppl.PlatformHPPL;
import com.iconmaster.source.prototype.SourcePackage;
import com.iconmaster.source.util.Range;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author iconmaster
 */
public class Linker {
	public static HashMap<String,Platform> platforms = new HashMap<>();
	
	static {
		registerPlatform(new PlatformHPPL());
	}
	
	public static void registerPlatform(Platform p) {
		platforms.put(p.name,p);
	}
	
	public HashMap<String,SourcePackage> imported = new HashMap<>();
	public Platform platform;
	public SourcePackage pkg = new SourcePackage();
	
	public Linker(String platform) {
		this.platform = platforms.get(platform);
	}
	
	public void linkUserPackage(SourcePackage user) {
		pkg.addContents(user);
		imported.put(user.getName(), user);
	}
	
	public void linkLibrary(SourcePackage lib) {
		pkg.addContents(lib);
		imported.put(lib.getName(), lib);
	}
	
	public SourcePackage getImportRef(String pkgName) {
		if (platform.pkgs.containsKey(pkgName)) {
			return platform.pkgs.get(pkgName);
		}
		return null;
	}
	
	public void addImportRef(String pkgName) throws SourceException {
		SourcePackage newPkg = getImportRef(pkgName);
		if (newPkg!=null) {
			linkLibrary(newPkg);
			manageLinks();
		} else {
			throw new SourceException(new Range(0,1),"Cannot resolve import "+pkgName);
		}
	}
	
	public ArrayList<SourceException> manageLinks() {
		ArrayList<SourceException> a = new ArrayList<>();
		for (String imp : pkg.getImports()) {
			if (!imported.containsKey(imp)) {
				try {
					addImportRef(imp);
				} catch (SourceException ex) {
					a.add(ex);
				}
			}
		}
		return a;
	}
}
