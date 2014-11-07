package com.iconmaster.source.link.platform.hppl;

import com.iconmaster.source.link.Platform;
import com.iconmaster.source.prototype.Field;
import com.iconmaster.source.prototype.Function;
import com.iconmaster.source.prototype.SourcePackage;
import com.iconmaster.source.util.Directives;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
public class PlatformHPPL extends Platform {

	public PlatformHPPL() {
		this.name = "HPPL";
		
		this.registerLibrary(new LibraryCore());
		this.registerLibrary(new LibraryMath());
		
		this.registerLibrary(new LibraryPrimeDraw());
	}
	
	@Override
	public String getCompileName(SourcePackage pkg, Function fn, String name) {
		if (Directives.has(fn, "native")) {
			ArrayList<String> a = Directives.getValues(fn, "native");
			if (a.isEmpty()) {
				return name;
			} else {
				return a.get(0);
			}
		}
		name = name.replace(".", "_").replace("?", "_");
		if (name.startsWith("_")) {
			name = "a"+name;
		}
		if (fn.order!=0) {
			name+="_"+fn.order;
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
