package com.iconmaster.source.link.platform.test;

import com.iconmaster.source.link.Platform;
import com.iconmaster.source.link.platform.PlatformLoader.LoadedPlatform;
import com.iconmaster.source.prototype.SourcePackage;

/**
 *
 * @author iconmaster
 */
@LoadedPlatform
public class PlatformTest extends Platform {
	
	public PlatformTest() {
		this.name = "Test";
		
		this.registerLibrary(new LibraryCore());
		
		//transforms.add(CompileUtils.iteratorReplacer); //if you want all iterators replaced with more generic for loops
		//transforms.add(CompileUtils.forEachReplacer); //if you want all SIL for-eaches replaced with for-ranges
		//transforms.add(CompileUtils.gotoReplacer); //if you want ALL SIL branches/loops replaced with SIL GOTOs
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
	public String assemble(SourcePackage pkg) {
		return "done";
	}

	@Override
	public void run(SourcePackage pkg) {
		
	}
	
}
