package com.iconmaster.source.link.platform.test;

import com.iconmaster.source.compile.CompileUtils;
import com.iconmaster.source.compile.Operation;
import com.iconmaster.source.link.Platform;
import com.iconmaster.source.link.platform.PlatformLoader.LoadedPlatform;
import com.iconmaster.source.prototype.SourcePackage;
import java.util.ArrayList;

/**
 *
 * @author iconmaster
 */
@LoadedPlatform
public class PlatformTest extends Platform {
	
	public PlatformTest() {
		this.name = "Test";
		
		this.registerLibrary(new LibraryCore());
		
		transforms.add(new CompileUtils.IteratorTransformer(LibraryCore.range) {
			
			@Override
			public ArrayList<Operation> onCall(SourcePackage pkg, Object workingOn, String[] vars, ArrayList<Operation> forBlock) {
				return new ArrayList<>();
			}
		});
		
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
	public Object assemble(SourcePackage pkg) {
		return null;
	}

	@Override
	public Object run(SourcePackage pkg) {
		return null;
	}
	
}
