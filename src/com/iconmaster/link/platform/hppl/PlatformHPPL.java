package com.iconmaster.link.platform.hppl;

import com.iconmaster.source.compile.CompileUtils;
import com.iconmaster.source.link.Platform;
import com.iconmaster.source.link.platform.PlatformLoader.LoadedPlatform;
import com.iconmaster.source.prototype.SourcePackage;

/**
 *
 * @author iconmaster
 */
@LoadedPlatform
public class PlatformHPPL extends Platform {
	
	public PlatformHPPL() {
		this.name = "HPPL";
		
		//load all the default library packages:
		this.registerLibrary(new LibraryCore());
		
		//load code transformers:
		
		transforms.add(CompileUtils.iteratorReplacer); //if you want all non-system iterators inlined
		//transforms.add(CompileUtils.gotoReplacer); //if you want ALL SIL branches/loops replaced with SIL GOTOs
	}

	@Override
	public boolean canAssemble() {
		return true; //switch to true if your platform compiles (Example: LLVM).
	}

	@Override
	public boolean canRun() {
		return false; //switch to true if your platform interprets (Example: SourceBox).
	}

	@Override
	public Object assemble(SourcePackage pkg) {
		return null; //return an object that represents the compiler output.
	}

	@Override
	public Object run(SourcePackage pkg) {
		return null; //interpret SIL here.
	}
	
}
