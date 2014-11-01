package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.link.Platform;
import com.iconmaster.source.prototype.Field;
import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.SourcePackage;

/**
 *
 * @author iconmaster
 */
public class PlatformHPPL extends Platform {

	public PlatformHPPL() {
		this.name = "HPPL";
		
		this.registerLibrary(new LibraryCore());
		this.registerLibrary(new LibraryMath());
	}
	
	@Override
	public String getCompileName(SourcePackage pkg, Function fn, String name) {
		name = name.replace(".", "_").replace("?", "_");
		if (name.startsWith("_")) {
			name = "a"+name;
		}
		return name;
	}
	
	@Override
	public String getCompileName(SourcePackage pkg, Field fn, String name) {
		name = name.replace(".", "_").replace("?", "_");
		if (name.startsWith("_")) {
			name = "a"+name;
		}
		return name;
	}

	@Override
	public String assemble(SourcePackage pkg) {
		return HPPLAssembler.assemble(pkg);
	}

	@Override
	public boolean canAssemble() {
		return true;
	}

	@Override
	public boolean canRun() {
		return false;
	}

	@Override
	public void run(SourcePackage pkg) {
		
	}
}
