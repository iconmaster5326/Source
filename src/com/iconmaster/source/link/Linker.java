package com.iconmaster.source.link;

import com.iconmaster.source.link.platform.hppl.PlatformHPPL;
import com.iconmaster.source.prototype.SourcePackage;
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
	public ArrayList<String> unresolvedImports = new ArrayList<>();
	
	public Linker(String platform) {
		this.platform = platforms.get(platform);
		linkLibrary(this.platform.pkgs.get("core"));
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
	
	public boolean addImportRef(String pkgName) {
		SourcePackage newPkg = getImportRef(pkgName);
		if (newPkg!=null) {
			linkLibrary(newPkg);
			manageLinks();
		} else {
			return false;
		}
		return true;
	}
	
	public void manageLinks() {
		unresolvedImports.clear();
		for (String imp : pkg.getImports()) {
			if (!imported.containsKey(imp)) {
				boolean found = addImportRef(imp);
				if (!found) {
					unresolvedImports.add(imp);
				} else {
					break;
				}
			}
		}
	}
	
	public static Linker link(String plat,SourcePackage pkg) {
		Linker linker = new Linker(plat);
		linker.linkUserPackage(pkg);
		linker.manageLinks();
		return linker;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Linker (platform ");
		sb.append(platform.name).append("):\n\t");
		sb.append(pkg.toString().replace("\n", "\n\t"));
		return sb.toString();
	}
}
